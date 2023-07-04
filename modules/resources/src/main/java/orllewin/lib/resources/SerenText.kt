package orllewin.lib.resources

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.widget.AppCompatTextView
import orllewin.resources.R

class SerenText(val context: Context) {

    companion object{
        val DEFAULT = "default"
    }

    fun applyTypefaceRegular(view: AppCompatTextView, typeface: String){
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                when (typeface) {
                    DEFAULT -> view.typeface = Typeface.DEFAULT
                    "google_sans" -> apply(view, R.font.google_sans_text_regular)
                    "inter_regular" -> apply(view, R.font.inter_regular)
                    "league_gothic_italic" -> apply(view, R.font.league_gothic_italic)
                }
            }
            else -> view.typeface = Typeface.DEFAULT
        }
    }
    fun applyTypefaceHeader(view: AppCompatTextView, typeface: String){
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                when (typeface) {
                    DEFAULT -> view.typeface = Typeface.DEFAULT
                    "google_sans" -> apply(view, R.font.google_sans_text_medium)
                    "inter_black" -> apply(view, R.font.inter_black)
                    "league_gothic_italic" -> {
                        apply(view, R.font.league_gothic_italic)
                        //Android clips italic fonts so add unicode no-break space character to force correct rendering
                        view.append("\u00A0")
                    }

                }
            }
            else -> view.typeface = Typeface.DEFAULT
        }
    }

    private fun apply(view: AppCompatTextView, fontRes: Int){
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> view.typeface = context.resources.getFont(fontRes)
        }
    }

}