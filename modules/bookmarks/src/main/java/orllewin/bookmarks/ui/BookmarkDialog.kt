package orllewin.bookmarks.ui

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import orllewin.bookmarks.R
import orllewin.extensions.mainThread
import orllewin.bookmarks.databinding.DialogBookmarkBinding
import orllewin.bookmarks.db.BookmarkEntry
import orllewin.bookmarks.db.Bookmarks
import java.net.URI


class BookmarkDialog(
    context: Context,
    private val mode: Int,
    val uri: String,
    val name: String,
    onDismiss: (label: String?, uri: String?) -> Unit) : AppCompatDialog(context, R.style.BookmarkDialog) {

    companion object{
        const val mode_new = 0
        const val mode_edit = 1
    }

    init {
        val view = View.inflate(context, R.layout.dialog_bookmark, null)

        val binding = DialogBookmarkBinding.bind(view)

        setContentView(view)

        val bookmarks = Bookmarks(context)

        binding.bookmarkToolbar.setNavigationIcon(R.drawable.vector_close)
        binding.bookmarkToolbar.setNavigationOnClickListener {
            onDismiss(null, null)
            dismiss()
        }

        binding.bookmarkName.setText(name)
        binding.bookmarkUri.setText(uri)

        binding.bookmarkToolbar.inflateMenu(R.menu.add_bookmark)
        binding.bookmarkToolbar.setOnMenuItemClickListener { menuItem ->
            if(menuItem.itemId == R.id.menu_action_save_bookmark){

                if(mode == mode_new) {
                    //Determine index:
                    //todo - this is expensive, just get last item, limit1?
                    bookmarks.get { allBookmarks ->

                        val index = when {
                            allBookmarks.isEmpty() -> 0
                            else -> allBookmarks.last().index + 1
                        }

                        bookmarks.add(
                            BookmarkEntry(
                                uid = -1,
                                label = binding.bookmarkName.text.toString(),
                                uri = URI.create(binding.bookmarkUri.text.toString()),
                                index = index,
                                category = "default"
                            )
                        ) {
                            mainThread {
                                onDismiss(null, null)
                                dismiss()
                            }
                        }
                    }
                }else if(mode == mode_edit){
                    onDismiss(
                        binding.bookmarkName.text.toString(),
                        binding.bookmarkUri.text.toString())
                    dismiss()
                }
            }

            true

        }
    }
}