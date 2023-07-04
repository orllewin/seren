package orllewin.filter.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orllewin.filter.db.Filter
import orllewin.extensions.visible
import orllewin.filter.R
import orllewin.filter.databinding.FilterRowBinding

class FilterAdapter(val onFilter: (filter: Filter) -> Unit, val onOverflow: (view: View, filter: Filter) -> Unit): RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    val filters = mutableListOf<Filter>()

    class ViewHolder(val binding: FilterRowBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun update(filters: List<Filter>){
        this.filters.clear()
        this.filters.addAll(filters)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filter_row, parent, false)
        return ViewHolder(FilterRowBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filter = filters[position]

        if(filter.visible) {
            holder.binding.root.visible(true)
            holder.binding.filterUrl.text = filter.filterUrl

            when(filter.filterType){
                Filter.TYPE_HIDE -> holder.binding.filterType.text = "Remove from view"
                Filter.TYPE_WARNING -> holder.binding.filterType.text = "Mark with warning"
                else -> holder.binding.filterType.text = ""
            }

            holder.binding.root.setOnClickListener { _ ->
                onFilter(filters[holder.adapterPosition])
            }
            holder.binding.filterOverflow.setOnClickListener { view ->
                onOverflow(view, filters[holder.adapterPosition])
            }
        }else{
            holder.binding.root.visible(false)
        }
    }

    override fun getItemCount(): Int = filters.size

    fun hide(filter: Filter) {
        filter.visible = false
        notifyItemChanged(filters.indexOf(filter))
    }

    fun show(filter: Filter) {
        filter.visible = true
        notifyItemChanged(filters.indexOf(filter))
    }

    fun remove(filter: Filter){
        val index = filters.indexOf(filter)
        filters.remove(filter)
        notifyItemRemoved(index)
    }

}