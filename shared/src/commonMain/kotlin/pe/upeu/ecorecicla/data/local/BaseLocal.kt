package pe.upeu.ecorecicla.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import pe.upeu.ecorecicla.data.local.db.EcoDb
import pe.upeu.ecorecicla.data.repository.PesajeLocal
import pe.upeu.ecorecicla.domain.model.*

class BaseLocal(private val db: EcoDb, val usuario: String): PesajeLocal {
    private val q=db.ecoQueries
    private val json=Json { ignoreUnknownKeys=true; encodeDefaults=true }
    val pesajes=q.todos(usuario).asFlow().mapToList(Dispatchers.Default).map { rows -> rows.map { json.decodeFromString<Pesaje>(it.payload) } }
    val ruta=q.ruta(usuario).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.let { row -> json.decodeFromString<Ruta>(row.payload) } }
    val ultimaSync=q.ruta(usuario).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.ultima_sync ?: "Nunca" }
    override suspend fun todos()=q.todos(usuario).executeAsList().map { json.decodeFromString<Pesaje>(it.payload) }
    override suspend fun guardar(p: Pesaje) { q.guardarPesaje(usuario,p.id,p.puntoId,p.fechaHora,p.material,p.pesoBruto,p.tara,p.pesoNeto,p.operario,if(p.estado is EstadoPesaje.Anulado) "ANULADO" else "REGISTRADO",if(p.pendiente) 1 else 0,p.operacion.name,json.encodeToString(p)) }
    override suspend fun eliminar(id: Long) { q.eliminarPesaje(usuario,id) }
    override suspend fun reemplazar(localId: Long,p: Pesaje) { db.transaction { q.eliminarPesaje(usuario,localId); q.guardarPesaje(usuario,p.id,p.puntoId,p.fechaHora,p.material,p.pesoBruto,p.tara,p.pesoNeto,p.operario,if(p.estado is EstadoPesaje.Anulado) "ANULADO" else "REGISTRADO",if(p.pendiente) 1 else 0,p.operacion.name,json.encodeToString(p)) } }
    fun guardarRuta(r: Ruta,fecha: String) { db.transaction {
        q.guardarRuta(usuario,json.encodeToString(r),fecha); q.limpiarPuntos(usuario)
        r.puntos.forEach { p -> q.guardarPunto(usuario,p.id,p.razonSocial,p.direccion,p.zona,p.materialHabitual,p.secuencia.toLong(),if(p.atendido) 1 else 0) }
    } }
    fun limpiar() { db.transaction { q.limpiarPesajes(usuario); q.limpiarPuntos(usuario); q.limpiarRuta(usuario) } }
}
