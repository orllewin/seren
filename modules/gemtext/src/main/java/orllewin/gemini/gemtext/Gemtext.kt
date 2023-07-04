package orllewin.gemini.gemtext

import java.util.*
import kotlin.math.min

object Gemtext {

    fun findTitle(lines: List<String>): String? {
        var title: String? = null
        val range = min(lines.size, 5)
        loop@ for (index in 0 until range) {
            val line = lines[index]
            if(line.startsWith("#")){
                title = line.substring(line.indexOf(" "), line.length)
                break
            }
        }

        return when (title) {
            null -> null
            else -> title.trim()
        }
    }

    fun process(source: List<String>, removeAsciiArt: Boolean, removeEmoji: Boolean): List<String>{
        return when {
            removeEmoji -> {
                val emojiless = removeEmoji(source)
                findCodeBlocks(emojiless, removeAsciiArt)
            }
            else -> findCodeBlocks(source, removeAsciiArt)
        }
    }

    fun removeEmoji(source: List<String>): List<String>{
        val emojiless = mutableListOf<String>()
        source.forEach { line ->
            val filtered: IntArray = line.codePoints().filter { codePoint ->
                val isEmoji = when (Character.UnicodeBlock.of(codePoint)) {
                    null, Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS -> true
                    else -> false
                }

                !isEmoji
            }.toArray()
            emojiless.add(String(filtered, 0, filtered.size))
        }
        return emojiless

    }

    fun removeBoldUnicode(source: List<String>){

    }

    /**
     *
     * This is safe for most cases but fails when a line starts with ``` _within_ a code block
     *
     */
    fun findCodeBlocks(source: List<String>, removeAsciiArt: Boolean): List<String>{
        val sb = StringBuilder()
        var inCodeBlock = false
        val parsed = mutableListOf<String>()
        var metaContainsASCIIArt = false
        source.forEach { line ->
            if (line.startsWith("```")) {
                if (!inCodeBlock) {

                    metaContainsASCIIArt = line.lowercase(Locale.getDefault()).contains("ascii")

                    //New code block starting
                    sb.clear()
                    sb.append("```")

                    if(line.length > 3){
                        //Code block has alt text
                        val alt = line.substring(3)
                        sb.append("<|ALT|>$alt</|ALT>")
                    }
                } else {
                    //End of code block
                    val codeBlock = sb.toString()
                    when {
                        removeAsciiArt -> {
                            if(!isArt(codeBlock) && !metaContainsASCIIArt) parsed.add(codeBlock)
                        }
                        else -> parsed.add(codeBlock)
                    }
                    metaContainsASCIIArt = false
                }
                inCodeBlock = !inCodeBlock
            } else {
                if (inCodeBlock) {
                    sb.append("$line\n")
                } else {
                    parsed.add(line)
                }
            }
        }

        return parsed
    }

    private fun isArt(source: String, checkCommonASCIIBlocks: Boolean = false): Boolean{

        val regex = "([^)\\s])\\1{4,}".toRegex()
        val hasConsecutiveChars = regex.containsMatchIn(source)
        if(hasConsecutiveChars) return true

        if(checkCommonASCIIBlocks) {
            val hasArtSymbols = !source.none { it in arrayOf('█', '▄', '╝', '░', '┐', '├') }
            if (hasArtSymbols) return true
        }

        //Check for only alphanumeric, no syntax
        val alphanumeric = source.filter { it.isLetterOrDigit() }.length == source.length
        if(alphanumeric) return true

        //Check for only symbols, eg. --__/\__--
        val alphanumericCount = source.filter { it.isLetterOrDigit() }.length
        if(alphanumericCount == 0) return true

        //If 99% of source is symbols
        if(source.length - alphanumericCount > source.length * 99 * 0.01) return true

        //If 99% of source is alphanumeric
        val symbolCount = source.filter { !it.isLetterOrDigit() }.length
        if(symbolCount < source.length * 1 * 0.01) return true

        return false
    }
}