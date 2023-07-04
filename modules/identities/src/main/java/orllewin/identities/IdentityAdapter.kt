package orllewin.identities

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orllewin.extensions.visible
import orllewin.gemini.identity.Identity
import orllewin.identities.databinding.IdentityRowBinding

class IdentityAdapter(val onIdentity: (identity: Identity) -> Unit, val onOverflow: (view: View, identity: Identity) -> Unit): RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {

    val identities = mutableListOf<Identity>()

    class ViewHolder(val binding: IdentityRowBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun update(identities: List<Identity>){
        this.identities.clear()
        this.identities.addAll(identities)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.identity_row, parent, false)
        return ViewHolder(IdentityRowBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val identity = identities[position]

        if(identity.visible) {
            holder.binding.root.visible(true)
            holder.binding.identityName.text = identity.name
            holder.binding.identityUri.text = identity.uri.toString()

            holder.binding.bookmarkLayout.setOnClickListener {
                onIdentity(identities[holder.adapterPosition])
            }

            holder.binding.identityOverflow.setOnClickListener { view ->
                onOverflow(view, identities[holder.adapterPosition])
            }
        }else{
            holder.binding.root.visible(false)
        }
    }

    override fun getItemCount(): Int = identities.size

    fun hide(identity: Identity) {
        identity.visible = false
        notifyItemChanged(identities.indexOf(identity))
    }

    fun show(identity: Identity) {
        identity.visible = true
        notifyItemChanged(identities.indexOf(identity))
    }

    fun remove(identity: Identity){
        val index = identities.indexOf(identity)
        identities.remove(identity)
        notifyItemRemoved(index)
    }
}