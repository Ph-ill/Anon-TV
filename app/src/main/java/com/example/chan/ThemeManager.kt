package com.example.chan

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages theme switching and preferences for the AnonTv app
 */
class ThemeManager private constructor(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    enum class ThemeMode(val value: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    /**
     * Get the current theme mode
     */
    fun getCurrentThemeMode(): ThemeMode {
        val savedMode = prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.value)
        return ThemeMode.values().find { it.value == savedMode } ?: ThemeMode.SYSTEM
    }
    
    /**
     * Set the theme mode and apply it
     */
    fun setThemeMode(themeMode: ThemeMode) {
        prefs.edit().putInt(KEY_THEME_MODE, themeMode.value).apply()
        AppCompatDelegate.setDefaultNightMode(themeMode.value)
    }
    
    /**
     * Check if the current theme is dark
     */
    fun isDarkTheme(context: Context): Boolean {
        return when (getCurrentThemeMode()) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and 
                    Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    /**
     * Get the current theme name for display
     */
    fun getCurrentThemeName(): String {
        return when (getCurrentThemeMode()) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System"
        }
    }
    
    /**
     * Get all available theme modes
     */
    fun getAvailableThemes(): List<ThemeMode> {
        return ThemeMode.values().toList()
    }
    
    /**
     * Apply the saved theme mode on app startup
     */
    fun applySavedTheme() {
        val savedMode = getCurrentThemeMode()
        AppCompatDelegate.setDefaultNightMode(savedMode.value)
    }
} 