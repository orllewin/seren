package orllewin.gemini

import android.net.Uri

sealed class Request(val uri: Uri, val port: Int) {
    class Simple(uri: Uri): Request(uri, getPort(uri))
    class Image(uri: Uri): Request(uri, getPort(uri))
    class Download(uri: Uri): Request(uri, getPort(uri))

    companion object {
        private fun getPort(uri: Uri) = if (uri.port != -1) uri.port else 1965
    }
}