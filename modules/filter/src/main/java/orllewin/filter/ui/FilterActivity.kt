package orllewin.filter.ui

import android.annotation.SuppressLint
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
import orllewin.filter.db.Filter
import orllewin.filter.db.Filters
import orllewin.extensions.hide
import orllewin.extensions.mainThread
import orllewin.extensions.show
import orllewin.file_io.SafIO
import org.json.JSONArray
import org.json.JSONObject
import orllewin.filter.R
import orllewin.filter.databinding.ActivityFilterBinding

class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding
    private lateinit var filters: Filters
    private lateinit var adapter: FilterAdapter
    private val safIO = SafIO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        safIO.registerForFileCreation(this)
        safIO.registerForFileOpen(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        binding.filtersRecycler.layoutManager = LinearLayoutManager(this)
        adapter = FilterAdapter({ filter ->
            FilterDialog(this, FilterDialog.mode_edit, filter.filterUrl, filter.filterType){ _, _ ->
                refresh()
            }.show()
        }, { view: View, filter: Filter ->
            showFilterOverflow(view, filter)
        })
        binding.filtersRecycler.adapter = adapter

        binding.addFilterFab.setOnClickListener { view ->
            FilterDialog(this, FilterDialog.mode_new, null, null){ _, _ ->
                refresh()
            }.show()
        }

        filters = Filters(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun showFilterOverflow(view: View, filter: Filter){
        //onOverflow
        val filterOverflow = PopupMenu(this, view)

        filterOverflow.inflate(R.menu.menu_filter)

        filterOverflow.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_filter_delete -> delete(filter)
            }
            true
        }

        filterOverflow.show()
    }

    private fun refresh(){
        filters.get { filters ->
            mainThread {
                when {
                    filters.isEmpty() -> {
                        binding.filtersRecycler.hide()
                        binding.empty.show()
                    }
                    else -> {
                        binding.filtersRecycler.show()
                        adapter.update(filters)
                        binding.empty.hide()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filters, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_filters_import -> importFilters()
            R.id.menu_filters_export -> exportFilters()
            else -> onBackPressed()
        }

        return true
    }

    fun importFilters(){
        safIO.chooseFile("application/json"){ uri ->
            safIO.readFile(this, uri){ content ->
                val filterJson = JSONObject(content)
                val filtersJsonArray = filterJson.getJSONArray("filters")
                val filtersEntries = arrayListOf<Filter>()

                var skipped = 0
                var added = 0

                repeat(filtersJsonArray.length()){ index ->
                    val filter = filtersJsonArray.getJSONObject(index)
                    val filterUrl = filter.getString("url")
                    val filterType = filter.getInt("type")

                    println("Importing filter: $filterUrl : $uri")
                    val existing = adapter.filters.filter {  entry ->
                        entry.filterUrl == filterUrl
                    }
                    when {
                        existing.isNotEmpty() -> skipped++
                        else -> {
                            added++
                            filtersEntries.add(Filter(filterUrl, filterType))
                        }
                    }
                }

                filters.addAll(filtersEntries){
                    filters.get { filters ->
                        mainThread{
                            binding.empty.hide()
                            adapter.update(filters)
                            when {
                                skipped > 0 -> {
                                    Toast.makeText(this, "$added filters imported ($skipped duplicates)", Toast.LENGTH_SHORT).show()
                                }
                                else -> Toast.makeText(this, "$added filters imported", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
            }
        }
    }

    private fun exportFilters(){
        safIO.newFile("ariane_filters.json", "application/json"){ uri ->
            filters.get { filters ->
                val json = JSONObject()
                val filtersJson = JSONArray()

                filters.forEach { entry ->
                    val filterJson = JSONObject()
                    filterJson.put("url", entry.filterUrl)
                    filterJson.put("type", entry.filterType)
                    filtersJson.put(filterJson)
                }

                json.put("filters", filtersJson)

                safIO.saveTextToFile(this, uri, json.toString(2))

                Snackbar.make(binding.root, "Filters exported", Snackbar.LENGTH_LONG)
                    .setAction("Share") {
                        safIO.shareFile(this, uri, "application/json")
                    }
                    .show()
            }
        }
    }

    /**
     *
     * Filter isn't actually deleted from the DB until the SnackBar disappears. Which is nice.
     *
     */
    @SuppressLint("ShowToast")
    private fun delete(filter: Filter){
        //OnDelete
        adapter.hide(filter)
        Snackbar.make(binding.root, "Deleted ${filter.filterUrl}", Snackbar.LENGTH_SHORT).addCallback(
            object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) = when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION -> adapter.show(filter)
                    else -> filters.delete(filter) {
                        mainThread {
                            adapter.remove(filter)
                            refresh()
                        }
                    }
                }
            }).setAction("Undo"){
            //Action listener unused
        }.show()
    }
}