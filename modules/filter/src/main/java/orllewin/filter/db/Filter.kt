package orllewin.filter.db

class Filter(val filterUrl: String, val filterType: Int){
    companion object{
        const val TYPE_HIDE = 0
        const val TYPE_WARNING = 1
    }

    var visible = true

    fun isHide(): Boolean = filterType == TYPE_HIDE
    fun isWarning(): Boolean = filterType == TYPE_WARNING
}