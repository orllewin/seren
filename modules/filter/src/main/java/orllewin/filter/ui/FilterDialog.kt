package orllewin.filter.ui

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import orllewin.filter.db.Filter
import orllewin.filter.db.Filters
import orllewin.extensions.mainThread
import orllewin.filter.R
import orllewin.filter.databinding.DialogFilterBinding

class FilterDialog(
    context: Context,
    private val mode: Int,
    val filterUrl: String?,
    val filterType: Int?,
    onDismiss: (filterUrl: String?, mode: Int?) -> Unit): AppCompatDialog(context, R.style.ResourcesMainTheme) {

    companion object{
        const val mode_new = 0
        const val mode_edit = 1
    }

    init {
        val view = View.inflate(context, R.layout.dialog_filter, null)

        val binding = DialogFilterBinding.bind(view)

        setContentView(view)

        binding.filterToolbar.setNavigationIcon(R.drawable.vector_close)
        binding.filterToolbar.setNavigationOnClickListener {
            onDismiss(null, null)
            dismiss()
        }

        filterUrl?.let{
            binding.filterUrlEdit.setText(filterUrl)
        }

        when (filterType) {
            null -> binding.modeHideRadioButton.isChecked = true
            else -> {
                when(filterType){
                    Filter.TYPE_HIDE -> binding.modeHideRadioButton.isChecked = true
                    Filter.TYPE_WARNING -> binding.modeWarningRadioButton.isChecked = true
                }
            }
        }

        val filters = Filters(context)

        binding.filterToolbar.inflateMenu(R.menu.add_filter)
        binding.filterToolbar.setOnMenuItemClickListener { menuItem ->
            if(menuItem.itemId == R.id.menu_action_save_filter){
                val type = when {
                    binding.modeHideRadioButton.isChecked -> Filter.TYPE_HIDE
                    else -> Filter.TYPE_WARNING
                }
                if(mode == mode_new){
                    filters.add(Filter(binding.filterUrlEdit.text.toString(), type)) {
                        mainThread {
                            onDismiss(null, null)
                            dismiss()
                        }
                    }
                }else if(mode == mode_edit){
                    filters.update(Filter("$filterUrl", filterType ?: Filter.TYPE_HIDE), binding.filterUrlEdit.text.toString(), type){
                        mainThread {
                            onDismiss(null, null)
                            dismiss()
                        }
                    }
                }
            }
            true
        }
    }
}