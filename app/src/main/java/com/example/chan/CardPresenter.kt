package com.example.chan

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

class CardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val thread = item as Thread
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = thread.semantic_url?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: thread.sub ?: "(No Subject)"
        cardView.contentText = "Replies: ${thread.replies ?: 0}"

        if (thread.tim != null) {
            val thumbnailUrl = "https://i.4cdn.org/wsg/${thread.tim}s.jpg"
            Glide.with(viewHolder.view.context)
                .load(thumbnailUrl)
                .centerCrop()
                .error(android.R.drawable.ic_menu_gallery)
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
}
