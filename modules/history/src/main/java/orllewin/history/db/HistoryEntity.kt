package orllewin.history.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
class HistoryEntity(
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "uri") val uri: String?,
    @ColumnInfo(name = "timestamp") val timestamp: Long?
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}