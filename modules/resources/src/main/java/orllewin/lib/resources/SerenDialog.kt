package orllewin.lib.resources

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import orllewin.resources.R

class SerenDialog(
    private val title: String?,
    private val content: String,
    private val negative: String?,
    private val onNegativeAction: () -> Unit,
    private val positive: String,
    private val onPositiveAction: () -> Unit): DialogFragment() {

    constructor(
        title: String?,
        content: String,
        positive: String,
        onPositiveAction: () -> Unit
    ) : this(
        title,
        content,
        null,
        { },
        positive,
        onPositiveAction
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.modern_material_dialog)
        title?.let {
            builder.setTitle(title)
        }

        builder.setMessage(content)

        negative?.let{
            builder.setNegativeButton(negative){ _, _ ->
                onNegativeAction()
            }
        }

        builder.setPositiveButton(positive){ _, _ ->
            onPositiveAction()
        }

        return builder.create()
    }

    override fun getTheme(): Int = R.style.modern_dialog
}