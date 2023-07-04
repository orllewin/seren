package orllewin.filter.db

interface FilterDatasource {

    fun get(onFilters: (List<Filter>) -> Unit)
    fun add(filter: Filter, onAdded: () -> Unit)
    fun add(filters: Array<Filter>, onAdded: () -> Unit)
    fun delete(filter: Filter, onDelete: () -> Unit)
    fun update(filter: Filter, filterUrl: String?, filterType: Int?, onUpdate: () -> Unit)

}