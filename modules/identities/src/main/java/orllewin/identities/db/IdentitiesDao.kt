package orllewin.identities.db

import androidx.room.*

@Dao
interface IdentitiesDao {
    @Query("SELECT * FROM entities")
    suspend fun getAll(): List<IdentityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(identities: Array<IdentityEntity>)

    @Query("UPDATE entities SET name=:name, uri=:uri, rule=:rule WHERE uid = :id")
    fun updateContent(id: Int, name: String, uri: String, rule: String)

    @Query("SELECT * from entities WHERE alias = :alias LIMIT 1")
    suspend fun getIdentity(alias: String): IdentityEntity

    @Delete
    suspend fun delete(identity: IdentityEntity)
}