package orllewin.filter.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filters")
class FilterEntity (
    @ColumnInfo(name = "filter_url") var filterUrl: String,
    @ColumnInfo(name = "filter_type") var filterType: Int
){
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}