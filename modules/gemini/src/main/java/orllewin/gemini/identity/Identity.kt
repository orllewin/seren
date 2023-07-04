package orllewin.gemini.identity

import android.net.Uri
import orllewin.lib.certifikat.CertGen

import orllewin.lib.tls.DummyTrustManager
import java.security.KeyFactory
import java.security.KeyStore
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class Identity (
    val name: String,
    val alias: String,
    val uri: Uri,
    val rule: String,
    val privateKey: ByteArray){

    var visible = true
    var sslContext: SSLContext? = null

    fun createSSLContext(){
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            val cert = CertGen.get(alias)
            val keyFactory = KeyFactory.getInstance("RSA")
            val key = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKey))
            setKeyEntry(alias, key, null, arrayOf(cert))
        }

        val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        keyManagerFactory.init(ks, null)

        sslContext = SSLContext.getInstance("TLS")
        sslContext?.init(keyManagerFactory.keyManagers, DummyTrustManager.get(), null)
    }
}