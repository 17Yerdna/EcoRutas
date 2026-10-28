package pe.upeu.ecorecicla.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.upeu.ecorecicla.domain.model.*

interface PesajeLocal {
    suspend fun todos(): List<Pesaje>
    suspend fun guardar(p: Pesaje)
    suspend fun eliminar(id: Long)
    suspend fun siguienteId(): Long = minOf(0L,todos().minOfOrNull {it.id} ?: 0L)-1
    suspend fun reemplazar(localId: Long,p: Pesaje) { eliminar(localId); guardar(p) }
}
interface PesajeRemoto {
    suspend fun crear(p: Pesaje): Pesaje
    suspend fun corregir(p: Pesaje): Pesaje
    suspend fun anular(p: Pesaje)
    suspend fun descargar(): List<Pesaje>
}
class PesajeRepositoryImpl(private val local: PesajeLocal,private val remoto: PesajeRemoto) {
    private val mutex=Mutex()
    suspend fun guardar(p: Pesaje): Pesaje = mutex.withLock {
        val filas=local.todos()
        val id=if(p.id==0L || (p.id<0 && filas.any {it.id==p.id})) local.siguienteId() else p.id
        p.copy(id=id,pendiente=true,operacion=if(id<0) OperacionPendiente.CREATE else OperacionPendiente.UPDATE).also { local.guardar(it) }
    }
    suspend fun corregir(p: Pesaje) = mutex.withLock { local.guardar(p.copy(pendiente=true,operacion=if(p.id<0) OperacionPendiente.CREATE else OperacionPendiente.UPDATE)) }
    suspend fun anular(p: Pesaje,motivo: String) = mutex.withLock {
        if(p.id<0 && !p.intentoEnvio) local.eliminar(p.id) else local.guardar(p.copy(estado=EstadoPesaje.Anulado(motivo),pendiente=true,operacion=OperacionPendiente.ANULATE))
    }
    suspend fun sincronizar(): ResumenSync = mutex.withLock {
        var enviados=0
        for(op in listOf(OperacionPendiente.CREATE,OperacionPendiente.UPDATE,OperacionPendiente.ANULATE)) {
            for(p in local.todos().filter { it.pendiente && (it.operacion==op || (op==OperacionPendiente.CREATE && it.id<0)) }.sortedByDescending { it.id }) {
                val resultado=when(op) {
                    OperacionPendiente.CREATE -> {
                        local.guardar(p.copy(intentoEnvio=true))
                        val creado=remoto.crear(p)
                        if(p.operacion==OperacionPendiente.ANULATE) {
                            local.reemplazar(p.id,creado.copy(estado=p.estado,pendiente=true,operacion=OperacionPendiente.ANULATE))
                            enviados++; continue
                        }
                        if(creado.material!=p.material || creado.pesoBruto!=p.pesoBruto || creado.tara!=p.tara || creado.observacion!=p.observacion || creado.humedadEstimada!=p.humedadEstimada)
                            remoto.corregir(p.copy(id=creado.id)) else creado
                    }
                    OperacionPendiente.UPDATE -> remoto.corregir(p)
                    OperacionPendiente.ANULATE -> { remoto.anular(p); p }
                    else -> p
                }.copy(pendiente=false,operacion=OperacionPendiente.NONE)
                local.reemplazar(p.id,resultado); enviados++
            }
        }
        val recibidos=remoto.descargar()
        val pendientes=local.todos().filter { it.pendiente }.map { it.id }.toSet()
        recibidos.filter { it.id !in pendientes }.forEach { local.guardar(it) }
        ResumenSync(enviados,recibidos.size)
    }
}
