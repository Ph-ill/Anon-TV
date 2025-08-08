package com.example.chan

import android.app.Application

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        android.util.Log.d("App", "App.onCreate() called")
        
        // Initialize theme manager and apply saved theme
        ThemeManager.getInstance(this).applySavedTheme()
        
        // Initialize favourites manager
        android.util.Log.d("App", "About to initialize FavouritesManager")
        FavouritesManager.initialize(this)
        android.util.Log.d("App", "FavouritesManager initialized")
        
        // Initialize hidden threads manager
        android.util.Log.d("App", "About to initialize HiddenThreadsManager")
        HiddenThreadsManager.initialize(this)
        android.util.Log.d("App", "HiddenThreadsManager initialized")
    }
}
