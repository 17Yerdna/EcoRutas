package pe.upeu.ecorecicla.di

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import pe.upeu.ecorecicla.data.local.baseLocalAndroid
import pe.upeu.ecorecicla.data.security.*
import pe.upeu.ecorecicla.presentation.EcoViewModel
import java.text.SimpleDateFormat
import java.util.*

fun platformModule(context: Context)=module {
    single {baseLocalAndroid(context)}
    single<AlmacenSeguro> {AlmacenSeguroAndroid(context)}
    single {HttpClient(OkHttp) {
        install(ContentNegotiation) {json(Json {ignoreUnknownKeys=true;encodeDefaults=true})}
        install(HttpTimeout) {requestTimeoutMillis=15000;connectTimeoutMillis=8000}
        install(Logging) {level=LogLevel.NONE;sanitizeHeader {it.equals("Authorization",true)}}
    }}
    factory {EcoViewModel(get(),get(),get(),{SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX",Locale.US).apply {timeZone=TimeZone.getTimeZone("America/Lima")}.format(Date())},{UUID.randomUUID().toString()})}
}
