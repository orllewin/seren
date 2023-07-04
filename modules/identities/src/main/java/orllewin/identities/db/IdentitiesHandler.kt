package orllewin.identities.db

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import orllewin.gemini.identity.Identity

class IdentitiesHandler(private val db: IdentitiesDatabase): IdentitiesDatasource {

    override fun get(onIdentities: (List<Identity>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val dbIdentities = db.identities().getAll()
            val identities = mutableListOf<Identity>()

            dbIdentities.forEach { identityEntity ->
                identities.add(
                    Identity(
                        name = "${identityEntity.name}",
                        alias = "${identityEntity.alias}",
                        uri = Uri.parse(identityEntity.uri),
                        rule = "${identityEntity.rule}",
                        privateKey = identityEntity.privateKey ?: byteArrayOf()
                    )
                )
            }
            onIdentities(identities)
        }
    }

    override fun add(identity: Identity, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val identityEntity = IdentityEntity(
                name = identity.name,
                alias = identity.alias,
                uri = identity.uri.toString(),
                rule = identity.rule,
                privateKey = identity.privateKey)

            db.identities().insertAll(arrayOf(identityEntity))
            onAdded()
        }
    }

    override fun add(identities: Array<Identity>, onAdded: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(identity: Identity, onDelete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.identities().getIdentity(identity.alias)
            db.identities().delete(entity)
            onDelete()
        }
    }

    override fun update(identity: Identity, onUpdate: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.identities().getIdentity(identity.alias)
            db.identities().updateContent(entity.uid, identity.name, identity.uri.toString(), identity.rule)
            onUpdate()
        }
    }
}