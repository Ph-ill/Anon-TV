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
import android.view.KeyEvent
import android.content.Context
import android.os.Handler
import android.os.Looper

// Data class to represent a loading state
data class LoadingCard(val isLoading: Boolean = true)

class CardPresenter(private val onFavouriteChanged: (() -> Unit)? = null) : Presenter() {
    
    private var popupMenu: ThreadPopupMenu? = null

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

        // Apply dark background to the entire card
        cardView.setBackgroundColor(0xFF1A1A1A.toInt())

        // Use post to ensure styling is applied after view is created
        cardView.post {
            try {
                // Try to find and style the text container
                for (i in 0 until cardView.childCount) {
                    val child = cardView.getChildAt(i)
                    if (child is android.view.ViewGroup) {
                        child.setBackgroundColor(0xFF1A1A1A.toInt())
                        // Also style any text views within
                        for (j in 0 until child.childCount) {
                            val textChild = child.getChildAt(j)
                            if (textChild is android.widget.TextView) {
                                textChild.setTextColor(0xFFFFFFFF.toInt())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("CardPresenter", "Could not style card children: ${e.message}")
            }
        }

        // Set up long press detection for popup menu
        setupLongPressDetection(cardView, thread)

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
        
        // Apply dark background to the entire card
        cardView.setBackgroundColor(0xFF1A1A1A.toInt())
        
        // Use post to ensure styling is applied after view is created
        cardView.post {
            try {
                // Try to find and style the text container
                for (i in 0 until cardView.childCount) {
                    val child = cardView.getChildAt(i)
                    if (child is android.view.ViewGroup) {
                        child.setBackgroundColor(0xFF1A1A1A.toInt())
                        // Also style any text views within
                        for (j in 0 until child.childCount) {
                            val textChild = child.getChildAt(j)
                            if (textChild is android.widget.TextView) {
                                textChild.setTextColor(0xFFFFFFFF.toInt())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("CardPresenter", "Could not style card children: ${e.message}")
            }
        }
        
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
    
    private fun setupLongPressDetection(cardView: ImageCardView, thread: Thread) {
        // Use Android's built-in OnLongClickListener for reliable long press detection
        cardView.setOnLongClickListener {
            Log.d("CardPresenter", "Long click detected on thread: ${thread.no}")
            
            // Initialize popup menu if needed
            if (popupMenu == null) {
                popupMenu = ThreadPopupMenu(cardView.context, onFavouriteChanged)
            }
            
            // Show popup menu
            popupMenu?.show(cardView, thread)
            Log.d("CardPresenter", "Popup menu shown for thread: ${thread.no}")
            true // Consume the long click event to prevent normal click
        }
        
        // Make sure the card view can receive focus and long clicks
        cardView.isFocusable = true
        cardView.isLongClickable = true
    }
}
