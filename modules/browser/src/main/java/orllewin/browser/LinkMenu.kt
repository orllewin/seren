package orllewin.browser

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.PopupMenu

object LinkMenu {

    fun show(context: Context, uri: Uri, view: View, onMenuOption: (menuId: Int) -> Unit){
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.link_popup_menu)

        popupMenu.menu.findItem(R.id.menu_action_link_label).title = uri.toString()

        if(!uri.toString().startsWith("gemini://")){
            val filterMenuItem = popupMenu.menu.findItem(R.id.menu_add_to_filters)
            filterMenuItem.isVisible = false
        }

        popupMenu.setOnMenuItemClickListener { item ->
            onMenuOption(item.itemId)
            true
        }
        popupMenu.show()
    }
}