package orllewin.bookmarks.db

import java.net.URI

class BookmarkEntry(
    val uid: Int,
    val label: String,
    val uri: URI,
    val index: Int,
    val category: String?
){
    var visible = true
}