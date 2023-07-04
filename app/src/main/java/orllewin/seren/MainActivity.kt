package orllewin.seren

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import orllewin.browser.BrowserFragment
import orllewin.seren.databinding.ActivityMainBinding
import orllewin.settings.Settings

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Settings.initialise(this)
    }

    override fun onStart() {
        super.onStart()

        when {
            intent.hasExtra("address") && intent.hasExtra("gemtext") -> {
                println("Seren startup: has address and gemtext")
                preload(intent.getStringExtra("address"), intent.getStringExtra("gemtext"))
                intent.removeExtra("address")
                intent.removeExtra("gemtext")
            }
            intent.data != null -> {
                val uri = intent.data
                println("Seren startup: has data: $uri")
                preload(uri.toString(), null)
                intent.data = null
            }
            else -> defaultStart()
        }
    }

    private fun defaultStart(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
            when (fragment) {
                is BrowserFragment -> fragment.defaultStart()
            }
        }
    }

    private fun preload(address: String?, gemtext: String?){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
            when (fragment) {
                is BrowserFragment -> fragment.preload(address, gemtext)
            }
        }
    }
}