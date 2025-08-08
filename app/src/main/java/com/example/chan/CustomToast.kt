package com.example.chan

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

object CustomToast {
    
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        // For API level 30 and above, custom toast views are restricted
        // So we'll fall back to regular Toast for newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(context, message, duration).show()
        } else {
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.custom_toast, null)
            
            val text = layout.findViewById<TextView>(R.id.toast_message)
            text.text = message
            
            val toast = Toast(context)
            toast.duration = duration
            @Suppress("DEPRECATION")
            toast.view = layout
            toast.show()
        }
    }
    
    fun showSuccess(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }
    
    fun showError(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
}
