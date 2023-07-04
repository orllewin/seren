package orllewin.gemini.gemtext.editor

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import orllewin.gemini.gemtext.GemtextSyntaxer

class EditGemtext @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatEditText(context, attrs) {

    init {
        val syntaxer = GemtextSyntaxer()
        addTextChangedListener(EditWatcher { editable ->
            syntaxer.process(editable)
        })
    }

    fun insertNewLine(insert: String){
        when {
            isLineStart() -> insert(insert)
            else -> insert("\n$insert")
        }
    }

    fun insert(insert: String){
        text?.insert(selectionStart, insert)
    }

    fun addLink() = insertNewLine("=> ")
    fun addGeminiLink() = insertNewLine("=> gemini://")
    fun addListItem() = insertNewLine("* ")

    fun addCode(){
        if(isLineStart()){
            val cursor = selectionStart
            text?.insert(selectionStart, "```\n\n```")
            setSelection(cursor + 4)
        }else{
            val cursor = selectionStart
            text?.insert(selectionStart, "\n```\n\n```")
            setSelection(cursor + 5)
        }

    }

    fun addHash(){
        when {
            selectionStart > 1 -> {
                when {
                    text?.substring(selectionStart-2, selectionStart) == "# " -> {
                        text?.replace(selectionStart-1, selectionStart, "#")
                        text?.insert(selectionStart, " ")
                    }
                    else -> insertNewLine("# ")
                }
            }
            else -> text?.insert(selectionStart, "# ")
        }
    }

    private fun isLineStart(): Boolean {
        return when (selectionStart) {
            0 -> true
            else -> text?.get(selectionStart -1) == '\n'
        }
    }

    fun string(): String = text.toString()
}