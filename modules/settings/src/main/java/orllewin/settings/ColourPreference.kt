package orllewin.settings

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import orllewin.extensions.hide
import orllewin.extensions.show

class ColourPreference: Preference {

    var icon: ImageView? = null

    var bindListener: () -> Unit = {}

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        icon = holder?.itemView?.findViewById(R.id.colour_icon)
        icon?.setImageDrawable(ColorDrawable(Color.parseColor("#0000ff")))

        bindListener.invoke()
    }

    fun setOnBindListener(listener: () -> Unit){
        this.bindListener = listener
    }

    fun setColour(colour: String?){
        if(colour == null){
            icon?.hide()
        }else{
            ContextCompat.getDrawable(context, R.drawable.drawable_circle)?.run {
                setTint(Color.parseColor(colour))
                icon?.setImageDrawable(this)
                icon?.show()
            }
        }

    }
}