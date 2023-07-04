package orllewin.identities.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IdentityEntity::class], version = 2, exportSchema = false)
abstract class IdentitiesDatabase: RoomDatabase() {
    abstract fun identities(): IdentitiesDao
}