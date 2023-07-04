package orllewin.gemini.gemtext.adapter

import android.graphics.Color

data class AdapterConfig (
    val homeColor: Int,
    var colourLargeHeaders: Boolean,
    var remapBoldUnicodeChars: Boolean) {

    fun clone(config: AdapterConfig){
        this.remapBoldUnicodeChars = config.remapBoldUnicodeChars
    }

    class Builder {
        var homeColour: Int = Color.BLACK
        var colourLargeHeaders: Boolean = true
        var remapBoldUnicodeChars: Boolean = true

        fun homeColour(homeColour: Int) = apply { this.homeColour = homeColour }
        fun colourLargeHeaders(colourLargeHeaders: Boolean) = apply { this.colourLargeHeaders = colourLargeHeaders }
        fun remapBoldUnicodeChars(remapBoldUnicodeChars: Boolean) = apply { this.remapBoldUnicodeChars = remapBoldUnicodeChars }
        fun build() = AdapterConfig(
            homeColour,
            colourLargeHeaders,
            remapBoldUnicodeChars)
    }

    companion object{
        fun getDefault(): AdapterConfig{
            return Builder().build()
        }
    }
}