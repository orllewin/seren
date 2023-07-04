package orllewin.history.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import orllewin.extensions.mainThread
import orllewin.extensions.show
import orllewin.history.R
import orllewin.history.databinding.ActivityHistoryBinding
import orllewin.history.db.History


class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        binding.historyRecycler.layoutManager = LinearLayoutManager(this)

        History(this).get{ entries ->
            if(entries.isEmpty()){
                binding.empty.show()
            }else {
                HistoryAdapter(entries) { entry ->
                    Intent().run {
                        putExtra("address", entry.uri.toString())
                        setResult(RESULT_OK, this)
                        finish()
                    }
                }.run {
                    mainThread {
                        binding.historyRecycler.adapter = this
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }
}