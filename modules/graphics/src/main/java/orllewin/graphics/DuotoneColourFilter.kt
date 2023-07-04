package orllewin.graphics

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.annotation.ColorInt

object DuotoneColourFilter {

    fun get(@ColorInt colorBlack: Int, @ColorInt colorWhite: Int, contrast: Float): ColorFilter{
        val cm = ColorMatrix()

        val cmBlackWhite = ColorMatrix()
        val lumR = 0.2125f
        val lumG = 0.7154f
        val lumB = 0.0721f
        val blackWhiteArray = floatArrayOf(
            lumR, lumG, lumB, 0f, 0f,
            lumR, lumG, lumB, 0f, 0f,
            lumR, lumG, lumB, 0f, 0f, 0f, 0f, 0f, 1f, 0f
        )
        cmBlackWhite.set(blackWhiteArray)

        val cmContrast = ColorMatrix()
        val scale = contrast + 1.0f
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastArray = floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        cmContrast.set(contrastArray)

        val cmDuoTone = ColorMatrix()
        val r1: Float = Color.red(colorWhite).toFloat()
        val g1: Float = Color.green(colorWhite).toFloat()
        val b1: Float = Color.blue(colorWhite).toFloat()
        val r2: Float = Color.red(colorBlack).toFloat()
        val g2: Float = Color.green(colorBlack).toFloat()
        val b2: Float = Color.blue(colorBlack).toFloat()
        val r1r2 = (r1 - r2) / 255f
        val g1g2 = (g1 - g2) / 255f
        val b1b2 = (b1 - b2) / 255f
        val duoToneArray = floatArrayOf(
            r1r2, 0f, 0f, 0f, r2,
            g1g2, 0f, 0f, 0f, g2,
            b1b2, 0f, 0f, 0f, b2, 0f, 0f, 0f, 1f, 0f
        )
        cmDuoTone.set(duoToneArray)

        cm.postConcat(cmBlackWhite)
        cm.postConcat(cmContrast)
        cm.postConcat(cmDuoTone)

        return ColorMatrixColorFilter(cm)
    }
}