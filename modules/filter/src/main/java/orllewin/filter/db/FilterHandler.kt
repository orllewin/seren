package orllewin.filter.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilterHandler(val db: FilterDatabase): FilterDatasource {
    override fun get(onFilters: (List<Filter>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val dbFilters = db.filters().getAll()
            val filters = mutableListOf<Filter>()

            dbFilters.forEach { filterEntity ->
                filters.add(Filter(filterEntity.filterUrl, filterEntity.filterType))
            }
            onFilters(filters.sortedBy { filter ->
                filter.filterUrl
            })
        }
    }

    override fun add(filter: Filter, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            db.filters().insertAll(arrayOf(FilterEntity(filter.filterUrl, filter.filterType)))
            onAdded()
        }
    }

    override fun add(filters: Array<Filter>, onAdded: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entities = filters.map { entry ->
                FilterEntity(entry.filterUrl,entry.filterType)
            }
            db.filters().insertAll(entities.toTypedArray())
            onAdded()
        }
    }

    override fun delete(filter: Filter, onDelete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO){
            val entity = db.filters().getFilter(filter.filterUrl)
            db.filters().delete(entity)
            onDelete()
        }
    }

    override fun update(
        filter: Filter,
        filterUrl: String?,
        filterType: Int?,
        onUpdate: () -> Unit
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val entity = db.filters().getFilter(filter.filterUrl)
            db.filters().delete(entity)
            db.filters().insertAll(arrayOf(FilterEntity("$filterUrl", filterType ?: Filter.TYPE_HIDE)))
            onUpdate()
        }
    }
}