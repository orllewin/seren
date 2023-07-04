package oppen.gemini.browser

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import orllewin.extensions.hideKeyboard
import orllewin.gemini.UriHandler

class AddressEditorActionListener(
    private val editText: EditText,
    val onNavigate: (String) -> Unit): TextView.OnEditorActionListener {

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return when (actionId) {
            EditorInfo.IME_ACTION_GO -> {
                editText.hideKeyboard()
                editText.clearFocus()
                UriHandler.inferAction(editText.text.toString().trim()){ address ->
                    onNavigate(address)
                }
                true
            }
            else -> false
        }
    }
}