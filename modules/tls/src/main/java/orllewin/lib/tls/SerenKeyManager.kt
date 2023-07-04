package orllewin.lib.tls

import javax.net.ssl.KeyManagerFactory

/*
    https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
 */
abstract class SerenKeyManager {
    abstract fun getFactory(): KeyManagerFactory?
}