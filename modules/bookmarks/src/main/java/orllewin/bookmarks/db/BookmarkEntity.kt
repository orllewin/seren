package orllewin.bookmarks.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
class BookmarkEntity(
    @ColumnInfo(name = "label") val label: String?,
    @ColumnInfo(name = "uri") val uri: String?,
    @ColumnInfo(name = "uiIndex") val uiIndex: Int?,
    @ColumnInfo(name = "category") val category: String?
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}