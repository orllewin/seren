package orllewin.identities

import orllewin.gemini.identity.Identity
import orllewin.lib.certifikat.CertGen
import orllewin.lib.tls.SerenKeyManager
import java.io.ByteArrayInputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory

/**
 * Old impl: https://codeberg.org/oppenlab/Ariane/src/branch/main/app/src/main/java/oppen/ariane/io/keymanager/ArianeKeyManager.kt
 *
 *
 */
class IdentityKeyManager(val identities: List<Identity>): SerenKeyManager() {
    override fun getFactory(): KeyManagerFactory? {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            val aliases = aliases().toList()
            val first = aliases.first()
            val cert = CertGen.get(first)
            load(ByteArrayInputStream(cert.encoded), "".toCharArray())
        }

        val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("X509")
        keyManagerFactory.init(ks, "".toCharArray())

        return keyManagerFactory
    }
}