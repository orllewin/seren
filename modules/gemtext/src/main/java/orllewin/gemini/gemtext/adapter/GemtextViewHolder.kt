package orllewin.gemini.gemtext.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import orllewin.gemtext.databinding.*

sealed class GemtextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    class Text(val binding: GemtextTextBinding): GemtextViewHolder(binding.root)
    class H1(val binding: GemtextH1Binding): GemtextViewHolder(binding.root)
    class H2(val binding: GemtextH2Binding): GemtextViewHolder(binding.root)
    class H3(val binding: GemtextH3Binding): GemtextViewHolder(binding.root)
    class ListItem(val binding: GemtextTextBinding): GemtextViewHolder(binding.root)
    class ImageLink(val binding: GemtextImageLinkBinding): GemtextViewHolder(binding.root)
    class ImageLinkFullWidth(val binding: GemtextImageLinkFullWidthBinding): GemtextViewHolder(binding.root)
    class Link(val binding: GemtextLinkBinding): GemtextViewHolder(binding.root)
    class LinkFullWidth(val binding: GemtextLinkFullWidthBinding): GemtextViewHolder(binding.root)
    class Code(val binding: GemtextCodeBlockBinding): GemtextViewHolder(binding.root)
    class CodeFullWidth(val binding: GemtextCodeBlockFullWidthBinding): GemtextViewHolder(binding.root)
    class Quote(val binding: GemtextQuoteBinding): GemtextViewHolder(binding.root)
}