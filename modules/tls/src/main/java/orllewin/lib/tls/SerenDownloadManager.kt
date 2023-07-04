package orllewin.lib.tls

import android.net.Uri
import java.io.File

interface SerenDownloadManager {
    fun getDownloadFile(uri: Uri): File
}