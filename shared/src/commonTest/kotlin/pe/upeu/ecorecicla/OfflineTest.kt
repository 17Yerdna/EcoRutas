package pe.upeu.ecorecicla
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pe.upeu.ecorecicla.domain.model.*
import pe.upeu.ecorecicla.data.repository.*
import pe.upeu.ecorecicla.data.remote.*

class OfflineTest {
    @Test fun humedadSobreviveDtoYDominio() {
        val dto=PesajeDto(1,1,"2026-10-28","PAPEL",10.0,2.0,8.0,"op",EstadoDto("REGISTRADO"),humedadEstimada=23.5)
        assertEquals(23.5,dto.dominio().humedadEstimada)
    }
    private val peso=Pesaje(-1,1,"2026-10-28T10:00:00-05:00","PAPEL",10.0,2.0,8.0,"op",EstadoPesaje.Registrado,true)
    @Test fun conversionDtoConservaNetoServidor() {
        val d=PesajeDto(1,1,peso.fechaHora,"PAPEL",10.0,2.0,8.0,"op",EstadoDto("REGISTRADO"))
        assertEquals(8.0,d.dominio().pesoNeto)
    }
    @Test fun erroresDistinguen401De403() { assertEquals(TipoError.SESION,traducirError(401)); assertEquals(TipoError.PROHIBIDO,traducirError(403)) }
    @Test fun registrarOfflineGeneraNegativosSinColision() = runTest {
        val local=Memoria(); val repo=PesajeRepositoryImpl(local,Remoto())
        repo.guardar(peso); repo.guardar(peso)
        assertEquals(listOf(-1L,-2L),local.filas.map { it.id })
        assertTrue(local.filas.all { it.pendiente })
    }
    @Test fun sincronizaEnOrdenYNoDuplica() = runTest {
        val local=Memoria(); val remoto=Remoto(); val repo=PesajeRepositoryImpl(local,remoto)
        repo.guardar(peso); repo.sincronizar(); repo.sincronizar()
        assertEquals(listOf("CREATE","DOWNLOAD","DOWNLOAD"),remoto.llamadas)
        assertEquals(1,local.filas.size); assertEquals(10L,local.filas.single().id)
    }
    @Test fun falloDeRedConservaPendienteHastaReconectar() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        repo.guardar(peso); r.caido=true
        assertFailsWith<ErrorApi> {repo.sincronizar()}
        assertEquals(-1L,l.filas.single().id); assertTrue(l.filas.single().pendiente)
        r.caido=false; repo.sincronizar(); assertFalse(l.filas.single().pendiente)
    }
    @Test fun correccionDeAltaPendienteNoGeneraOtraAlta() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        val p=repo.guardar(peso); repo.corregir(p.copy(pesoBruto=14.0))
        assertEquals(OperacionPendiente.CREATE,l.filas.single().operacion)
        repo.sincronizar(); assertEquals(1,r.llamadas.count {it=="CREATE"})
    }
    @Test fun descargaNoSobrescribeRegistroPendiente() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        val p=peso.copy(id=50,operacion=OperacionPendiente.NONE)
        l.guardar(p); r.datos=listOf(p.copy(pesoBruto=99.0,pendiente=false))
        repo.sincronizar(); assertEquals(10.0,l.filas.single().pesoBruto)
    }
    @Test fun anularAltaLocalLaCancelaSinEnviar() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        val p=repo.guardar(peso); repo.anular(p,"Cancelado"); repo.sincronizar()
        assertTrue(l.filas.isEmpty()); assertEquals(listOf("DOWNLOAD"),r.llamadas)
    }
    @Test fun ordenCreateUpdateAnulateDownload() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        l.guardar(peso); l.guardar(peso.copy(id=20,operacion=OperacionPendiente.UPDATE)); l.guardar(peso.copy(id=30,estado=EstadoPesaje.Anulado("Error"),operacion=OperacionPendiente.ANULATE))
        repo.sincronizar(); assertEquals(listOf("CREATE","UPDATE","ANULATE","DOWNLOAD"),r.llamadas)
    }
    @Test fun respuestaPerdidaNoDescartaCorreccionOffline() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        repo.guardar(peso.copy(claveCliente="estable")); r.perderRespuesta=true
        assertFailsWith<ErrorApi> {repo.sincronizar()}
        repo.corregir(l.filas.single().copy(pesoBruto=14.0))
        repo.sincronizar()
        assertEquals(14.0,l.filas.single().pesoBruto)
        assertTrue("UPDATE" in r.llamadas)
    }
    @Test fun cancelarTrasRespuestaPerdidaAnulaLaCreacionRemota() = runTest {
        val l=Memoria(); val r=Remoto(); val repo=PesajeRepositoryImpl(l,r)
        repo.guardar(peso.copy(claveCliente="cancelacion")); r.perderRespuesta=true
        assertFailsWith<ErrorApi> {repo.sincronizar()}
        repo.anular(l.filas.single(),"Cancelado")
        assertEquals(OperacionPendiente.ANULATE,l.filas.single().operacion)
        repo.sincronizar()
        assertTrue("ANULATE" in r.llamadas)
        assertEquals(10L,l.filas.single().id);assertFalse(l.filas.single().pendiente)
    }
    class Memoria: PesajeLocal {
        val filas=mutableListOf<Pesaje>()
        override suspend fun todos() = filas.toList()
        override suspend fun guardar(p: Pesaje) { filas.removeAll { it.id==p.id }; filas.add(p) }
        override suspend fun eliminar(id: Long) { filas.removeAll { it.id==id } }
    }
    class Remoto: PesajeRemoto {
        val llamadas=mutableListOf<String>()
        var caido=false
        var datos=emptyList<Pesaje>()
        var perderRespuesta=false
        var creado: Pesaje?=null
        override suspend fun crear(p: Pesaje): Pesaje {
            if(caido) throw ErrorApi(TipoError.SIN_CONEXION,"Sin red")
            llamadas.add("CREATE")
            if(creado==null) creado=p.copy(id=10,pendiente=false)
            if(perderRespuesta) {perderRespuesta=false;throw ErrorApi(TipoError.SIN_CONEXION,"Respuesta perdida")}
            return creado!!
        }
        override suspend fun corregir(p: Pesaje): Pesaje { llamadas.add("UPDATE"); return p.copy(pendiente=false) }
        override suspend fun anular(p: Pesaje) { llamadas.add("ANULATE") }
        override suspend fun descargar(): List<Pesaje> { llamadas.add("DOWNLOAD"); return datos }
    }
}
