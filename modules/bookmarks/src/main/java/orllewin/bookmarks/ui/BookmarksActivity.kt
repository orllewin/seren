package orllewin.bookmarks.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import orllewin.bookmarks.db.BookmarkEntry
import orllewin.bookmarks.db.Bookmarks
import org.json.JSONArray
import org.json.JSONObject
import orllewin.bookmarks.R
import orllewin.bookmarks.databinding.ActivityBookmarksBinding
import orllewin.extensions.hide
import orllewin.extensions.mainThread
import orllewin.extensions.show
import orllewin.file_io.SafIO
import java.net.URI


class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var bookmarks: Bookmarks
    private lateinit var adapter: BookmarksAdapter
    private val safIO = SafIO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        safIO.registerForFileCreation(this)
        safIO.registerForFileOpen(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        binding.bookmarksRecycler.layoutManager = LinearLayoutManager(this)
        adapter = BookmarksAdapter({ entry ->
            Intent().run {
                putExtra("address", entry.uri.toString())
                setResult(RESULT_OK, this)
                finish()
            }
        }, { view: View, bookmarkEntry: BookmarkEntry, isFirst: Boolean, isLast: Boolean ->
            showBookmarkOverflow(view, bookmarkEntry, isFirst, isLast)
        })
        binding.bookmarksRecycler.adapter = adapter


        bookmarks = Bookmarks(this)
        bookmarks.get { bookmarks ->
            mainThread {
                when {
                    bookmarks.isEmpty() -> binding.empty.show()
                    else -> adapter.update(bookmarks)
                }
            }
        }
    }

    private fun showBookmarkOverflow(view: View, bookmarkEntry: BookmarkEntry, isFirst: Boolean, isLast: Boolean){
        //onOverflow
        val bookmarkOverflow = PopupMenu(this, view)

        bookmarkOverflow.inflate(R.menu.menu_bookmark)

        if(isFirst) bookmarkOverflow.menu.removeItem(R.id.menu_bookmark_move_up)
        if(isLast) bookmarkOverflow.menu.removeItem(R.id.menu_bookmark_move_down)

        bookmarkOverflow.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_bookmark_edit -> edit(bookmarkEntry)
                R.id.menu_bookmark_delete -> delete(bookmarkEntry)
                R.id.menu_bookmark_move_up -> moveUp(bookmarkEntry)
                R.id.menu_bookmark_move_down -> moveDown(bookmarkEntry)
            }
            true
        }

        bookmarkOverflow.show()
    }

    private fun edit(bookmarkEntry: BookmarkEntry){
        BookmarkDialog(
            this,
            BookmarkDialog.mode_edit,
            bookmarkEntry.uri.toString(),
            bookmarkEntry.label
        ){ label, uri ->
            bookmarks.update(bookmarkEntry, label, uri){
                bookmarks.get { bookmarks -> mainThread { adapter.update(bookmarks) } }
            }
        }.show()
    }

    /**
     *
     * Bookmark isn't actually deleted from the DB until the Snackbar disappears. Which is nice.
     *
     */
    @SuppressLint("ShowToast")
    private fun delete(bookmarkEntry: BookmarkEntry){
        //OnDelete
        adapter.hide(bookmarkEntry)
        Snackbar.make(binding.root, "Deleted ${bookmarkEntry.label}", Snackbar.LENGTH_SHORT).addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) = when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> adapter.show(bookmarkEntry)
                    else -> bookmarks.delete(bookmarkEntry) {
                        mainThread { adapter.remove(bookmarkEntry) }
                    }
                }
            }).setAction("Undo"){
            //Action listener unused
        }.show()
    }

    private fun moveUp(bookmarkEntry: BookmarkEntry){
        bookmarks.moveUp(bookmarkEntry){
            bookmarks.get { bookmarks -> mainThread { adapter.update(bookmarks) } }
        }
    }

    private fun moveDown(bookmarkEntry: BookmarkEntry){
        bookmarks.moveDown(bookmarkEntry){
            bookmarks.get { bookmarks -> mainThread { adapter.update(bookmarks) } }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bookmarks, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_bookmarks_import -> importBookmarks()
            R.id.menu_bookmarks_export -> exportBookmarks()
            else -> onBackPressed()
        }

        return true
    }

    private fun importBookmarks(){
        safIO.chooseFile("application/json"){ uri ->
            safIO.readFile(this, uri){ content ->
                val bookmarksJson = JSONObject(content)
                val bookmarksJsonArray = bookmarksJson.getJSONArray("bookmarks")
                val bookmarkEntries = arrayListOf<BookmarkEntry>()

                var skipped = 0
                var added = 0

                repeat(bookmarksJsonArray.length()){ index ->
                    val bookmark = bookmarksJsonArray.getJSONObject(index)
                    val bookmarkLabel = bookmark.getString("label")
                    val bookmarkUri = bookmark.getString("uri")
                    val bookmarkCategory = when {
                        bookmark.has("category") -> bookmark.getString("category")
                        else -> "default"
                    }
                    println("Importing bookmark: $bookmarkLabel : $uri")
                    val existing = adapter.bookmarks.filter {  entry ->
                        entry.uri.toString() == bookmarkUri
                    }
                    when {
                        existing.isNotEmpty() -> skipped++
                        else -> {
                            added++
                            bookmarkEntries.add(BookmarkEntry(-1, bookmarkLabel, URI.create(bookmarkUri), index, bookmarkCategory))
                        }
                    }
                }

                bookmarks.add(bookmarkEntries){
                    bookmarks.get { bookmarks ->
                        mainThread{
                            binding.empty.hide()
                            adapter.update(bookmarks)
                            when {
                                skipped > 0 -> {
                                    Toast.makeText(this, "$added bookmarks imported ($skipped duplicates)", Toast.LENGTH_SHORT).show()
                                }
                                else -> Toast.makeText(this, "$added bookmarks imported", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
            }
        }
    }

    private fun exportBookmarks(){
        safIO.newFile("ariane_bookmarks.json", "application/json"){ uri ->
            bookmarks.get { bookmarks ->
                val json = JSONObject()
                val bookmarksJson = JSONArray()

                bookmarks.forEach { entry ->
                    val bookmarkJson = JSONObject()
                    bookmarkJson.put("label", entry.label)
                    bookmarkJson.put("uri", entry.uri)
                    bookmarkJson.put("category", entry.category)
                    bookmarksJson.put(bookmarkJson)
                }

                json.put("bookmarks", bookmarksJson)

                safIO.saveTextToFile(this, uri, json.toString(2))

                Snackbar.make(binding.root, "Bookmarks exported", Snackbar.LENGTH_LONG)
                    .setAction("Share") {
                        safIO.shareFile(this, uri, "application/json")
                    }
                    .show()
            }
        }
    }
}