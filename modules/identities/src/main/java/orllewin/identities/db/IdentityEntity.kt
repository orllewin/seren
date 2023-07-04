package orllewin.identities.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entities")
class IdentityEntity(
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "alias") val alias: String?,
    @ColumnInfo(name = "uri") val uri: String?,
    @ColumnInfo(name = "rule") val rule: String?,
    @ColumnInfo(name = "private_key") val privateKey: ByteArray?,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}