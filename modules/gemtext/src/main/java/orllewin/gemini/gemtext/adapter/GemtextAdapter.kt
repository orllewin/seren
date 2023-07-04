package orllewin.gemini.gemtext.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import orllewin.extensions.*
import orllewin.filter.db.Filter
import orllewin.gemini.Gemini
import orllewin.gemini.Request
import orllewin.gemini.Response
import orllewin.gemini.gemtext.Gemtext
import orllewin.gemini.gemtext.Line
import orllewin.gemini.UriHandler
import orllewin.gemtext.databinding.*
import orllewin.graphics.DuotoneColourFilter
import orllewin.logger.Logger
import orllewin.lib.resources.SerenText
import orllewin.gemtext.R

class GemtextAdapter(context: Context, val onLink: (link: Uri, longTap: Boolean, view: View, adapterPosition: Int) -> Unit): RecyclerView.Adapter<GemtextViewHolder>() {

    private var renderLines = mutableListOf<String>()
    private var rawLines = mutableListOf<String>()
    private var processedLines = mutableListOf<Line>()
    private val filters: MutableList<Filter> = mutableListOf()
    private val ouri = UriHandler("")
    private var inlineImageUris = HashMap<Int, Uri?>()
    private var imageLineCheckIndex = 0
    private var downloadLineIndex = 0

    private val typeText = 0
    private val typeH1 = 1
    private val typeH2 = 2
    private val typeH3 = 3
    private val typeListItem = 4
    private val typeImageLink = 5
    private val typeImageLinkFullWidth = 6
    private val typeLink = 7
    private val typeLinkFullWidth = 8
    private val typeCodeBlock = 9
    private val typeCodeBlockFullWidth = 10
    private val typeQuote = 11

    private val config = AdapterConfig.getDefault()
    private var colourH1Blocks: Boolean = false
    private var hideCodeBlocks: Boolean = true
    private var showInlineIcons: Boolean = true
    private var fullWidthButtons: Boolean = false
    private var fullWidthButtonColourOverride = -1
    private var headerTypeface = SerenText.DEFAULT
    private var contentTypeface = SerenText.DEFAULT
    private var experimentalMode = "none"

    private var homeColour: Int? = null
    private var duotoneBackgroundColour: Int? = null
    private var duotoneForegroundColour: Int? = null

    private val serenText = SerenText(context)

    @SuppressLint("NotifyDataSetChanged")
    fun render(lines: List<String>, hideAsciiArt: Boolean, removeEmoji: Boolean, autoloadImages: Boolean, address: String){
        inlineImageUris.clear()
        renderLines.clear()
        rawLines.clear()

        rawLines.addAll(lines)
        renderLines.addAll(Gemtext.process(lines, hideAsciiArt, removeEmoji))

        notifyDataSetChanged()

        if(autoloadImages){
            imageLineCheckIndex = 0
            ouri.update(address)
            GlobalScope.launch(Dispatchers.IO) {
                checkImageLinks()
            }
        }
    }

    private suspend fun checkImageLinks(){
        rawLines.forEachIndexed { index, line ->
            val segments = line.split(" ")
            if(line.startsWith("=>") && segments.size > 1 && segments[1].endsWithImage()){
                println("checkImageLinks() found image link: $line")
                val segments = line.split(" ")
                val uri = Uri.parse(segments[1])
                val linkHost = uri.host
                if(linkHost == null){
                    ouri.resolve(uri.toString())
                    val request = Request.Image(ouri.toUri())
                    val imageReq = GlobalScope.async{
                        getImage(request)
                    }

                    val uri = imageReq.await()
                    inlineImageUris[index] = uri
                }
            }
        }
        Handler(Looper.getMainLooper()).post{
            inlineImageUris.keys.forEach { lineIndex ->
                notifyItemChanged(lineIndex)
            }
        }
    }

    private fun getImage(imageRequest: Request.Image): Uri?{
        when (val response = Gemini.synchronousRequest(imageRequest)) {
            is Response.Image -> return response.file
            is Response.Error -> println("GemtextAdapter: error downloading image: ${response.error}")
            else -> println("GemtextAdapter: error downloading image: unexpected response type")
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        val line = renderLines[position]
        return when {
            line.startsWith("```") -> {
                when {
                    fullWidthButtons -> typeCodeBlockFullWidth
                    else -> typeCodeBlock
                }
            }
            line.startsWith("###") -> typeH3
            line.startsWith("##") -> typeH2
            line.startsWith("#") -> typeH1
            line.startsWith("*") -> typeListItem
            line.startsWith("=>") && getLink(line).endsWithImage() -> {
                when {
                    fullWidthButtons -> typeImageLinkFullWidth
                    else -> typeImageLink
                }
            }
            line.startsWith("=>") -> {
                when {
                    fullWidthButtons -> typeLinkFullWidth
                    else -> typeLink
                }
            }
            line.startsWith(">") -> typeQuote
            else -> typeText
        }
    }

    private fun getLink(line: String): String{
        val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
        return linkParts[0]
    }

    private fun getUri(linkLine: String): Uri{
        val linkParts = linkLine.substring(2).trim().split("\\s+".toRegex(), 2)
        return Uri.parse(linkParts.first())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GemtextViewHolder {
        val i = LayoutInflater.from(parent.context)
        return when(viewType){
            typeText -> GemtextViewHolder.Text(GemtextTextBinding.inflate(i))
            typeH1 -> GemtextViewHolder.H1(GemtextH1Binding.inflate(i))
            typeH2 -> GemtextViewHolder.H2(GemtextH2Binding.inflate(i))
            typeH3 -> GemtextViewHolder.H3(GemtextH3Binding.inflate(i))
            typeListItem -> GemtextViewHolder.ListItem(GemtextTextBinding.inflate(i))
            typeImageLink -> GemtextViewHolder.ImageLink(GemtextImageLinkBinding.inflate(i))
            typeImageLinkFullWidth -> GemtextViewHolder.ImageLinkFullWidth(GemtextImageLinkFullWidthBinding.inflate(i))
            typeLink -> GemtextViewHolder.Link(GemtextLinkBinding.inflate(i))
            typeLinkFullWidth -> GemtextViewHolder.LinkFullWidth(GemtextLinkFullWidthBinding.inflate(i))
            typeCodeBlock-> GemtextViewHolder.Code(GemtextCodeBlockBinding.inflate(i))
            typeCodeBlockFullWidth-> GemtextViewHolder.CodeFullWidth(GemtextCodeBlockFullWidthBinding.inflate(i))
            typeQuote -> GemtextViewHolder.Quote(GemtextQuoteBinding.inflate(i))
            else -> GemtextViewHolder.Text(GemtextTextBinding.inflate(i))
        }
    }

    override fun onBindViewHolder(holder: GemtextViewHolder, position: Int) {
        val line = renderLines[position]

        when(holder){
            is GemtextViewHolder.Text -> {
                holder.binding.gemtextTextTextview.text = line
                setContentTypeface(holder.binding.gemtextTextTextview)
            }
            is GemtextViewHolder.Code -> {
                var altText: String? = null
                val b = holder.binding

                if(line.startsWith("```<|ALT|>")){
                    //there's alt text: "```<|ALT|>$alt</|ALT>"
                    altText = line.substring(10, line.indexOf("</|ALT>"))
                    b.gemtextTextMonospaceTextview.text = line.substring(line.indexOf("</|ALT>") + 7)
                }else{
                    b.gemtextTextMonospaceTextview.text = line.substring(3)
                }

                if(hideCodeBlocks){
                    b.showCodeBlock.setText("Show code")//reset for recycling
                    altText?.let{
                        b.showCodeBlock.append(": $altText")
                    }
                    b.showCodeBlock.visible(true)
                    b.showCodeBlock.paint.isUnderlineText = true
                    b.showCodeBlock.setOnClickListener {
                        setupCodeBlockToggle(holder, altText)
                    }

                    b.gemtextTextMonospaceTextview.visible(false)

                    when {
                        showInlineIcons -> b.showCodeBlock.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_code, 0)
                        else -> b.showCodeBlock.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }else{
                    b.showCodeBlock.visible(false)
                    b.gemtextTextMonospaceTextview.visible(true)
                }
            }
            is GemtextViewHolder.CodeFullWidth -> {
                var altText: String? = null
                val b = holder.binding

                if(line.startsWith("```<|ALT|>")){
                    //there's alt text: "```<|ALT|>$alt</|ALT>"
                    altText = line.substring(10, line.indexOf("</|ALT>"))
                    b.gemtextTextMonospaceTextview.text = line.substring(line.indexOf("</|ALT>") + 7)
                }else{
                    b.gemtextTextMonospaceTextview.text = line.substring(3)
                }

                if(hideCodeBlocks){
                    b.showCodeBlock.setText("Show code")//reset for recycling
                    altText?.let{
                        b.showCodeBlock.append(": $altText")
                    }
                    b.showCodeBlock.visible(true)
                    b.showCodeBlock.paint.isUnderlineText = true
                    b.showCodeBlock.setOnClickListener {
                        setupCodeBlockToggleFullWidth(holder, altText)
                    }

                    b.gemtextTextMonospaceTextview.visible(false)

                    when {
                        showInlineIcons -> b.showCodeBlock.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_code, 0)
                        else -> b.showCodeBlock.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }else{
                    b.showCodeBlock.visible(false)
                    b.gemtextTextMonospaceTextview.visible(true)
                }
            }
            is GemtextViewHolder.H1 -> {
                when {
                    line.length > 2 -> holder.binding.gemtextTextTextview.text = line.substring(2).trim()
                    else -> holder.binding.gemtextTextTextview.text = ""
                }
                setHeaderTypeface(holder.binding.gemtextTextTextview)
                if(colourH1Blocks && homeColour != null) holder.binding.gemtextTextTextview.setTextColor(homeColour!!)
            }
            is GemtextViewHolder.H2 -> {
                when {
                    line.length > 3 -> holder.binding.gemtextTextTextview.text = line.substring(3).trim()
                    else -> holder.binding.gemtextTextTextview.text = ""
                }
                setHeaderTypeface(holder.binding.gemtextTextTextview)
            }
            is GemtextViewHolder.H3 -> {
                when {
                    line.length > 4 -> holder.binding.gemtextTextTextview.text = line.substring(4).trim()
                    else -> holder.binding.gemtextTextTextview.text = ""
                }
                setHeaderTypeface(holder.binding.gemtextTextTextview)
            }
            is GemtextViewHolder.ImageLink -> {
                val b = holder.binding
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                if(linkParts.size > 1) linkName = linkParts[1]

                val displayText = linkName
                b.gemtextTextLink.text = displayText
                b.gemtextTextLink.paint.isUnderlineText = true
                b.gemtextTextLink.setOnClickListener {
                    val uri = getUri(renderLines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.itemView, holder.adapterPosition)
                }
                b.gemtextTextLink.setOnLongClickListener {
                    val uri = getUri(renderLines[holder.adapterPosition])
                    println("User long-clicked link: $uri")
                    onLink(uri, true, holder.itemView, holder.adapterPosition)
                    true
                }

                setContentTypeface(b.gemtextTextLink)

                when {
                    inlineImageUris.containsKey(position) -> {
                        b.gemtextInlineImage.visible(true)
                        b.gemtextInlineImage.setImageURI(inlineImageUris[position])
                        configureImage(b.gemtextInlineImage)
                        b.gemtextInlineImageCard.show()
                    }
                    else -> {
                        b.gemtextInlineImage.setImageURI(null)
                        b.gemtextInlineImage.hide()
                    }
                }

                when {
                    showInlineIcons -> {
                        if(downloadLineIndex == position){
                            b.gemtextTextLink.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_image, 0)
                        }else{
                            b.gemtextTextLink.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_image, 0)
                        }
                    }
                    else -> b.gemtextTextLink.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            is GemtextViewHolder.ImageLinkFullWidth -> {
                val b = holder.binding
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                if(linkParts.size > 1) linkName = linkParts[1]

                if(fullWidthButtonColourOverride != -1) b.gemtextTextLink.backgroundTintList = ColorStateList.valueOf(fullWidthButtonColourOverride)

                val displayText = linkName
                b.gemtextTextLink.text = displayText
                b.gemtextTextLink.paint.isUnderlineText = true
                b.gemtextTextLink.setOnClickListener {
                    val uri = getUri(renderLines[holder.adapterPosition])
                    println("User clicked link: $uri")
                    onLink(uri, false, holder.itemView, holder.adapterPosition)

                }
                b.gemtextTextLink.setOnLongClickListener {
                    val uri = getUri(renderLines[holder.adapterPosition])
                    println("User long-clicked link: $uri")
                    onLink(uri, true, holder.itemView, holder.adapterPosition)
                    true
                }

                setContentButtonTypeface(b.gemtextTextLink)

                when {
                    inlineImageUris.containsKey(position) -> {
                        b.gemtextInlineImageCard.visible(true)
                        configureImage(b.gemtextInlineImage)
                        b.gemtextInlineImage.setImageURI(inlineImageUris[position])
                    }
                    else -> {
                        b.gemtextInlineImage.setImageURI(null)
                        b.gemtextInlineImageCard.hide()
                    }
                }

                when {
                    showInlineIcons -> b.gemtextTextLink.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_image, 0)
                    else -> b.gemtextTextLink.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            is GemtextViewHolder.Link -> {
                val b = holder.binding
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                val filter: Filter? = matchFilter(linkParts[0])

                if(filter != null && filter.isHide()){
                    b.root.hide()
                }else {
                    //No filter, render normally
                    b.root.show()
                    if (linkParts.size > 1) linkName = linkParts[1]

                    val displayText = linkName
                    b.gemtextTextLink.text = displayText
                    b.gemtextTextLink.paint.isUnderlineText = true

                    when {
                        filter != null && filter.isWarning() -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_caution, 0)
                        showInlineIcons && linkParts.first().startsWith("http") -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_external, 0)
                        else -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }

                    b.gemtextTextLink.setOnClickListener {
                        val uri = getUri(renderLines[holder.adapterPosition])
                        println("User clicked link: $uri")
                        onLink(uri, false, holder.itemView, holder.adapterPosition)

                    }
                    b.gemtextTextLink.setOnLongClickListener {
                        val uri = getUri(renderLines[holder.adapterPosition])
                        println("User long-clicked link: $uri")
                        onLink(uri, true, holder.itemView, holder.adapterPosition)
                        true
                    }

                    setContentTypeface(b.gemtextTextLink)

                    when {
                        filter != null && filter.isWarning() -> b.root.alpha = 0.5f
                        else -> b.root.alpha = 1f
                    }
                }
            }
            is GemtextViewHolder.LinkFullWidth -> {
                val b = holder.binding
                val linkParts = line.substring(2).trim().split("\\s+".toRegex(), 2)
                var linkName = linkParts[0]

                val filter: Filter? = matchFilter(linkParts[0])

                if(filter != null && filter.isHide()){
                    b.root.hide()
                }else {
                    //No filter, render normally
                    b.root.show()
                    if (linkParts.size > 1) linkName = linkParts[1]

                    val displayText = linkName

                    b.gemtextTextLink.text = displayText

                    if (fullWidthButtonColourOverride != -1) b.gemtextTextLink.backgroundTintList =
                        ColorStateList.valueOf(fullWidthButtonColourOverride)

                    when {
                        filter != null && filter.isWarning() -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_caution, 0)
                        showInlineIcons && linkParts.first().startsWith("http") -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.vector_external, 0)
                        else -> b.gemtextTextLink.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    }

                    b.gemtextTextLink.setOnClickListener {
                        val uri = getUri(renderLines[holder.adapterPosition])
                        println("User clicked link: $uri")
                        onLink(uri, false, holder.itemView, holder.adapterPosition)
                    }
                    b.gemtextTextLink.setOnLongClickListener {
                        val uri = getUri(renderLines[holder.adapterPosition])
                        println("User long-clicked link: $uri")
                        onLink(uri, true, holder.itemView, holder.adapterPosition)
                        true
                    }

                    setContentButtonTypeface(b.gemtextTextLink)
                }
            }
            is GemtextViewHolder.ListItem -> {
                holder.binding.gemtextTextTextview.text = "• ${line.substring(1)}".trim()
                setContentTypeface(holder.binding.gemtextTextTextview)
            }
            is GemtextViewHolder.Quote -> {
                holder.binding.gemtextTextQuoteTextview.text = line.substring(1).trim()
                setContentTypeface(holder.binding.gemtextTextQuoteTextview)
            }
        }
    }

    private fun configureImage(imageView: AppCompatImageView){
        when(experimentalMode){
            "monochrome" -> imageView.monochrome(true)
            "duotone" -> imageView.colorFilter = DuotoneColourFilter.get(duotoneForegroundColour ?: Color.BLACK, duotoneBackgroundColour ?: Color.WHITE, 0.5f)
            else -> imageView.normal()
        }
    }

    private fun setupCodeBlockToggle(holder: GemtextViewHolder.Code, altText: String?) {
        val b = holder.binding
        when {
            b.gemtextTextMonospaceTextview.isVisible -> {
                b.showCodeBlock.setText(R.string.show_code)
                b.gemtextTextMonospaceTextview.visible(false)
                altText?.let{
                    b.showCodeBlock.append(": $altText")
                }
            }
            else -> {
                b.showCodeBlock.setText(R.string.hide_code)
                b.gemtextTextMonospaceTextview.visible(true)
                altText?.let{
                    b.showCodeBlock.append(": $altText")
                }
            }
        }
    }

    private fun setupCodeBlockToggleFullWidth(holder: GemtextViewHolder.CodeFullWidth, altText: String?) {
        val b = holder.binding
        if(fullWidthButtonColourOverride != -1) b.showCodeBlock.backgroundTintList = ColorStateList.valueOf(fullWidthButtonColourOverride)
        when {
            b.gemtextTextMonospaceTextview.isVisible -> {
                b.showCodeBlock.setText(R.string.show_code)

                b.gemtextTextMonospaceTextview.visible(false)
                altText?.let{
                    b.showCodeBlock.append(": $altText")
                }
            }
            else -> {
                b.showCodeBlock.setText(R.string.hide_code)
                b.gemtextTextMonospaceTextview.visible(true)
                altText?.let{
                    b.showCodeBlock.append(": $altText")
                }
            }
        }
    }

    override fun getItemCount(): Int = renderLines.size

    private fun setContentButtonTypeface(view: AppCompatButton){
        when(contentTypeface){
            "sans_serif" -> view.typeface = Typeface.SANS_SERIF
            "serif" -> view.typeface = Typeface.SERIF
            else -> view.typeface = Typeface.SERIF
        }
    }

    private fun setContentTypeface(view: AppCompatTextView) = serenText.applyTypefaceRegular(view, contentTypeface)
    private fun setHeaderTypeface(view: AppCompatTextView) = serenText.applyTypefaceHeader(view, headerTypeface)

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterViewSettings(
        config: AdapterConfig,
        colourH1Blocks: Boolean,
        hideCodeBlocks: Boolean,
        showInlineIcons: Boolean,
        fullWidthButtons: Boolean,
        fullWidthButtonColour: String?,
        headerTypeface: String,
        contentTypeface: String,
        experimentalMode: String,
        @ColorInt duotoneBackgroundColour: Int?,
        @ColorInt duotoneForegroundColour: Int?,
        @ColorInt homeColour: Int?) {

        this.config.clone(config)

        this.colourH1Blocks = colourH1Blocks
        this.hideCodeBlocks = hideCodeBlocks
        this.showInlineIcons = showInlineIcons
        this.fullWidthButtons = fullWidthButtons
        fullWidthButtonColour?.let { color ->
            try {
                val longColour = java.lang.Long.parseLong(color.substring(1), 16)
                this.fullWidthButtonColourOverride = Color.parseColor(fullWidthButtonColour)
            }catch (nfe: NumberFormatException){
                Logger.log("Error parsing full width button color: $fullWidthButtonColour")
            }
        }
        this.headerTypeface = headerTypeface
        this.contentTypeface = contentTypeface
        this.experimentalMode = experimentalMode

        this.duotoneForegroundColour = duotoneForegroundColour
        this.duotoneBackgroundColour = duotoneBackgroundColour
        this.homeColour = homeColour
    }

    fun getRaw(): String = rawLines.joinToString("\n")

    fun loadImage(position: Int, uri: Uri) {
        inlineImageUris[position] = uri
        notifyItemChanged(position)
    }

    fun setFilters(filters: List<Filter>) {
        this@GemtextAdapter.filters.clear()
        this@GemtextAdapter.filters.addAll(filters)
    }

    private fun matchFilter(url: String): Filter? {
        return when {
            filters.isEmpty() || !url.startsWith("gemini://") -> null
            else -> filters.find { filter -> url.contains(filter.filterUrl) }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun render() = notifyDataSetChanged()
}