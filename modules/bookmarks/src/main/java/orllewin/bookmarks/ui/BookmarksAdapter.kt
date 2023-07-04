package orllewin.bookmarks.ui


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orllewin.bookmarks.R
import orllewin.extensions.visible
import orllewin.bookmarks.databinding.BookmarkRowBinding
import orllewin.bookmarks.db.BookmarkEntry

class BookmarksAdapter(val onBookmark: (bookmarkEntry: BookmarkEntry) -> Unit, val onOverflow: (view: View, bookmarkEntry: BookmarkEntry, isFirst: Boolean, isLast: Boolean) -> Unit): RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {

    val bookmarks = mutableListOf<BookmarkEntry>()

    class ViewHolder(val binding: BookmarkRowBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun update(bookmarkEntries: List<BookmarkEntry>){
        this.bookmarks.clear()
        this.bookmarks.addAll(bookmarkEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_row, parent, false)
        return ViewHolder(BookmarkRowBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark = bookmarks[position]

        if(bookmark.visible) {
            holder.binding.root.visible(true)
            holder.binding.bookmarkName.text = bookmark.label
            holder.binding.bookmarkUri.text = bookmark.uri.toString()

            holder.binding.bookmarkLayout.setOnClickListener {
                onBookmark(bookmarks[holder.adapterPosition])
            }

            holder.binding.bookmarkOverflow.setOnClickListener { view ->
                val isFirst = (holder.adapterPosition == 0)
                val isLast = (holder.adapterPosition == bookmarks.size - 1)
                onOverflow(view, bookmarks[holder.adapterPosition], isFirst, isLast)
            }
        }else{
            holder.binding.root.visible(false)
        }
    }

    override fun getItemCount(): Int = bookmarks.size

    fun hide(bookmarkEntry: BookmarkEntry) {
        bookmarkEntry.visible = false
        notifyItemChanged(bookmarks.indexOf(bookmarkEntry))
    }

    fun show(bookmarkEntry: BookmarkEntry) {
        bookmarkEntry.visible = true
        notifyItemChanged(bookmarks.indexOf(bookmarkEntry))
    }

    fun remove(bookmarkEntry: BookmarkEntry){
        val index = bookmarks.indexOf(bookmarkEntry)
        bookmarks.remove(bookmarkEntry)
        notifyItemRemoved(index)
    }
}