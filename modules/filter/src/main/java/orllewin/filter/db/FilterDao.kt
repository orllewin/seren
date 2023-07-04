package orllewin.filter.db

import androidx.room.*

@Dao
interface FilterDao {

    @Query("SELECT * FROM filters")
    suspend fun getAll(): List<FilterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(filters: Array<FilterEntity>)

    @Delete
    suspend fun delete(filter: FilterEntity)

    @Query("SELECT * FROM filters WHERE filter_url = :filterUrl LIMIT 1")
    suspend fun getFilter(filterUrl: String): FilterEntity

}