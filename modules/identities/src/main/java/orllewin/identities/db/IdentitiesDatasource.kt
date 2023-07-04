package orllewin.identities.db

import orllewin.gemini.identity.Identity


interface IdentitiesDatasource {

    fun get(onIdentities: (List<Identity>) -> Unit)
    fun add(identity: Identity, onAdded: () -> Unit)
    fun add(identities: Array<Identity>, onAdded: () -> Unit)
    fun delete(identity: Identity, onDelete: () -> Unit)
    fun update(identity: Identity, onUpdate: () -> Unit)
}