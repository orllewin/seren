package orllewin.browser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InputDialog(context: Context, meta: String, onInput: (input: String) -> Unit) {

    private var dialog: AlertDialog? = null

    init {
        val builder = MaterialAlertDialogBuilder(context, orllewin.resources.R.style.modern_material_dialog)

        val inputView: View = LayoutInflater.from(context).inflate(R.layout.input_dialog, null, false)

        val title = inputView.findViewById<AppCompatTextView>(R.id.input_title)
        title.text = meta

        builder.setView(inputView)

        builder.setNegativeButton(orllewin.resources.R.string.cancel){ _, _ ->
            dialog?.dismiss()
        }

        builder.setPositiveButton(orllewin.resources.R.string.submit){ _, _ ->
            val input = inputView.findViewById<AppCompatEditText>(R.id.input_edit).text.toString()
            onInput(input)
        }

        dialog = builder.create()

    }

    fun show() = dialog?.show()
}