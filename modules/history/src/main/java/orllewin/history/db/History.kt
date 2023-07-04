package orllewin.history.db

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room

class History(context: Context) {

    private val TAG = "History"
    private val db: HistoryDatabase = Room.databaseBuilder(context, HistoryDatabase::class.java, "seren_history_database_v1").build()
    private var history: HistoryHandler = HistoryHandler(db)

    fun add(title: String?, address: String) = history.add(title ?: "", Uri.parse(address)){
        Log.d(TAG, "History: address added: $address")
    }

    fun get(onEntries: (List<HistoryEntry>) -> Unit) = history.get { history ->
        onEntries(history)
    }

}