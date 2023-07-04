package orllewin.gemini.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import orllewin.gemini.editor.databinding.ActivityHomeEditorBinding

class HomedEditorActivity : AppCompatActivity() {

    companion object{
        fun createIntent(context: Context, gemtext: String): Intent {
            return Intent(context, HomedEditorActivity::class.java).apply {
                putExtra("gemtext", gemtext)
            }
        }
    }

    lateinit var binding: ActivityHomeEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =  ActivityHomeEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)

        binding.editGemtext.setText(intent.getStringExtra("gemtext"))

        binding.insertHashButton.setOnClickListener { binding.editGemtext.addHash() }
        binding.insertCodeButton.setOnClickListener { binding.editGemtext.addCode() }
        binding.insertListItemButton.setOnClickListener { binding.editGemtext.addListItem() }
        binding.insertLinkButton.setOnClickListener { binding.editGemtext.addLink() }
        binding.insertLinkButton.setOnLongClickListener { binding.editGemtext.addGeminiLink(); true }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}