package orllewin.seren

import android.app.Application
import orllewin.gemini.Gemini
import orllewin.identities.db.Identities
import orllewin.logger.Logger
import orllewin.settings.Settings

class Seren : Application() {

    override fun onCreate() {
        super.onCreate()

        Settings.initialise(this)

        Identities(this).get { identities ->
            Gemini.updateIdentities(identities)
        }

        Logger.initialise()
        Logger.logWithTime("Starting Seren...")
    }
}