package orllewin.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import orllewin.filter.ui.FilterActivity
import orllewin.gemini.editor.HomedEditorActivity

class PreferenceActivity : AppCompatActivity() {

    private var gemtext: String? = null

    companion object{
        fun createIntent(context: Context, gemtext: String?): Intent{
            return Intent(context, PreferenceActivity::class.java).apply{
                putExtra("gemtext", gemtext ?: "")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        gemtext = intent.getStringExtra("gemtext")

        if (savedInstanceState == null) {

            val bundle = Bundle()
            bundle.putString("gemtext", gemtext)

            val settingsFragment = SettingsFragment()
            settingsFragment.arguments = bundle

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()
        }



        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.vector_close)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

        private var duotoneColourPreference: ColourPreference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
       }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            //General SECTION

            val homeCapsuleEditorPreference = findPreference<Preference>("home_capsule_editor")
            homeCapsuleEditorPreference?.setOnPreferenceClickListener {
                startActivity(HomedEditorActivity.createIntent(requireContext(), arguments?.getString("gemtext", "") ?: ""))
                true
            }
            val homeModeSwitch = findPreference<SwitchPreferenceCompat>("home_mode")
            homeModeSwitch?.setOnPreferenceChangeListener { _, newValue ->
                println("Home mode switch new value: $newValue")
                val isRemote = newValue as Boolean
                homeCapsuleEditorPreference?.isEnabled = !isRemote

                true
            }

            //Wellbeing section
            val filterPreference = findPreference<Preference>(getString(R.string.prefs_filter))
            filterPreference?.let{ filter ->
                filter.setOnPreferenceClickListener {  _ ->
                    startActivity(Intent(requireContext(), FilterActivity::class.java))
                    true
                }
            }

            //THEME SECTION
            val homeIconColourPreference = findPreference<ColourPreference>(getString(R.string.prefs_home_icon_colour))

            when (val homeIconColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_home_icon_colour), null)) {
                null -> homeIconColourPreference?.summary = getString(R.string.defaultt)
                else -> homeIconColourPreference?.summary = homeIconColour
            }

            homeIconColourPreference?.setOnBindListener {
                val homeIconColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_home_icon_colour), null)
                homeIconColourPreference.setColour(homeIconColour)
            }
            homeIconColourPreference?.setOnPreferenceClickListener { _ ->
                val homeIconColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_home_icon_colour), Settings.DEFAULT_ACCENT_COLOUR)

                ColourPicker(requireContext(), homeIconColour){ colour ->
                    homeIconColourPreference.setColour(colour)
                    homeIconColourPreference.summary = colour
                    preferenceManager.sharedPreferences.edit().putString(getString(R.string.prefs_home_icon_colour), colour).apply()
                }.show()
                true
            }

            val backgroundPreference = findPreference<ColourPreference>(getString(R.string.prefs_background_colour))

            when (val backgroundColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_background_colour), null)) {
                null -> backgroundPreference?.summary = getString(R.string.defaultt)
                else -> backgroundPreference?.summary = backgroundColour
            }

            backgroundPreference?.setOnBindListener {
                val backgroundColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_background_colour), null)
                backgroundPreference.setColour(backgroundColour)
            }
            backgroundPreference?.setOnPreferenceClickListener { _ ->
                val backgroundColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_background_colour), "#f9dede")

                ColourPicker(requireContext(), backgroundColour){ colour ->
                    backgroundPreference.summary = colour
                    backgroundPreference.setColour(colour)
                    preferenceManager.sharedPreferences.edit().putString(getString(R.string.prefs_background_colour), colour).apply()
                }.show()
                true
            }

            val themeTitleTypefacePreference = findPreference<ListPreference>(getString(R.string.prefs_sans_headers_3))
            setFontSummary(themeTitleTypefacePreference!!.value as String, themeTitleTypefacePreference)
            themeTitleTypefacePreference.setOnPreferenceChangeListener { _, newValue ->
                setFontSummary(newValue as String, themeTitleTypefacePreference)
                true
            }

            val themeContentTypefacePreference = findPreference<ListPreference>(getString(R.string.prefs_sans_content_3))
            setFontSummary(themeContentTypefacePreference!!.value as String, themeContentTypefacePreference)
            themeContentTypefacePreference.setOnPreferenceChangeListener { _, newValue ->
                setFontSummary(newValue as String, themeContentTypefacePreference)
                true
            }

            //ACCESSIBILITY SECTION
            val buttonColourPreference = findPreference<ColourPreference>(getString(R.string.prefs_button_colour))

            when (val buttonColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_button_colour), "#f9dede")) {
                null -> buttonColourPreference?.summary = getString(R.string.defaultt)
                else -> buttonColourPreference?.summary = buttonColour
            }

            buttonColourPreference?.setOnBindListener {
                val buttonColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_button_colour), "#f9dede")
                buttonColourPreference.setColour(buttonColour)
            }
            buttonColourPreference?.setOnPreferenceClickListener { _ ->
                val buttonColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_button_colour), "#f9dede")

                ColourPicker(requireContext(), buttonColour){ colour ->
                    buttonColourPreference.summary = colour
                    buttonColourPreference.setColour(colour)
                    preferenceManager.sharedPreferences.edit().putString(getString(R.string.prefs_button_colour), colour).apply()
                }.show()
                true
            }

            //Theme section
            duotoneColourPreference = findPreference(getString(R.string.prefs_duotone_colour))
            duotoneColourPreference?.setOnBindListener {
                val duotoneImageColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_duotone_colour), "#1d1d1d")
                duotoneColourPreference?.setColour(duotoneImageColour)
            }
            duotoneColourPreference?.setOnPreferenceClickListener { _ ->
                val buttonColour = preferenceManager.sharedPreferences.getString(getString(R.string.prefs_duotone_colour), "#1d1d1d")

                ColourPicker(requireContext(), buttonColour){ colour ->
                    duotoneColourPreference?.summary = colour
                    duotoneColourPreference?.setColour(colour)
                    preferenceManager.sharedPreferences.edit().putString(getString(R.string.prefs_duotone_colour), colour).apply()
                }.show()
                true
            }
            val imageModePreference = findPreference<ListPreference>(getString(R.string.prefs_experimental_image_mode))
            updateImageMode(imageModePreference, imageModePreference?.value)
            imageModePreference?.setOnPreferenceChangeListener { _, newValue ->
                updateImageMode(imageModePreference, newValue)
                true
            }
        }

        private fun updateImageMode(imageModePreference: ListPreference?, value: Any?){
            if(imageModePreference == null || value == null) return
            when (value as String) {
                "none" -> imageModePreference.summary = getString(R.string.none)
                "monochrome" -> imageModePreference.summary = getString(R.string.monochrome)
                "duotone" -> imageModePreference.summary = getString(R.string.duotone)
                else -> imageModePreference.summary = value as CharSequence?
            }

            when (value) {
                "duotone" -> duotoneColourPreference?.isEnabled = true
                else -> duotoneColourPreference?.isEnabled = false
            }
        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            if(preference == null) return false

            if(preference.key.startsWith("prefs_theme_")){
                when(preference.key){
                    "prefs_theme_system" -> {
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_day")?.isChecked = false
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_night")?.isChecked = false
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    "prefs_theme_day" -> {
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_system")?.isChecked = false
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_night")?.isChecked = false
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    "prefs_theme_night" -> {
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_system")?.isChecked = false
                        preferenceScreen.findPreference<CheckBoxPreference>("prefs_theme_day")?.isChecked = false
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                return true
            }

            return false
        }

        private fun setFontSummary(value: String, preference: Preference){
            preference.summary = when (value) {
                "default" -> getString(R.string.defaultt)
                "google_sans" -> getString(R.string.typeface_google_sans)
                "inter_black" -> getString(R.string.typeface_inter_black)
                "inter_regular" -> getString(R.string.typeface_inter_regular)
                "league_gothic_italic" -> getString(R.string.typeface_league_gothic_italic)
                else -> value
            }
        }
    }
}