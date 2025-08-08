package com.example.chan

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

object CustomToast {
    
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.custom_toast, null)
            
            val text = layout.findViewById<TextView>(R.id.toast_message)
            text.text = message
            
            val toast = Toast(context)
            toast.duration = duration
            @Suppress("DEPRECATION")
            toast.view = layout
            toast.show()
        } catch (e: Exception) {
            // Fallback to standard toast if custom toast fails
            android.util.Log.e("CustomToast", "Failed to show custom toast, falling back to standard", e)
            Toast.makeText(context, message, duration).show()
        }
    }
    
    fun showSuccess(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }
    
    fun showError(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
}
