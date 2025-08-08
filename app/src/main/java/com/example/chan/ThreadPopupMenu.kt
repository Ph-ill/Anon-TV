package com.example.chan

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.leanback.widget.ImageCardView

class ThreadPopupMenu(private val context: Context, private val onFavouriteChanged: (() -> Unit)? = null) {
    
    private var dialog: AlertDialog? = null
    
    fun show(cardView: ImageCardView?, thread: Thread) {
        Log.d("ThreadPopupMenu", "Attempting to show popup for thread: ${thread.no}")
        
        try {
            // Dismiss any existing dialog
            dialog?.dismiss()
            
            // Inflate custom layout
            val inflater = LayoutInflater.from(context)
            val customView = inflater.inflate(R.layout.custom_popup_menu, null)
            
            // Set up click listeners
            val hideOption = customView.findViewById<TextView>(R.id.hide_option)
            val favouriteOption = customView.findViewById<TextView>(R.id.favourite_option)
            
            // Check if thread is already in favourites and update text accordingly
            val isFavourite = FavouritesManager.isFavourite(thread)
            favouriteOption.text = if (isFavourite) "Unfavourite" else "Favourite"
            
            hideOption.setOnClickListener {
                CustomToast.show(context, "Hide option clicked for thread ${thread.no}")
                Log.d("ThreadPopupMenu", "Hide clicked for thread: ${thread.no}")
                dialog?.dismiss()
            }
            
            favouriteOption.setOnClickListener {
                if (isFavourite) {
                    // Remove from favourites
                    if (FavouritesManager.removeFavourite(thread)) {
                        CustomToast.showSuccess(context, "Removed from favourites")
                        Log.d("ThreadPopupMenu", "Unfavourited thread: ${thread.no}")
                        onFavouriteChanged?.invoke()
                    }
                } else {
                    // Add to favourites
                    if (FavouritesManager.addFavourite(thread)) {
                        CustomToast.showSuccess(context, "Added to favourites")
                        Log.d("ThreadPopupMenu", "Favourited thread: ${thread.no}")
                        onFavouriteChanged?.invoke()
                    }
                }
                dialog?.dismiss()
            }
            
            // Create dialog with custom view
            val builder = AlertDialog.Builder(context)
            builder.setView(customView)
            builder.setCancelable(true)
            
            dialog = builder.create()
            
            // Make dialog background transparent so our custom background shows
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            dialog?.show()
            Log.d("ThreadPopupMenu", "Custom dialog shown successfully")
            
        } catch (e: Exception) {
            Log.e("ThreadPopupMenu", "Error showing popup menu", e)
            CustomToast.showError(context, "Error showing menu: ${e.message}")
        }
    }
    
    fun hide() {
        dialog?.dismiss()
        dialog = null
    }
    
    fun isPopupShowing(): Boolean = dialog?.isShowing == true
}
