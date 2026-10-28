package pe.upeu.ecorecicla.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.serialization.json.*
import pe.upeu.ecorecicla.domain.model.Sesion

class AlmacenSeguroAndroid(context: Context): AlmacenSeguro {
    private val prefs=context.getSharedPreferences("eco_secure",Context.MODE_PRIVATE)
    private val json=Json { ignoreUnknownKeys=true }
    private val key: SecretKey get() {
        val store=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("eco_session",null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply { init(KeyGenParameterSpec.Builder("eco_session",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()) }.generateKey()
    }
    override fun guardar(s: Sesion) {
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE,key) }
        val encrypted=cipher.doFinal(json.encodeToString(s).toByteArray())
        check(prefs.edit().putString("iv",Base64.encodeToString(cipher.iv,Base64.NO_WRAP)).putString("data",Base64.encodeToString(encrypted,Base64.NO_WRAP)).commit())
    }
    override fun leer(): Sesion? = runCatching {
        val data=prefs.getString("data",null) ?: return null
        val iv=Base64.decode(prefs.getString("iv",null),Base64.NO_WRAP)
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE,key,GCMParameterSpec(128,iv)) }
        val s=json.decodeFromString<Sesion>(String(cipher.doFinal(Base64.decode(data,Base64.NO_WRAP))))
        val payload=String(Base64.decode(s.token.split('.')[1],Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        val exp=json.parseToJsonElement(payload).jsonObject["exp"]!!.jsonPrimitive.long
        if(exp*1000<=System.currentTimeMillis()) { borrar(); null } else s
    }.getOrElse { borrar(); null }
    override fun borrar() { check(prefs.edit().clear().commit()) }
}
