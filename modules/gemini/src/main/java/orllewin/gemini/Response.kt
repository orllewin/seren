package orllewin.gemini

import android.net.Uri

sealed class Response(val request: Request?) {
    class Empty: Response(null)
    class Gemtext(request: Request?, val header: Header, val lines: List<String>): Response(request)
    class Input(request: Request?, val header: Header, val uri: Uri): Response(request)
    class IdentityRequired(request: Request?, val header: Header, val uri: Uri): Response(request)
    class Redirect(request: Request?, val original: Uri, val redirect: Uri): Response(request)
    class File(request: Request?, val header: Header): Response(request)
    class Text(request: Request?, val header: Header, val text: String): Response(request)
    class Image(request: Request?, val header: Header, val file: Uri): Response(request)
    class Binary(request: Request?, val header: Header, val file: Uri): Response(request)
    class Error(request: Request?, val error: String, val header: Header? = null): Response(request)
}