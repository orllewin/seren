package orllewin.gemini.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import orllewin.gemini.Gemini
import orllewin.gemini.R
import orllewin.gemini.Request
import orllewin.gemini.Response
import java.io.FileOutputStream


class DownloadWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    companion object{
        const val KEY_URL = "oppen.gemini.downloader.KEY_URL"
        const val KEY_LOCAL_FILE = "oppen.gemini.downloader.KEY_LOCAL_FILE"
        const val KEY_APP_NAME = "oppen.gemini.downloader.KEY_APP_NAME"

        const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
        const val NOTIFICATION_ID = 1
    }

    override fun doWork(): Result {
        val appName = inputData.getString(KEY_APP_NAME) ?: "Unknown"
        val address = inputData.getString(KEY_URL) ?: "Unknown"
        val localFile = inputData.getString(KEY_LOCAL_FILE) ?: "Unknown"

        notification(appName, address, "Downloading...", null, null)

        val request = Request.Download(Uri.parse(address))
        when(val response = Gemini.synchronousRequest(request)){
            is Response.Image -> {
                notification(appName, address, "Image", null, null)
            }
            is Response.Binary -> {
                val localUri = Uri.parse(localFile)
                applicationContext.contentResolver.openFileDescriptor(localUri, "w")?.use {  parcelFileDescriptor ->
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).use{ fileOutputStream ->
                        response.file.toFile().inputStream().copyTo(fileOutputStream)
                    }
                }
                notification(appName, address, "Downloaded", localUri, address)
            }
            is Response.Error -> {
                notification(appName, address, "Error: ${response.error}", null, null)
            }
            else -> {
                notification(appName, address, "Other response", null, null)
            }
        }

        return Result.success()
    }


    private fun notification(appName: String, title: String, message: String, localUri: Uri?, address: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "${appName}_notifications"
            val description = "$appName file uploads"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.vector_cloud_download)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        localUri?.let{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val extension = MimeTypeMap.getFileExtensionFromUrl(address)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            println("DLOADR: $extension : $mime")
            intent.setDataAndType(localUri, mime)
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            builder.setAutoCancel(true)
            builder.setContentIntent(pendingIntent)
        }

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, builder.build())
    }
}