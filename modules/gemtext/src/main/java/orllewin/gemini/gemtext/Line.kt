package orllewin.gemini.gemtext

sealed class Line(val line: String, val type: Int){
    class Regular(line: String): Line(line, TYPE_TEXT)
    class Code(line: String, val meta: String?): Line(line, TYPE_CODE)
    class Link(line: String, val url: String, val description: String?): Line(line, TYPE_LINK)
    class ImageLink(line: String, val url: String, val description: String?): Line(line, TYPE_IMAGE_LINK)
    class HeaderSmall(line: String, val content: String): Line(line, TYPE_SMALL_HEADER)
    class HeaderMedium(line: String, val content: String): Line(line, TYPE_MEDIUM_HEADER)
    class HeaderBig(line: String, val content: String): Line(line, TYPE_BIG_HEADER)
    class ListItem(line: String, val content: String): Line(line, TYPE_LIST_ITEM)
    class Quote(line: String, val content: String): Line(line, TYPE_QUOTE)

    companion object{
        const val TYPE_TEXT = 0
        const val TYPE_BIG_HEADER = 1
        const val TYPE_MEDIUM_HEADER = 2
        const val TYPE_SMALL_HEADER = 3
        const val TYPE_LIST_ITEM = 4
        const val TYPE_IMAGE_LINK = 5
        const val TYPE_LINK = 6
        const val TYPE_CODE = 7
        const val TYPE_QUOTE = 8
    }
}
