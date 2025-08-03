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

        cardView.titleText = thread.semantic_url?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: "Thread No. ${thread.no}"
        cardView.contentText = "Replies: ${thread.replies ?: 0}"

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
    }
}
