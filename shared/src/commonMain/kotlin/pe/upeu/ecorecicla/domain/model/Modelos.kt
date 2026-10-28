package pe.upeu.ecorecicla.domain.model

import kotlinx.serialization.Serializable

@Serializable data class PuntoAcopio(val id: Long,val razonSocial: String,val direccion: String,val zona: String,val materialHabitual: String,val secuencia: Int,val atendido: Boolean,val motivoOmision: String = "")
@Serializable sealed class EstadoPesaje {
    @Serializable data object Registrado: EstadoPesaje()
    @Serializable data class Anulado(val motivo: String): EstadoPesaje()
}
@Serializable enum class OperacionPendiente { NONE, CREATE, UPDATE, ANULATE }
@Serializable data class Pesaje(val id: Long,val puntoId: Long,val fechaHora: String,val material: String,val pesoBruto: Double,val tara: Double,val pesoNeto: Double,val operario: String,val estado: EstadoPesaje,val pendiente: Boolean,val observacion: String = "",val operacion: OperacionPendiente = if(pendiente) OperacionPendiente.CREATE else OperacionPendiente.NONE,val claveCliente: String = "",val intentoEnvio: Boolean = false)
@Serializable data class Sesion(val token: String,val usuario: String,val rol: String,val privilegios: Set<String>)
@Serializable data class Ruta(val id: Long,val fecha: String,val cerrada: Boolean,val puntos: List<PuntoAcopio>)
data class ResumenSync(val enviados: Int,val recibidos: Int)
