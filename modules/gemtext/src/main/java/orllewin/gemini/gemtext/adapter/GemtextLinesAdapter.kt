package orllewin.gemini.gemtext.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import orllewin.filter.db.Filter
import orllewin.gemini.gemtext.Line
import orllewin.gemini.UriHandler
import orllewin.gemtext.databinding.*
import orllewin.lib.resources.SerenText

class GemtextLinesAdapter(context: Context, val onLink: (link: Uri, longTap: Boolean, view: View, adapterPosition: Int) -> Unit): RecyclerView.Adapter<GemtextViewHolder>() {

    private var processedLines = mutableListOf<Line>()
    private val filters: MutableList<Filter> = mutableListOf()
    private val ouri = UriHandler("")
    private var inlineImageUris = HashMap<Int, Uri?>()
    private var imageLineCheckIndex = 0
    private var downloadLineIndex = 0
    private val config = AdapterConfig.getDefault()
    private val serenText = SerenText(context)

    private var headerTypeface = "default"
    private var contentTypeface = "default"

    override fun getItemCount(): Int = processedLines.size

    override fun getItemViewType(position: Int): Int = processedLines[position].type

    //todo - need to handle full-width links and image links, and code blocks
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GemtextViewHolder {
        val i = LayoutInflater.from(parent.context)
        return when(viewType){
            Line.TYPE_TEXT -> GemtextViewHolder.Text(GemtextTextBinding.inflate(i))
            Line.TYPE_BIG_HEADER -> GemtextViewHolder.H1(GemtextH1Binding.inflate(i))
            Line.TYPE_MEDIUM_HEADER -> GemtextViewHolder.H2(GemtextH2Binding.inflate(i))
            Line.TYPE_SMALL_HEADER -> GemtextViewHolder.H3(GemtextH3Binding.inflate(i))
            Line.TYPE_LIST_ITEM -> GemtextViewHolder.ListItem(GemtextTextBinding.inflate(i))
            Line.TYPE_IMAGE_LINK -> GemtextViewHolder.ImageLink(GemtextImageLinkBinding.inflate(i))
            Line.TYPE_LINK -> GemtextViewHolder.Link(GemtextLinkBinding.inflate(i))
            Line.TYPE_CODE -> GemtextViewHolder.Code(GemtextCodeBlockBinding.inflate(i))
            Line.TYPE_QUOTE -> GemtextViewHolder.Quote(GemtextQuoteBinding.inflate(i))
            else -> GemtextViewHolder.Text(GemtextTextBinding.inflate(i))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GemtextViewHolder, position: Int) {
        val line = processedLines[position]

        when(line.type){
            Line.TYPE_TEXT -> {
                val textHolder = (holder as GemtextViewHolder.Text)
                val regularLine = line as Line.Regular
                textHolder.binding.gemtextTextTextview.text = regularLine.line
                setContentTypeface(textHolder.binding.gemtextTextTextview)
            }
            Line.TYPE_BIG_HEADER -> {
                val h1Holder = (holder as GemtextViewHolder.H1)
                val largeHeaderLine = line as Line.HeaderBig
                h1Holder.binding.gemtextTextTextview.text = largeHeaderLine.content
                setHeaderTypeface(h1Holder.binding.gemtextTextTextview)
                if(config.colourLargeHeaders) h1Holder.binding.gemtextTextTextview.setTextColor(config.homeColor)

            }
            Line.TYPE_MEDIUM_HEADER -> {
                val h2Holder = (holder as GemtextViewHolder.H2)
                val mediumHeaderLine = line as Line.HeaderMedium
                h2Holder.binding.gemtextTextTextview.text = mediumHeaderLine.content
                setHeaderTypeface(h2Holder.binding.gemtextTextTextview)
            }
            Line.TYPE_SMALL_HEADER -> {
                val h3Holder = (holder as GemtextViewHolder.H3)
                val smallHeaderLine = line as Line.HeaderSmall
                h3Holder.binding.gemtextTextTextview.text = smallHeaderLine.content
                setHeaderTypeface(h3Holder.binding.gemtextTextTextview)
            }
            Line.TYPE_LIST_ITEM -> {
                val listItemHolder = (holder as GemtextViewHolder.ListItem)
                val listItemLine = line as Line.ListItem
                listItemHolder.binding.gemtextTextTextview.text = "• ${listItemLine.content}"
                setContentTypeface(listItemHolder.binding.gemtextTextTextview)
            }
            Line.TYPE_IMAGE_LINK -> {

            }
            Line.TYPE_LINK -> {

            }
            Line.TYPE_CODE -> {

            }
            Line.TYPE_QUOTE -> {

            }
        }
    }

    private fun setContentTypeface(view: AppCompatTextView) = serenText.applyTypefaceRegular(view, contentTypeface)
    private fun setHeaderTypeface(view: AppCompatTextView) = serenText.applyTypefaceHeader(view, headerTypeface)

}