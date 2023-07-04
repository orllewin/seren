package orllewin.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import androidx.preference.PreferenceManager
import orllewin.logger.Logger

object Settings {

    const val DEFAULT_HOME_CAPSULE = "gemini://seren_local"

    const val DEFAULT_ACCENT_COLOUR = "#F65E5E"

    lateinit var prefs: SharedPreferences

    fun initialise(context: Context){
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun backgroundColour(context: Context): Int{
        when (val backgroundPref = prefs.getString(context.getString(R.string.prefs_background_colour), null)) {
            null -> return defaultBackgroundColor(context)
            else -> {
                return try {
                    val longColour = java.lang.Long.parseLong(backgroundPref.substring(1), 16)
                    Color.parseColor(backgroundPref)
                }catch (nfe: NumberFormatException){
                    Logger.log("Error parsing background color: $backgroundPref - returning default instead")
                    defaultBackgroundColor(context)
                }
            }
        }
    }

    private fun defaultBackgroundColor(context: Context): Int{
        return when((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_NO -> Color.WHITE
            Configuration.UI_MODE_NIGHT_YES -> Color.BLACK
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Color.WHITE
            else -> Color.WHITE
        }
    }

    fun homeCapsule(context: Context): String {
        if(prefs == null) initialise(context)
        return prefs.getString("home_capsule", DEFAULT_HOME_CAPSULE) ?: DEFAULT_HOME_CAPSULE
    }
    fun homeColour(): Int = Color.parseColor(prefs.getString("home_icon_colour", DEFAULT_ACCENT_COLOUR))
    fun colourLargeHeaders(): Boolean = prefs.getBoolean("h1_accent_coloured", true)
    fun remapBoldUnicode(): Boolean = prefs.getBoolean("remap_bold_unicode", true)
    fun hideAsciiArt(): Boolean = prefs.getBoolean("hide_ascii_art", false)
    fun hideEmoji(): Boolean = prefs.getBoolean("remove_emoji", false)
    fun bypassSplash(): Boolean = prefs.getBoolean("bypass_splash_screen", false)
}