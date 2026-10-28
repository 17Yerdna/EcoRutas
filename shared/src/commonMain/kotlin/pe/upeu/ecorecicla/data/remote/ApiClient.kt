package pe.upeu.ecorecicla.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import pe.upeu.ecorecicla.domain.model.*
import pe.upeu.ecorecicla.data.repository.PesajeRemoto

class ApiClient(private val client: HttpClient,private val url: String,private val token: () -> String?,private val al401: () -> Unit): PesajeRemoto {
    var alDescargarRuta: (Ruta) -> Unit = {}
    private suspend fun call(method: HttpMethod,path: String,body: JsonObject? = null): HttpResponse {
        val r=try { client.request(url+path) { this.method=method; contentType(ContentType.Application.Json); token()?.let { bearerAuth(it) }; if(body!=null) setBody(body) } }
        catch(e: CancellationException) { throw e }
        catch(e: Exception) { throw ErrorApi(TipoError.SIN_CONEXION,"Sin conexión. Sus cambios permanecen en el equipo") }
        if(!r.status.isSuccess()) {
            val error=runCatching { r.body<ErrorDto>() }.getOrElse { ErrorDto(status=r.status.value,message="Error HTTP ${r.status.value}") }
            if(r.status.value==401) al401()
            throw ErrorApi(traducirError(r.status.value),error.message,error.validationErrors)
        }
        return r
    }
    suspend fun login(usuario: String,contrasena: String): Sesion = call(HttpMethod.Post,"/auth/login",buildJsonObject { put("usuario",usuario); put("contrasena",contrasena) }).body()
    suspend fun ruta(): Ruta = call(HttpMethod.Get,"/rutas/dia").body()
    private fun cuerpo(p: Pesaje,crear: Boolean) = buildJsonObject {
        if(crear) { put("puntoId",p.puntoId); put("claveCliente",p.claveCliente) }
        put("material",p.material); put("pesoBruto",p.pesoBruto); put("tara",p.tara); put("observacion",p.observacion)
    }
    override suspend fun crear(p: Pesaje) = call(HttpMethod.Post,"/pesajes",cuerpo(p,true)).body<PesajeDto>().dominio()
    override suspend fun corregir(p: Pesaje) = call(HttpMethod.Put,"/pesajes/${p.id}",cuerpo(p,false)).body<PesajeDto>().dominio()
    override suspend fun anular(p: Pesaje) { call(HttpMethod.Delete,"/pesajes/${p.id}?motivo=${(p.estado as EstadoPesaje.Anulado).motivo.encodeURLParameter()}") }
    override suspend fun descargar(): List<Pesaje> {
        val r=ruta()
        val result=mutableListOf<Pesaje>()
        for(p in r.puntos) {
            var pagina=0
            do {
                val page=call(HttpMethod.Get,"/puntos/${p.id}/pesajes?pagina=$pagina&tamanio=100").body<Pagina<PesajeDto>>()
                result.addAll(page.contenido.map { it.dominio() }); pagina++
            } while(!page.ultima)
        }
        alDescargarRuta(r)
        return result
    }
    suspend fun omitir(rutaId: Long,puntoId: Long,motivo: String): Ruta = call(HttpMethod.Put,"/rutas/$rutaId/puntos/$puntoId/omision",buildJsonObject { put("motivo",motivo) }).body()
    suspend fun cerrar(rutaId: Long): Ruta = call(HttpMethod.Post,"/rutas/$rutaId/cierre").body()
}
