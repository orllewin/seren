package orllewin.gemini

import android.net.Uri

data class UriHandler constructor(private var uri: String) {

    var host: String = ""

    init {
        extractHost()
    }

    fun update(uri: String){
        this.uri = uri
        extractHost()
    }

    fun resolve(reference: String) {
        if(uri == "gemini://$host") uri = "$uri/"
        when {
            reference.startsWith("./") -> {
                removeInDirectoryReferences()
                update("$uri${reference.substring(2)}")
            }
            reference.startsWith("//") -> update("gemini:$reference")
            reference.startsWith("gemini://") -> update(reference)
            reference.startsWith("/") -> uri = "gemini://$host$reference"
            reference.startsWith("../") -> {
                removeInDirectoryReferences()
                val traversalCount = reference.split("../").size - 1
                uri = traverse(traversalCount) + reference.replace("../", "")
            }
            else -> {
                uri = when {
                    uri.endsWith("/") -> "${uri}$reference"
                    else -> "${uri.substring(0, uri.lastIndexOf("/"))}/$reference"
                }
            }
        }
    }

    private fun traverse(count: Int): String{
        val path = uri.removePrefix("gemini://$host")
        val segments  = path.split("/").filter { it.isNotEmpty() }
        val segmentCount = segments.size
        var nouri = "gemini://$host"

        segments.forEachIndexed{ index, segment ->
            if(index < segmentCount - count){
                nouri += "/$segment"
            }
        }

        return "$nouri/"

    }

    private fun removeInDirectoryReferences(){
        when {
            !uri.endsWith("/") -> uri = uri.substring(0, uri.lastIndexOf("/") + 1)
        }
    }

    private fun extractHost(){
        if(uri.isEmpty()) return
        val urn = uri.removePrefix("gemini://")
        host = when {
            urn.contains("/") -> urn.substring(0, urn.indexOf("/"))
            else -> urn
        }
    }

    fun copy(): UriHandler = UriHandler(uri)

    override fun toString(): String = uri

    fun toUri(): Uri = Uri.parse(uri)


    companion object{
        /**
         * When user types in the address edit try and infer what they want to do:
         * 1 - a gemini address including scheme
         * 2 - a gemini address without scheme (if this fails prompt to search elsewhere)
         * 3 - a search term
         */
        fun inferAction(input: String, onInferred: (String) -> Unit) = when {
            input.startsWith("gemini://") -> onInferred(input)
            input.contains(".") -> onInferred("gemini://$input")
            else -> onInferred("$DEFAULT_SEARCH_URI$input")//todo - user may want to override search provider
        }

        private const val DEFAULT_SEARCH_URI = "gemini://geminispace.info/search?"
    }
}