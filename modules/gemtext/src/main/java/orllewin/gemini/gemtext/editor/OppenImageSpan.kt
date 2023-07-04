package orllewin.gemini.gemtext.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.text.style.ImageSpan

class OppenImageSpan(context: Context, uri: Uri, val lineWidth: Int): ImageSpan(context, uri) {

    init {
        //todo coroutine resize to cacheDir based on lineWidth
        //todo - add delete iconoverlay in corner to remove image
    }
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        super.draw(canvas, text, start, end, x, top, y, bottom, paint)
    }


}