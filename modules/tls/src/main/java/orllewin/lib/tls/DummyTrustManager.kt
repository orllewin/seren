package orllewin.lib.tls

import android.annotation.SuppressLint
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object DummyTrustManager{
    fun get(): Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                //NOOP
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                chain?.forEach { cert ->
                    println("Gemini server authType: $authType cert: ${cert.subjectDN}")
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )
}

