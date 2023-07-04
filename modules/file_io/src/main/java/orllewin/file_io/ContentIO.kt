package orllewin.file_io

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


object ContentIO {
    fun getContentFilename(contentResolver: ContentResolver, uri: Uri): String {
        var filename = uri.toString()
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        when {
            cursor != null && cursor.moveToFirst() -> {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                filename = cursor.getString(columnIndex)
            }
        }
        cursor?.close()
        return filename
    }

    fun getContentFilesize(contentResolver: ContentResolver, uri: Uri): Long {
        val fileDescriptor: AssetFileDescriptor? = contentResolver.openAssetFileDescriptor(uri, "r")
        return fileDescriptor?.length ?: 0

    }
}