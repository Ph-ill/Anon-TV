package com.example.chan

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.chan.Thread

class CardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.setMainImageDimensions(313, 176)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val thread = item as Thread
        Log.d("CardPresenter", "Binding thread: $thread")
        val cardView = viewHolder.view as ImageCardView

        // Create a meaningful title from available data
        val title = when {
            !thread.sub.isNullOrBlank() -> thread.sub
            !thread.semantic_url.isNullOrBlank() -> thread.semantic_url.replace("-", " ").replaceFirstChar { it.uppercase() }
            else -> "Thread #${thread.no}"
        }
        
        // Create content text with replies count and comment preview
        val contentText = buildString {
            append("Replies: ${thread.replies ?: 0}")
            if (!thread.com.isNullOrBlank()) {
                val cleanComment = removeHtmlTags(thread.com).trim()
                if (cleanComment.isNotEmpty()) {
                    append(" • ")
                    append(if (cleanComment.length > 50) cleanComment.take(50) + "..." else cleanComment)
                }
            }
        }
        
        cardView.titleText = title
        cardView.contentText = contentText

        if (thread.tim != null) {
            val thumbnailUrl = "https://i.4cdn.org/wsg/${thread.tim}s.jpg"
            Glide.with(viewHolder.view.context)
                .load(thumbnailUrl)
                .centerCrop()
                .error(android.R.drawable.ic_menu_gallery)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("CardPresenter", "Glide load failed for $thumbnailUrl", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(cardView.mainImageView)
        } else {
            cardView.mainImageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
        Glide.with(viewHolder.view.context).clear(cardView.mainImageView)
    }

    private fun removeHtmlTags(html: String): String {
        return html.replace(Regex("<.*?>"), "")
            .replace(Regex("&[a-zA-Z]+;"), " ") // Replace HTML entities
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .trim()
    }
}
