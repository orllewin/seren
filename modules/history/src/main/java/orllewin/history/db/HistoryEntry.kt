package orllewin.history.db

import android.net.Uri

class HistoryEntry(
    val uid: Int,
    val timestamp: Long,
    val uri: Uri,
    val title: String
)