package oppen.gemini.browser

import android.net.Uri
import orllewin.gemini.Response

sealed class UIState{
    data class GeminiResponse(val address: String, val response: Response): UIState()
    object GoHome: UIState()
    object ShowLog: UIState()
    object MainMenu: UIState()
    object GoBookmarks: UIState()
    object GoHistory: UIState()
    object GoIdentity: UIState()
    data class Binary(val address: String): UIState()
    object ResetState: UIState()
}
