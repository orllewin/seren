package orllewin.bookmarks.db

import androidx.room.Database
import androidx.room.RoomDatabase
import orllewin.bookmarks.db.BookmarkEntity
import orllewin.bookmarks.db.BookmarksDao

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
abstract class BookmarksDatabase: RoomDatabase() {
    abstract fun bookmarks(): BookmarksDao
}