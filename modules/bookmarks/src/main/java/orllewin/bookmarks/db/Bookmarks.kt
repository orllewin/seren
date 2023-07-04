package orllewin.bookmarks.db

import android.content.Context
import androidx.room.Room

class Bookmarks(context: Context) {

    private val db: BookmarksDatabase = Room.databaseBuilder(context, BookmarksDatabase::class.java, "ariane_bookmarks_database_v1").build()
    private var bookmarks: BookmarksHandler = BookmarksHandler(db)

    fun add(bookmarkEntry: BookmarkEntry, onAdded: () -> Unit) {
        bookmarks.add(bookmarkEntry) { onAdded() }
    }

    fun add(bookmarkEntries: List<BookmarkEntry>, onAdded: () -> Unit) {
        bookmarks.add(bookmarkEntries.toTypedArray()) { onAdded() }
    }

    fun get(onEntries: (List<BookmarkEntry>) -> Unit) = bookmarks.get { history ->
        onEntries(history)
    }

    fun moveUp(entry: BookmarkEntry, onMoved: () -> Unit) = bookmarks.moveUp(entry) { onMoved() }
    fun moveDown(entry: BookmarkEntry, onMoved: () -> Unit) = bookmarks.moveDown(entry) { onMoved() }
    fun delete(entry: BookmarkEntry, onDelete: () -> Unit) = bookmarks.delete(entry){ onDelete() }
    fun update(entry: BookmarkEntry, label: String?, uri: String?, onUpdate: () -> Unit) =
        bookmarks.update(entry, label, uri){ onUpdate() }
}