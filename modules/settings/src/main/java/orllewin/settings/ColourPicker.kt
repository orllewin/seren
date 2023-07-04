package orllewin.settings

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider

class ColourPicker(context: Context, colourStr: String?, onColour: (colour: String) -> Unit) {

    private val dialog: AlertDialog

    private var red: Int = 0
    private var green: Int = 0
    private var blue: Int = 0

    private var preview: View? = null

    init {
        val view = View.inflate(context, R.layout.colour_picker, null)
        dialog = MaterialAlertDialogBuilder(context, orllewin.resources.R.style.modern_material_dialog)
            .setView(view)
            .setPositiveButton("Apply"){ _, _ ->
                val pickedColour = Color.rgb(red, green, blue)
                onColour(java.lang.String.format("#%06X", 0xFFFFFF and pickedColour))
            }
            .setNegativeButton("Cancel"){ _, _ ->
                //NOOP
            }
            .create()

        preview = view.findViewById(R.id.preview)
        val redSlider = view.findViewById<Slider>(R.id.red_slider)
        val greenSlider = view.findViewById<Slider>(R.id.green_slider)
        val blueSlider = view.findViewById<Slider>(R.id.blue_slider)

        val colour = Color.parseColor(colourStr)

        red = Color.red(colour)
        green = Color.green(colour)
        blue = Color.blue(colour)

        update()

        redSlider.value = red.toFloat()
        greenSlider.value = green.toFloat()
        blueSlider.value = blue.toFloat()

        redSlider.addOnChangeListener { _, value, _ ->
            red = value.toInt()
            update()
        }

        greenSlider.addOnChangeListener { _, value, _ ->
            green = value.toInt()
            update()
        }

        blueSlider.addOnChangeListener { _, value, _ ->
            blue = value.toInt()
            update()
        }
    }

    private fun update(){
        preview?.setBackgroundColor(Color.rgb(red, green, blue))
    }

    fun show() = dialog.show()
}