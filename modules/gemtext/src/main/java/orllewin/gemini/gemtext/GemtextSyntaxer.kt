package orllewin.gemini.gemtext

import android.graphics.Color
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.core.text.getSpans

class GemtextSyntaxer {

    private val lineHashes = HashMap<Int, Int>()

    private val COLOUR_LINK = Color.rgb(57, 122, 183)
    private val COLOUR_HEADER = Color.rgb(200, 100, 100)
    private val COLOUR_LIST = Color.rgb(41, 134, 50)
    private val COLOUR_CODE = Color.rgb(164, 122, 60)

    fun clearhashes(){
        lineHashes.clear()
    }

    fun process(spannable: Spannable?) {
        if (spannable == null) return

        var start = 0
        var end: Int
        var inCode = false

        spannable.lines().forEachIndexed{ index, line ->

            end = start + line.length + 1

            val lineHash = line.hashCode()

            //Only update the span if the line has changed
            if(lineHashes[index] != lineHash) {
                //Remove existing span
                  spannable.getSpans<ForegroundColorSpan>(start, end-1).run{
                      this.forEach { span ->
                          spannable.removeSpan(span)
                      }
                  }

                //Apply new span
                when {
                    line.startsWith("```") -> {
                        setColour(spannable, start, end - 1, COLOUR_CODE)
                        inCode = !inCode
                    }
                    inCode -> setColour(spannable, start, end - 1, COLOUR_CODE)
                    line.startsWith("#") -> setColour(spannable, start, end - 1, COLOUR_HEADER)
                    line.startsWith("=>") -> setColour(spannable, start, end - 1, COLOUR_LINK)
                    line.startsWith("*") -> setColour(spannable, start, end - 1, COLOUR_LIST)
                }
            }

            lineHashes[index] = lineHash

            start = end
        }
    }

    private fun setColour(spannable: Spannable, start: Int, end: Int, @ColorInt colour: Int){
        spannable.setSpan(ForegroundColorSpan(colour), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}