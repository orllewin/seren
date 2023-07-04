package orllewin.browser

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import orllewin.browser.databinding.DialogAboutBinding
import android.content.pm.PackageManager

import android.content.pm.PackageInfo
import android.net.Uri


class AboutDialog(context: Context){

    private var dialog: AlertDialog? = null

    init {
        val builder = MaterialAlertDialogBuilder(context, orllewin.resources.R.style.modern_material_dialog)

        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_about, null, false)
        val binding = DialogAboutBinding.bind(view)

        binding.versionLabel.text = versionName(context)

        builder.setView(view)

        builder.setNegativeButton("orllewin.uk"){ _, _ ->
            dialog?.dismiss()

            Intent(Intent.ACTION_VIEW).run {
                data = Uri.parse("https://orllewin.uk")
                context.startActivity(this)
            }
        }

        builder.setPositiveButton(orllewin.resources.R.string.close){ _, _ ->
            dialog?.dismiss()
        }

        dialog = builder.create()

    }

    private fun versionName(context: Context): String{
        try {
            val packageInfo: PackageInfo = context.getPackageManager().getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return ""
        }
    }

    fun show() = dialog?.show()
}