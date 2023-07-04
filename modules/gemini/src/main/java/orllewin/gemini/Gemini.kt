package orllewin.gemini

import android.net.Uri
import androidx.core.net.toUri
import orllewin.gemini.identity.Identity
import orllewin.gemini.identity.IdentityRule
import orllewin.logger.Logger
import orllewin.lib.tls.DummyTrustManager
import orllewin.lib.tls.SerenDownloadManager
import orllewin.lib.tls.SerenKeyManager
import orllewin.lib.tls.SerenTrustManager
import java.io.*
import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object Gemini {

    private var defaultSocketFactory: SSLSocketFactory? = null
    private var downloadManager: SerenDownloadManager? = null

    private const val INPUT = 1
    private const val SUCCESS = 2
    private const val REDIRECT = 3
    private const val TEMPORARY_FAILURE = 4
    private const val PERMANENT_FAILURE = 5
    private const val CLIENT_CERTIFICATE_REQUIRED = 6
    private const val UNKNOWN = -1

    val identities: MutableList<Identity> = mutableListOf()

    var doNothingAddress: String? = null

    fun updateIdentities(_identities: List<Identity>){
        Logger.log("clearing identities")
        identities.clear()
        identities.addAll(_identities)
    }

    private fun findMatchingIdentity(request: Request): Identity?{
        identities.forEach { identity ->
            when {
                identity.uri.host.equals(request.uri.host) -> {
                    when {
                        identity.rule == IdentityRule.EntireDomain().key -> return identity
                        identity.rule == IdentityRule.SpecificUrl().key && request.uri.toString() == identity.uri.toString() -> return identity
                    }
                }
            }
        }

        return null
    }

    @Suppress("unused")
    fun getCodeString(code: Int): String{
        return when(code){
            1 -> "Input"
            2 -> "Success"
            3 -> "Redirect"
            4 -> "Temporary Failure"
            5 -> "Permanent Failure"
            6 -> "Client Certificate Required"
            -3 -> "Client Certificate Error"
            -2 -> "Bad response: Server Error"
            -1 -> "Connection Error"
            else -> "Unknown: $code"
        }
    }

    @Suppress("unused")
    fun initialiseDownloadManager(downloadManager: SerenDownloadManager){
        Logger.log("initialise download manager")
        Gemini.downloadManager = downloadManager
    }

    @Suppress("unused")
    fun initialiseTLS(trustManager: SerenTrustManager?) = initialiseTLS(null, trustManager)

    @Suppress("unused")
    fun initialiseTLS(keyManager: SerenKeyManager?) = initialiseTLS(keyManager, null)

    @Suppress("unused")
    fun initialiseTLS(keyManager: SerenKeyManager?, trustManager: SerenTrustManager?){
        Logger.log("initialise tls (key manager, trust manager)")
        val sslContext = SSLContext.getInstance("TLS")

        when (trustManager) {
            null -> sslContext.init(keyManager?.getFactory()?.keyManagers, DummyTrustManager.get(), null)
            else -> sslContext.init(keyManager?.getFactory()?.keyManagers, arrayOf(trustManager), null)
        }

        defaultSocketFactory = sslContext.socketFactory
    }

    private fun EmptyResponse(): Response{
        return Response.Empty()
    }

    @Suppress("unused")
    fun request(address: String, onResponse: (response: Response) -> Unit){
        if(address == doNothingAddress) {
            onResponse(Response.Empty())
            return
        }
        request(Request.Simple(Uri.parse(address)), onResponse)
    }

    @Suppress("unused")
    fun request(uri: Uri, onResponse: (response: Response) -> Unit){
        if(uri.toString() == doNothingAddress) {
            onResponse(Response.Empty())
            return
        }
        request(Request.Simple(uri), onResponse)
    }

    @Suppress("unused")
    fun request(request: Request, onResponse: (response: Response) -> Unit){
        if(request.uri.toString() == doNothingAddress) {
            onResponse(Response.Empty())
            return
        }
        when (defaultSocketFactory) {
            null -> {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, DummyTrustManager.get(), null)
                defaultSocketFactory = sslContext.socketFactory
            }
        }
        val socket: SSLSocket?

        try {
            val identity = findMatchingIdentity(request)
            socket = when {
                identity != null -> {
                    Logger.log("using identity sslcontext: ${identity.name}")
                    if(identity.sslContext == null) identity.createSSLContext()
                    identity.sslContext?.socketFactory?.createSocket(request.uri.host, request.port) as SSLSocket
                }
                else -> defaultSocketFactory?.createSocket(request.uri.host, request.port) as SSLSocket
            }
            socket.startHandshake()
        }catch (uhe: UnknownHostException){
            onResponse(Response.Error(request, "socket error, unknown host: $uhe"))
            return
        }catch (ce: ConnectException){
            onResponse(Response.Error(request, "socket error, connect exception: $ce"))
            return
        }catch (she: SSLHandshakeException){
            onResponse(Response.Error(request, "socket error, ssl handshake exception: $she"))
            return
        }

        val outputStreamWriter = OutputStreamWriter(socket.outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        val outWriter = PrintWriter(bufferedWriter)

        val requestEntity = request.uri.toString() + "\r\n"
        outWriter.print(requestEntity)
        outWriter.flush()

        if (outWriter.checkError()) {
            outWriter.close()
            onResponse(Response.Error(request, "socket error, output writer error"))
            return
        }

        val inputStream = socket.inputStream
        val headerInputReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(headerInputReader)
        val headerLine = bufferedReader.readLine()

        if(headerLine == null){
            onResponse(Response.Error(request, "socket error, server did not respond with a header line"))
            return
        }

        val header = parseHeader(headerLine)

        when {
            header.code == INPUT -> onResponse(Response.Input(request, header, request.uri))
            header.code == CLIENT_CERTIFICATE_REQUIRED -> onResponse(Response.IdentityRequired(request, header, request.uri))
            header.code == REDIRECT -> onResponse(
                Response.Redirect(
                    request,
                    request.uri,
                    Uri.parse(header.meta)
                )
            )
            header.code != SUCCESS -> onResponse(Response.Error(request, header.meta, header))
            header.meta.startsWith("text/gemini") -> {
                val lines = mutableListOf<String>()
                lines.addAll(bufferedReader.readLines())
                onResponse(Response.Gemtext(request, header, lines))
            }
            else -> {
                when (request) {
                    is Request.Simple -> onResponse(Response.File(request, header))
                    is Request.Image -> getBinary(socket, request, header, onResponse)
                    is Request.Download -> {
                        when {
                            header.meta.startsWith("text") -> getString(socket, request, header, onResponse)
                            else -> getBinary(socket, request, header, onResponse)
                        }
                    }
                }
            }
        }

        //Close input
        bufferedReader.close()
        headerInputReader.close()

        //Close output:
        outputStreamWriter.close()
        bufferedWriter.close()
        outWriter.close()
        socket.close()
    }

    fun synchronousRequest(request: Request): Response{

        when (defaultSocketFactory) {
            null -> {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, DummyTrustManager.get(), null)
                defaultSocketFactory = sslContext.socketFactory
            }
        }
        val socket: SSLSocket?

        try {
            val identity = findMatchingIdentity(request)
            socket = when {
                identity != null -> {
                    Logger.log("using identity sslcontext: ${identity.name}")
                    if(identity.sslContext == null) identity.createSSLContext()
                    identity.sslContext?.socketFactory?.createSocket(request.uri.host, request.port) as SSLSocket
                }
                else -> defaultSocketFactory?.createSocket(request.uri.host, request.port) as SSLSocket
            }
            socket.startHandshake()
        }catch (uhe: UnknownHostException){
            return Response.Error(request, "socket error, unknown host: $uhe")
        }catch (ce: ConnectException){
            return Response.Error(request, "socket error, connect exception: $ce")
        }catch (she: SSLHandshakeException){
            return Response.Error(request, "socket error, ssl handshake exception: $she")
        }

        val outputStreamWriter = OutputStreamWriter(socket.outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        val outWriter = PrintWriter(bufferedWriter)

        val requestEntity = request.uri.toString() + "\r\n"
        outWriter.print(requestEntity)
        outWriter.flush()

        if (outWriter.checkError()) {
            outWriter.close()
            return Response.Error(request, "socket error, output writer error")
        }

        val inputStream = socket.inputStream
        val headerInputReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(headerInputReader)
        val headerLine = bufferedReader.readLine() ?: return Response.Error(request, "socket error, server did not respond with a header line")

        val header = parseHeader(headerLine)

        when {
            header.code == INPUT -> return Response.Input(request, header, request.uri)
            header.code == CLIENT_CERTIFICATE_REQUIRED -> return Response.IdentityRequired(request, header, request.uri)
            header.code == REDIRECT -> return Response.Redirect(request, request.uri, Uri.parse(header.meta))
            header.code != SUCCESS -> return Response.Error(request, header.meta, header)
            header.meta.startsWith("text/gemini") -> {
                val lines = mutableListOf<String>()
                lines.addAll(bufferedReader.readLines())
                return Response.Gemtext(request, header, lines)
            }
            else -> {
                when (request) {
                    is Request.Simple -> return Response.File(request, header)
                    is Request.Image -> return getBinarySynchronous(socket, request, header)
                    is Request.Download -> {
                        when {
                            header.meta.startsWith("text") -> return getStringSynchronous(socket, request, header)
                            else -> return getBinarySynchronous(socket, request, header)
                        }
                    }
                }
            }
        }

        //Close input
        /*
        bufferedReader.close()
        headerInputReader.close()

        //Close output:
        outputStreamWriter.close()
        bufferedWriter.close()
        outWriter.close()
        socket.close()

         */
    }


    private fun getBinary(socket: SSLSocket?, request: Request, header: Header, onResponse: (response: Response) -> Unit){
        downloadManager?.let{ downloadManager ->
            downloadManager.getDownloadFile(request.uri).run {
                createNewFile()
                outputStream().use{ outputStream ->
                    socket?.inputStream?.copyTo(outputStream)
                    socket?.close()
                }

                when (request) {
                    is Request.Image -> onResponse(Response.Image(request, header, toUri()))
                    else -> onResponse(Response.Binary(request, header, toUri()))
                }
            }

        } ?: run{
            onResponse(Response.Error(request, "No download manager available"))
        }
    }

    private fun getBinarySynchronous(socket: SSLSocket?, request: Request, header: Header): Response {
        downloadManager?.let{ downloadManager ->
            downloadManager.getDownloadFile(request.uri).run {
                createNewFile()
                outputStream().use{ outputStream ->
                    socket?.inputStream?.copyTo(outputStream)
                    socket?.close()
                }

                when (request) {
                    is Request.Image -> return Response.Image(request, header, toUri())
                    else -> return Response.Binary(request, header, toUri())
                }
            }

        } ?: run{
            return Response.Error(request, "No download manager available")
        }
    }

    private fun getString(socket: SSLSocket?, request: Request, header: Header, onResponse: (response: Response) -> Unit){
        val content = socket?.inputStream?.bufferedReader().use {
            reader -> reader?.readText()
        }
        socket?.close()
        onResponse(Response.Text(request, header, content ?: "Error fetching content"))
    }

    private fun getStringSynchronous(socket: SSLSocket?, request: Request, header: Header): Response{
        val content = socket?.inputStream?.bufferedReader().use {
                reader -> reader?.readText()
        }
        socket?.close()
        return Response.Text(request, header, content ?: "Error fetching content")
    }

    private fun parseHeader(header: String): Header {
        val cleanHeader = header.replace("\\s+".toRegex(), " ")
        val meta = when {
            header.startsWith("2") -> {
                val segments = cleanHeader.trim().split(" ")
                when {
                    segments.size > 1 -> segments[1]
                    else -> "text/gemini; charset=utf-8"
                }
            }
            else -> {
                when {
                    cleanHeader.contains(" ") -> cleanHeader.substring(cleanHeader.indexOf(" ") + 1)
                    else -> cleanHeader
                }
            }
        }

        return when {
            header.startsWith("1") -> Header(INPUT, meta)
            header.startsWith("2") -> Header(SUCCESS, meta)
            header.startsWith("3") -> Header(REDIRECT, meta)
            header.startsWith("4") -> Header(TEMPORARY_FAILURE, meta)
            header.startsWith("5") -> Header(PERMANENT_FAILURE, meta)
            header.startsWith("6") -> Header(CLIENT_CERTIFICATE_REQUIRED, meta)
            else -> Header(UNKNOWN, meta)
        }
    }
}