package orllewin.gemini.gemtext.processor

import orllewin.extensions.endsWithImage
import orllewin.gemini.gemtext.Line
import org.apache.commons.lang.StringEscapeUtils

/**
 * Replacement class for 'Gemtext' - gemtext does operations in multiple passes which is wasteful
 * This class should do all process in a single pass where possible (which may mean 2 in actuality, code blocks and everything else)
 */
class Processor(
    private val remapBoldUnicode: Boolean,
    private val removeEmoji: Boolean,
    private val removeAsciiArt: Boolean
){
    companion object{
        const val CODE_TAG = "```"
        const val ASCII = "ascii"
        const val HEADER_SYMBOL = "#"
        const val HEADER_BIG = "#"
        const val HEADER_MEDIUM = "##"
        const val HEADER_SMALL = "###"
        const val LINK = "=>"
        const val LIST_ITEM = "*"
        const val QUOTE = ">"
    }

    private var inCodeBlock = false
    private var codeMetaContainsAscii = false
    private var codeMeta: String? = null
    private val codeBuilder = StringBuilder()

    private val processed = mutableListOf<Line>()

    fun process(source: List<String>, unescapeCharacters: Boolean, onProcessed: (lines: List<Line>) -> Unit){

        processed.clear()
        codeBuilder.clear()

        inCodeBlock = false
        codeMetaContainsAscii = false
        codeMeta = null

        source.forEach { sourceLine ->
            var line = when {
                inCodeBlock -> sourceLine
                removeEmoji -> removeEmoji(sourceLine)
                else -> sourceLine
            }
            when {
                remapBoldUnicode -> line = remapBoldUnicode(line, unescapeCharacters)
            }
            when {
                sourceLine.startsWith(CODE_TAG) -> handleCodeTag(line)
                inCodeBlock -> codeBuilder.append("$sourceLine\n")
                else -> {
                    when {
                        sourceLine.startsWith(HEADER_SYMBOL) -> processHeader(line)
                        sourceLine.startsWith(LINK) -> processLink(line)
                        sourceLine.startsWith(LIST_ITEM) -> processListItem(line)
                        sourceLine.startsWith(QUOTE) -> processQuote(line)
                        else -> processed.add(Line.Regular(line))
                    }
                }
            }
        }

        onProcessed(processed)
    }

    private fun processListItem(sourceLine: String){
        val content = sourceLine.substring(1).trim()
        processed.add(Line.ListItem(sourceLine, content))
    }

    private fun processQuote(sourceLine: String){
        val content = sourceLine.substring(1).trim()
        processed.add(Line.Quote(sourceLine, content))
    }

    private fun processHeader(sourceLine: String){
        when {
            sourceLine.startsWith(HEADER_SMALL) -> processed.add(Line.HeaderSmall(sourceLine, sourceLine.substring(3).trim()))
            sourceLine.startsWith(HEADER_MEDIUM) -> processed.add(Line.HeaderMedium(sourceLine, sourceLine.substring(2).trim()))
            sourceLine.startsWith(HEADER_BIG) -> processed.add(Line.HeaderBig(sourceLine, sourceLine.substring(1).trim()))
        }
    }

    private fun processLink(sourceLine: String){
        val url = getLink(sourceLine)
        val isImageLink = url.endsWithImage()
        val description= sourceLine.substring(2).replace(url, "").trim()
        when {
            isImageLink -> processed.add(Line.ImageLink(sourceLine, url, description))
            else -> processed.add(Line.Link(sourceLine, url, description))
        }
    }

    private fun handleCodeTag(sourceLine: String){
        when {
            inCodeBlock -> {
                inCodeBlock = false
                val code = codeBuilder.toString()

                when {
                    !removeAsciiArt || codeMetaContainsAscii -> processed.add(Line.Code(code, codeMeta))
                    !isArt(code, true) -> processed.add(Line.Code(code, codeMeta))
                }
            }
            else -> {
                inCodeBlock = true
                codeBuilder.clear()
                when {
                    sourceLine.length > 3 -> {
                        //Code block has alt text
                        codeMetaContainsAscii = sourceLine.lowercase().contains(ASCII)
                        codeMeta = sourceLine.substring(3).trim()
                    }
                    else -> codeMetaContainsAscii = false
                }
            }
        }
    }

    private fun getLink(line: String): String{
        val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
        return linkParts[0]
    }

    //https://gist.github.com/fikr4n/1d8a3534b0e90940e9ea69a8b044730c
    /**
     * Characters in Java/Kotlin source need to be unescaped when running from unit tests
     */
    private fun remapBoldUnicode(line: String, unescapeCharacters: Boolean): String {

        val unicodeMapper = UnicodeMathematicalSymbolsMapper()

        val toProcess = when {
            unescapeCharacters -> StringEscapeUtils.unescapeJava(line)
            else -> line
        }
        val hasBoldUnicode = unicodeMapper.hasMathematicalAlphanumericSymbols(toProcess)

        return when {
            hasBoldUnicode -> unicodeMapper.remap(toProcess)
            else -> line
        }
    }

    private fun removeEmoji(line: String): String{
        val filteredChars = line.toCharArray().filter { char ->
            val isEmoji = Character.UnicodeScript.of(char.code).name.equals("UNKNOWN")
            !isEmoji
        }.toCharArray()
        return String(filteredChars)
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