package orllewin.history.db

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class HistoryHandler(private val db: HistoryDatabase): HistoryDatasource {

    override fun get(onHistory: (List<HistoryEntry>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val dbBookmarks = db.history().getAll()
            val history = mutableListOf<HistoryEntry>()

            dbBookmarks.forEach { entity ->
                history.add(HistoryEntry(entity.uid, entity.timestamp ?: 0L, Uri.parse(entity.uri), entity.title ?: ""))
            }
            onHistory(history)
        }
    }

    override fun add(entry: HistoryEntry, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val lastAdded = db.history().getLastAdded()
            val entity = HistoryEntity(entry.title, entry.uri.toString(), System.currentTimeMillis())

            when (lastAdded) {
                null -> db.history().insert(entity)
                else -> {
                    when {
                        lastAdded.uri.toString() != entry.uri.toString() -> db.history().insert(entity)
                    }
                }
            }

            onAdded()
        }
    }

    override fun add(title: String, uri: Uri, onAdded: () -> Unit) {
        if(!uri.toString().startsWith("gemini://")){
            return
        }
        GlobalScope.launch(Dispatchers.IO){
            cull()

            val lastAdded = db.history().getLastAdded()
            val entity = HistoryEntity(title, uri.toString(), System.currentTimeMillis())

            when (lastAdded) {
                null -> db.history().insert(entity)
                else -> {
                    when {
                        lastAdded.uri.toString() != uri.toString() -> db.history().insert(entity)
                    }
                }
            }

            onAdded()
        }
    }

    private fun cull(){
        //Remove old entries
        val now = System.currentTimeMillis()
        val then = TimeUnit.DAYS.toMillis(30)
        db.history().cull(now - then)
    }

    override fun clear(onClear: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            db.history().clear()
            onClear()
        }
    }

    override fun delete(entry: HistoryEntry, onDelete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.history().getEntry(entry.uid)
            db.history().delete(entity)
            onDelete()
        }
    }
}