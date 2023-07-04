package orllewin.browser

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import orllewin.bookmarks.ui.BookmarkDialog
import orllewin.bookmarks.ui.BookmarksActivity
import orllewin.history.ui.HistoryActivity
import orllewin.identities.IdentitiesActivity
import orllewin.logger.ui.LogActivity
import orllewin.settings.PreferenceActivity

class BrowserOverflow {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    var onNavigate: (address: String?) -> Unit = { _ -> }

    fun show(
        context: Context,
        root: View, anchor: View,
        viewModel: BrowserViewModel,
        onPrefs: () -> Unit,
        onViewSource: () -> Unit,
        onShare: () -> Unit,
        onSearch: () -> Unit){

        val popupMenu = PopupMenu(context, anchor)
        popupMenu.inflate(R.menu.main_overflow)

        val logMenu = popupMenu.menu.findItem(R.id.menu_action_log)
        logMenu.isVisible = false

        //menu divider only available post-P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) popupMenu.menu.setGroupDividerEnabled(true)

        this.onNavigate = { address ->
            viewModel.navigation(address)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.menu_action_search -> onSearch()
                R.id.menu_action_about -> AboutDialog(context).show()
                R.id.menu_action_add_bookmark -> {
                    BookmarkDialog(
                        context = context,
                        mode = BookmarkDialog.mode_new,
                        uri = viewModel.currentAddress,
                        name = viewModel.currentTitle
                    ){ _,  _ ->
                        //pop pop pop?
                    }.show()
                }
                R.id.menu_action_bookmarks -> gotoBookmarks(context)
                R.id.menu_action_history -> gotoHistory(context)
                R.id.menu_action_identities -> gotoIdentities(context, null)
                R.id.menu_action_share -> onShare()
                R.id.menu_action_view_source -> onViewSource.invoke()
                R.id.menu_action_settings -> onPrefs()
                R.id.menu_action_log -> context.startActivity(Intent(context, LogActivity::class.java))
                else -> {
                    //NOOP
                }
            }

            popupMenu.dismiss()

            true
        }

        popupMenu.show()
    }

    fun gotoHistory(context: Context) = resultLauncher.launch(Intent(context, HistoryActivity::class.java))
    fun gotoBookmarks(context: Context) = resultLauncher.launch(Intent(context, BookmarksActivity::class.java))
    fun gotoIdentities(context: Context, host: String?) {
        Intent(context, IdentitiesActivity::class.java).run {
            host?.let{
                this.putExtra("host", host)
            }
            resultLauncher.launch(this)
        }
    }

    fun registerActivityLauncher(activity: AppCompatActivity){
        resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let{ intent ->
                when {
                    intent.hasExtra("address") -> onNavigate(result.data?.getStringExtra("address"))
                    else -> {
                        //todo - certificate update?
                    }
                }
            }
        }
    }
}