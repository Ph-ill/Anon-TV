package com.example.chan

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.chan.Thread

// Data class to represent a loading state
data class LoadingCard(val isLoading: Boolean = true)

class CardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.setMainImageDimensions(313, 176)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        when (item) {
            is Thread -> bindThreadCard(viewHolder, item)
            is LoadingCard -> bindLoadingCard(viewHolder, item)
            is MenuItem -> bindMenuItemCard(viewHolder, item)
        }
    }

    private fun bindThreadCard(viewHolder: ViewHolder, thread: Thread) {
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

    private fun bindLoadingCard(viewHolder: ViewHolder, loadingCard: LoadingCard) {
        Log.d("CardPresenter", "Binding loading card")
        val cardView = viewHolder.view as ImageCardView
        
        cardView.titleText = "Loading..."
        cardView.contentText = "Loading more threads..."
        
        // Set a loading image or spinner
        cardView.mainImageView.setImageResource(android.R.drawable.ic_popup_sync)
    }

    private fun bindMenuItemCard(viewHolder: ViewHolder, menuItem: MenuItem) {
        Log.d("CardPresenter", "Binding menu item: ${menuItem.title}")
        val cardView = viewHolder.view as ImageCardView
        
        cardView.titleText = menuItem.title
        cardView.contentText = menuItem.description
        
        // Set icon if available
        menuItem.icon?.let { iconRes ->
            cardView.mainImageView.setImageResource(iconRes)
        } ?: run {
            cardView.mainImageView.setImageResource(android.R.drawable.ic_menu_manage)
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
