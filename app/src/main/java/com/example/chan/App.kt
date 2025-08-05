package com.example.chan

import android.app.Application

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize theme manager and apply saved theme
        ThemeManager.getInstance(this).applySavedTheme()
    }
}
