package orllewin.browser

import android.content.Context
import android.net.Uri
import orllewin.lib.tls.SerenDownloadManager
import java.io.File

class DownloadManager(val context: Context): SerenDownloadManager {

    override fun getDownloadFile(uri: Uri): File {
        var filename: String? = null
        val fileSegmentIndex: Int = uri.path!!.lastIndexOf('/')

        when {
            fileSegmentIndex != -1 -> filename = uri.path!!.substring(fileSegmentIndex + 1)
        }

        val host = uri.host?.replace(".", "_")
        val cacheName = "${host}_$filename"
        val cacheFile = File(context.cacheDir, cacheName)
        if(cacheFile.exists()) cacheFile.delete()
        return cacheFile
    }
}