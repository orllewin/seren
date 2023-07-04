package orllewin.gemini.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import orllewin.gemini.gemtext.GemtextSyntaxer
import orllewin.viewer.R
import orllewin.viewer.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {

    companion object{

        const val TITLE = "oppen.gemini.viewer.TITLE"
        const val ADDRESS = "oppen.gemini.viewer.ADDRESS"
        const val CONTENT = "oppen.gemini.viewer.CONTENT"

        fun createIntent(context: Context, title: String, address: String, content: String): Intent{
            return Intent(context, ViewerActivity::class.java).run{
                putExtra(TITLE, title)
                putExtra(ADDRESS, address)
                putExtra(CONTENT, content)
            }
        }
    }

    private lateinit var binding: ActivityViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        intent?.run {
            supportActionBar?.title = getStringExtra(TITLE)
            supportActionBar?.subtitle = getStringExtra(ADDRESS)

            val content = SpannableString(getStringExtra(CONTENT))

            GemtextSyntaxer().run {
                process(content)
            }

            binding.sourceTextView.text = content
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}