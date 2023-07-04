package orllewin.browser

import android.view.KeyEvent

//https://support.mozilla.org/en-US/kb/keyboard-shortcuts-perform-firefox-tasks-quickly
object KeyHelper {

    fun isQuit(event: KeyEvent, keyCode: Int): Boolean =
        event.isCtrlPressed && (event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_Q)

    fun isEnter(event: KeyEvent, keyCode: Int): Boolean =
        (event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)

    fun isFocusAddress(event: KeyEvent, keyCode: Int): Boolean =
        event.isCtrlPressed && event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_L)

    fun isNewAddress(event: KeyEvent, keyCode: Int): Boolean =
        event.isCtrlPressed && event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_K)

    fun isViewSource(event: KeyEvent, keyCode: Int): Boolean =
        event.isCtrlPressed && event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_U)

    fun isReload(event: KeyEvent, keyCode: Int): Boolean = when {
        event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_R) && event.isCtrlPressed -> {
            true
        }
        event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_REFRESH) -> {
            true
        }
        else -> false
    }

}