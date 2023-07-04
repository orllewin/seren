package orllewin.filter.db

import android.content.Context
import androidx.room.Room

class Filters(context: Context) {

    private val db: FilterDatabase = Room.databaseBuilder(context, FilterDatabase::class.java, "ariane_filters_database_v1").build()
    private var filters: FilterHandler = FilterHandler(db)

    fun add(filter: Filter, onAdded: () -> Unit) { filters.add(filter) { onAdded() } }
    fun addAll(filterList: List<Filter>, onAdded: () -> Unit) { filters.add(filterList.toTypedArray()) { onAdded() } }
    fun get(onFilters: (List<Filter>) -> Unit) = filters.get { history -> onFilters(history) }
    fun delete(filter: Filter, onDelete: () -> Unit) = filters.delete(filter){ onDelete() }
    fun update(filter: Filter, filterUrl: String, filterType: Int, onUpdate: () -> Unit) = filters.update(filter, filterUrl, filterType){ onUpdate() }
}