package pe.upeu.ecorecicla

import android.os.Bundle
import android.net.ConnectivityManager
import android.net.Network
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pe.upeu.ecorecicla.presentation.EcoViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import pe.upeu.ecorecicla.di.platformModule

class MainActivity: ComponentActivity() {
    private lateinit var connectivity: ConnectivityManager
    private lateinit var callback: ConnectivityManager.NetworkCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val koin=GlobalContext.getOrNull() ?: startKoin { modules(platformModule(applicationContext)) }.koin
        val vm=ViewModelProvider(this,object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T = koin.get<EcoViewModel>() as T
        })[EcoViewModel::class.java]
        connectivity=getSystemService(ConnectivityManager::class.java)
        callback=object: ConnectivityManager.NetworkCallback() {override fun onAvailable(network: Network) {vm.sincronizar()} }
        connectivity.registerDefaultNetworkCallback(callback)
        setContent {App(vm)}
    }
    override fun onDestroy() {connectivity.unregisterNetworkCallback(callback);super.onDestroy()}
}
