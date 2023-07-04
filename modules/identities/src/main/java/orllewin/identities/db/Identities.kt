package orllewin.identities.db

import android.content.Context
import androidx.room.Room
import orllewin.gemini.identity.Identity

class Identities(context: Context) {

    private val db: IdentitiesDatabase = Room.databaseBuilder(context, IdentitiesDatabase::class.java, "ariane_identities_database_v1")
        .fallbackToDestructiveMigration()
        .build()
    private var identities: IdentitiesHandler = IdentitiesHandler(db)

    fun add(identity: Identity, onAdded: () -> Unit) {
        identities.add(identity) { onAdded() }
    }

    fun get(onIdentities: (List<Identity>) -> Unit) = identities.get { identities ->
        onIdentities(identities)
    }

    fun delete(identity: Identity, onDelete: () -> Unit) = identities.delete(identity){ onDelete() }
    fun update(identity: Identity, onUpdate: () -> Unit) = identities.update(identity){ onUpdate() }
}