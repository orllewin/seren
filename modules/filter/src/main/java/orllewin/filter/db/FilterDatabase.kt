package orllewin.filter.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FilterEntity::class], version = 1, exportSchema = false)
abstract class FilterDatabase: RoomDatabase() {
    abstract fun filters(): FilterDao
}