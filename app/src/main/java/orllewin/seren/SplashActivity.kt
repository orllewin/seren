package orllewin.seren

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import orllewin.extensions.delayed
import orllewin.extensions.mainThread
import orllewin.gemini.Gemini
import orllewin.gemini.Response
import orllewin.seren.databinding.ActivitySplashBinding
import orllewin.settings.Settings
import kotlin.concurrent.thread

class SplashActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var skiss: SplashSkiss

    private var response: Response.Gemtext? = null

    private var minimumDisplayTimeElapsed = false
    private var networkResponded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Settings.initialise(this)

        if(Settings.bypassSplash()){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val backgroundColour = Settings.backgroundColour(this)
        val homeColour = Settings.homeColour()

        skiss = SplashSkiss(
            binding.skissView,
            backgroundColour,
            homeColour
        )

        delayed(2150){
            minimumDisplayTimeElapsed = true
            proceed()
        }

        binding.versionLabel.setTextColor(homeColour)
        binding.versionLabel.text = BuildConfig.VERSION_NAME

        skiss.start()

        thread {
            Gemini.request(Settings.homeCapsule(this)) { response ->
                if(response is Response.Gemtext) this.response = response
                networkResponded = true
                proceed()
            }
        }
    }

    private fun proceed() {
        if(!networkResponded || !minimumDisplayTimeElapsed) return

        mainThread {
            skiss.stop()

            Intent(this, MainActivity::class.java).run {
                response?.let {
                    putExtra("address", response!!.request?.uri.toString())
                    putExtra("gemtext", response!!.lines.joinToString("\n"))
                }
                startActivity(this)
                finish()
            }
        }
    }
}