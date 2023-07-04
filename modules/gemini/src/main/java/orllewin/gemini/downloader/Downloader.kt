package orllewin.gemini.downloader

import android.content.Context
import android.net.Uri
import androidx.work.*

class Downloader(val context: Context) {

    private val requests = mutableListOf<OneTimeWorkRequest>()

    fun enqueue(address: String, safUri: Uri){
        val data = Data.Builder()
            .putString(DownloadWorker.KEY_URL, address)
            .putString(DownloadWorker.KEY_LOCAL_FILE, safUri.toString())
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        requests.add(workRequest)

        WorkManager.getInstance(context).enqueueUniqueWork(
            address,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancel(url: String){
        WorkManager.getInstance(context).cancelUniqueWork(url)
    }
}