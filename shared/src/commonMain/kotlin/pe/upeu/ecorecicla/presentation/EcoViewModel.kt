package pe.upeu.ecorecicla.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.upeu.ecorecicla.domain.model.*
import pe.upeu.ecorecicla.data.local.*
import pe.upeu.ecorecicla.data.local.db.EcoDb
import pe.upeu.ecorecicla.data.remote.*
import pe.upeu.ecorecicla.data.repository.*
import pe.upeu.ecorecicla.data.security.AlmacenSeguro

data class EcoUi(val sesion: Sesion? = null,val ruta: Ruta? = null,val pesajes: List<Pesaje> = emptyList(),val ultimaSync: String = "Nunca",val cargando: Boolean = false,val mensaje: String = "",val campos: Map<String,String> = emptyMap(),val confirmarSalida: Boolean = false,val puntosFiltrados: List<PuntoAcopio> = emptyList(),val busqueda: String = "",val soloPendientes: Boolean = false)
@OptIn(ExperimentalCoroutinesApi::class)
class EcoViewModel(private val db: EcoDb,private val seguro: AlmacenSeguro,client: HttpClient,private val ahora: () -> String,private val uuid: () -> String): ViewModel() {
    private val _ui=MutableStateFlow(EcoUi())
    val ui: StateFlow<EcoUi> = _ui.asStateFlow()
    private var local: BaseLocal? = null
    private var repo: PesajeRepositoryImpl? = null
    private var observadores: Job? = null
    private val filtros=MutableStateFlow("" to false)
    fun filtrar(texto: String,solo: Boolean) {filtros.value=texto to solo; _ui.update {it.copy(busqueda=texto,soloPendientes=solo)}}
    private val api=ApiClient(client,"http://10.0.2.2:8080/api/v1",{ _ui.value.sesion?.token },{ seguro.borrar(); observadores?.cancel(); local=null; repo=null; _ui.value=EcoUi(mensaje="Sesión vencida. Ingrese nuevamente; sus pendientes permanecen guardados") })
    init { seguro.leer()?.let(::activar) }
    private fun activar(s: Sesion) {
        observadores?.cancel()
        val l=BaseLocal(db,s.usuario); local=l; repo=PesajeRepositoryImpl(l,api)
        _ui.value=EcoUi(sesion=s)
        api.alDescargarRuta={ l.guardarRuta(it,ahora()) }
        observadores=viewModelScope.launch {
            launch {filtros.flatMapLatest {l.filtrar(it.first,it.second,ahora().take(10))}.collect {p -> _ui.update {it.copy(puntosFiltrados=p)}}}
            launch { l.ruta.collect { r -> _ui.update { it.copy(ruta=r) } } }
            launch { l.pesajes.collect { p -> _ui.update { it.copy(pesajes=p) } } }
            launch { l.ultimaSync.collect { t -> _ui.update { it.copy(ultimaSync=t) } } }
        }
    }
    private fun accion(block: suspend () -> Unit) { viewModelScope.launch {
        _ui.update { it.copy(cargando=true,mensaje="",campos=emptyMap()) }
        try { withContext(Dispatchers.IO) { block() } }
        catch(e: CancellationException) { throw e }
        catch(e: ErrorApi) { _ui.update { it.copy(mensaje=e.message,campos=e.campos) } }
        catch(e: Exception) { _ui.update { it.copy(mensaje=e.message ?: "No se pudo completar la operación") } }
        finally { _ui.update { it.copy(cargando=false) } }
    } }
    fun login(usuario: String,contrasena: String) = accion {
        val s=api.login(usuario,contrasena); seguro.guardar(s); withContext(Dispatchers.Main) { activar(s) }; sincronizarInterno()
    }
    private suspend fun sincronizarInterno() {
        val resumen=repo?.sincronizar() ?: return
        _ui.update { it.copy(mensaje="Sincronización: ${resumen.enviados} enviados, ${resumen.recibidos} recibidos") }
    }
    fun sincronizar() { if(!_ui.value.cargando && _ui.value.sesion!=null) accion { sincronizarInterno() } }
    fun nuevaClave() = uuid()
    fun guardar(puntoId: Long,material: String,bruto: String,tara: String,observacion: String,humedad: String,anterior: Pesaje?,claveDraft: String,alGuardar: () -> Unit) = accion {
        val s=_ui.value.sesion ?: return@accion
        val b=bruto.toDoubleOrNull(); val t=tara.toDoubleOrNull()
        if(b==null || t==null || !b.isFinite() || !t.isFinite() || t<0 || b<=t) throw ErrorApi(TipoError.VALIDACION,"Revise bruto y tara",mapOf("pesoBruto" to "Debe ser mayor que la tara","tara" to "Debe ser no negativa"))
        val h=humedad.toDoubleOrNull()
        if(h==null || !h.isFinite() || h !in 0.0..100.0) throw ErrorApi(TipoError.VALIDACION,"Revise la humedad estimada",mapOf("humedadEstimada" to "Debe estar entre 0 y 100"))
        val existente=anterior ?: local!!.todos().firstOrNull {it.claveCliente==claveDraft}
        val p=existente?.copy(material=material,pesoBruto=b,tara=t,observacion=observacion,humedadEstimada=h) ?: Pesaje(0,puntoId,ahora(),material,b,t,0.0,s.usuario,EstadoPesaje.Registrado,true,observacion,claveCliente=claveDraft,humedadEstimada=h)
        if(existente==null) repo!!.guardar(p) else repo!!.corregir(p)
        _ui.update { it.copy(mensaje="Guardado en el equipo. Pendiente de sincronizar") }
        try { sincronizarInterno() } catch(e: ErrorApi) {
            if(e.tipo!=TipoError.SIN_CONEXION) throw e
            _ui.update {it.copy(mensaje="Guardado sin conexión. Pendiente de sincronizar")}
        }
        withContext(Dispatchers.Main) {alGuardar()}
    }
    fun anular(p: Pesaje,motivo: String) = accion { require(motivo.isNotBlank()) { "Motivo requerido" }; repo!!.anular(p,motivo); sincronizarInterno() }
    fun omitir(puntoId: Long,motivo: String) = accion { require(motivo.isNotBlank()) { "Motivo requerido" }; val r=api.omitir(_ui.value.ruta!!.id,puntoId,motivo); local!!.guardarRuta(r,ahora()) }
    fun cerrar() = accion { val r=api.cerrar(_ui.value.ruta!!.id); local!!.guardarRuta(r,ahora()) }
    fun salir(descartar: Boolean = false) = accion {
        val l=local
        if(!descartar && l?.todos()?.any { it.pendiente }==true) {
            runCatching { sincronizarInterno() }
            if(l.todos().any { it.pendiente }) { _ui.update { it.copy(confirmarSalida=true) }; return@accion }
        }
        observadores?.cancel(); l?.limpiar(); seguro.borrar(); local=null; repo=null; _ui.value=EcoUi()
    }
    fun cancelarSalida() { _ui.update { it.copy(confirmarSalida=false) } }
}
