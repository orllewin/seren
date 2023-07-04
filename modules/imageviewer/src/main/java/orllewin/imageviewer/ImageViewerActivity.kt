package orllewin.imageviewer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import orllewin.imageviewer.databinding.ActivityImageViewerBinding

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            return Intent(context, ImageViewerActivity::class.java).also{ intent ->
                intent.putExtra("imageUri", uri.toString())
            }
        }
    }

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)


        intent?.run{
            val uri = Uri.parse(getStringExtra("imageUri"))
            binding.touchImageView.setImageURI(uri)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}