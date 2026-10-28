package pe.upeu.ecorecicla.data.remote

import kotlinx.serialization.Serializable
import pe.upeu.ecorecicla.domain.model.*

@Serializable data class EstadoDto(val tipo: String,val motivo: String = "")
@Serializable data class PesajeDto(val id: Long,val puntoId: Long,val fechaHora: String,val material: String,val pesoBruto: Double,val tara: Double,val pesoNeto: Double,val operario: String,val estado: EstadoDto,val observacion: String = "",val pendiente: Boolean = false) {
    fun dominio() = Pesaje(id,puntoId,fechaHora,material,pesoBruto,tara,pesoNeto,operario,if(estado.tipo=="ANULADO") EstadoPesaje.Anulado(estado.motivo) else EstadoPesaje.Registrado,false,observacion)
}
@Serializable data class Pagina<T>(val contenido: List<T>,val pagina: Int,val tamanio: Int,val totalElementos: Long,val totalPaginas: Int,val ultima: Boolean)
@Serializable data class ErrorDto(val timestamp: String = "",val status: Int = 500,val error: String = "",val message: String = "Error del servicio",val path: String = "",val validationErrors: Map<String,String> = emptyMap())
enum class TipoError { SESION, PROHIBIDO, VALIDACION, NO_ENCONTRADO, CONFLICTO, SERVIDOR, SIN_CONEXION }
fun traducirError(status: Int) = when(status) { 400 -> TipoError.VALIDACION; 401 -> TipoError.SESION; 403 -> TipoError.PROHIBIDO; 404 -> TipoError.NO_ENCONTRADO; 409 -> TipoError.CONFLICTO; else -> TipoError.SERVIDOR }
class ErrorApi(val tipo: TipoError,override val message: String,val campos: Map<String,String> = emptyMap()): Exception(message)
