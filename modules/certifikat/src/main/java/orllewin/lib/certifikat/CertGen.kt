package orllewin.lib.certifikat

import android.os.Build
import androidx.annotation.RequiresApi
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*


const val PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----\n"
const val PUBLIC_KEY_END = "\n-----END PUBLIC KEY-----\n"
const val PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----\n"
const val PRIVATE_KEY_END = "\n-----END PRIVATE KEY-----\n"

/**
 * Adapted from https://gist.github.com/fschutte/57f1f90bc6381effa4c486734de874e1
 * and http://blog.thilinamb.com/2010/01/how-to-generate-self-signed.html
 */
object CertGen {

    fun generate(commonName: String, years: Int, onKey: (keys: KeyPair, cert: X509Certificate) -> Unit){
        val keyPair = generateKeyPair()
        val publicPem = publicKeyToPem(keyPair.public)
        val privatePem = privateKeyToPem(keyPair.private)

        val notBefore = Date()

        val calendar = Calendar.getInstance()
        calendar.time = notBefore
        calendar.add(Calendar.YEAR, years)
        val notAfter = calendar.time

        val owner = X500Name("CN=$commonName")
        val builder: X509v3CertificateBuilder = JcaX509v3CertificateBuilder(
            owner, BigInteger(64, SecureRandom()), notBefore, notAfter, owner, keyPair.public
        )

        //val signer = JcaContentSignerBuilder("SHA256WithRSA").setProvider(BouncyCastleProvider()).build(keyPair.private)
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.private)
        val holder = builder.build(signer)

        //val x509Cert = JcaX509CertificateConverter().setProvider(BouncyCastleProvider()).getCertificate(holder)
        val x509Cert = JcaX509CertificateConverter().getCertificate(holder)

        onKey(keyPair, x509Cert)
    }

    @Suppress("unused")
    fun certificateFromEncoded(bytes: ByteArray): X509Certificate? {
        val inputStream = ByteArrayInputStream(bytes)
        return CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as? X509Certificate
    }

    @Suppress("unused")
    fun x509ToCertificate(x509: X509Certificate, onCert: (cert: Certificate) -> Unit){
        val cf = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(x509.encoded.inputStream())
        onCert(cert)
    }

    private fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        return kpg.genKeyPair()
    }

    fun certToPem(cert: Certificate): String{
        val sb = StringBuilder()
        sb.append("-----BEGIN CERTIFICATE-----\n")

        val base64CertBytes = android.util.Base64.encode(cert.encoded, android.util.Base64.DEFAULT)
        val base64Cert = String(base64CertBytes)
        sb.append(base64Cert.replace("(.{64})".toRegex(), "$1\n"))
        sb.append("-----END CERTIFICATE-----")
        return sb.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun publicKeyToPemO(publicKey: PublicKey): String {
        val base64PubKey = Base64.getEncoder().encodeToString(publicKey.encoded)
        return "$PUBLIC_KEY_BEGIN${base64PubKey.replace("(.{64})".toRegex(), "$1\n")}$PUBLIC_KEY_END"
    }

    fun publicKeyToPem(publicKey: PublicKey): String {
        val base64PubKeyBytes = android.util.Base64.encode(publicKey.encoded, android.util.Base64.DEFAULT)
        val base64PubKey = String(base64PubKeyBytes)
        return "$PUBLIC_KEY_BEGIN${base64PubKey.replace("(.{64})".toRegex(), "$1\n")}$PUBLIC_KEY_END"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun privateKeyToPemO(privateKey: PrivateKey): String {
        val base64PubKey = Base64.getEncoder().encodeToString(privateKey.encoded)
        return "$PRIVATE_KEY_BEGIN${base64PubKey.replace("(.{64})".toRegex(), "$1\n")}$PRIVATE_KEY_END"
    }

    fun privateKeyToPem(privateKey: PrivateKey): String {
        val base64PubKeyBytes = android.util.Base64.encode(privateKey.encoded, android.util.Base64.DEFAULT)
        val base64PubKey = String(base64PubKeyBytes)
        return "$PRIVATE_KEY_BEGIN${base64PubKey.replace("(.{64})".toRegex(), "$1\n")}$PRIVATE_KEY_END"
    }

    fun logKeystore(){
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases = ks.aliases().toList()

        when {
            aliases.isEmpty() -> println("ARIANE Keystore is empty")
            else -> {
                aliases.forEach { alias ->
                    println("ARIANE Keystore alias: $alias")
                }
            }
        }
    }

    fun addX509ToKeystore(alias: String, certificate: X509Certificate){
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        ks.setCertificateEntry(alias, certificate)
    }

    fun get(alias: String, onCertificate: (cert: Certificate) -> Unit){
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val cert = ks.getCertificate(alias)
        onCertificate(cert)
    }

    fun get(alias: String): Certificate {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        return ks.getCertificate(alias)
    }
}