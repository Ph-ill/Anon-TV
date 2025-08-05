package com.example.chan

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Utility class for theme-related operations
 */
object ThemeUtils {
    
    /**
     * Get a color from the current theme
     */
    fun getThemeColor(context: Context, attrResId: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
    
    /**
     * Get a drawable from the current theme
     */
    fun getThemeDrawable(context: Context, attrResId: Int): Drawable? {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrResId))
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()
        return drawable
    }
    
    /**
     * Get primary color from theme
     */
    fun getPrimaryColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimary)
    }
    
    /**
     * Get secondary color from theme
     */
    fun getSecondaryColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorSecondary)
    }
    
    /**
     * Get background color from theme
     */
    fun getBackgroundColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorBackground)
    }
    
    /**
     * Get text primary color from theme
     */
    fun getTextPrimaryColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorTextPrimary)
    }
    
    /**
     * Get text secondary color from theme
     */
    fun getTextSecondaryColor(context: Context): Int {
        return getThemeColor(context, R.attr.colorTextSecondary)
    }
    
    /**
     * Get background gradient drawable from theme
     */
    fun getBackgroundGradient(context: Context): Drawable? {
        return getThemeDrawable(context, R.attr.backgroundGradient)
    }
    
    /**
     * Get accent gradient drawable from theme
     */
    fun getAccentGradient(context: Context): Drawable? {
        return getThemeDrawable(context, R.attr.accentGradient)
    }
    
    /**
     * Get card gradient drawable from theme
     */
    fun getCardGradient(context: Context): Drawable? {
        return getThemeDrawable(context, R.attr.cardGradient)
    }
    
    /**
     * Create a ColorStateList for text colors
     */
    fun createTextColorStateList(context: Context): ColorStateList {
        val primaryColor = getTextPrimaryColor(context)
        val secondaryColor = getTextSecondaryColor(context)
        
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf()
            ),
            intArrayOf(
                primaryColor,
                primaryColor,
                secondaryColor
            )
        )
    }
    
    /**
     * Check if the current theme is dark
     */
    fun isDarkTheme(context: Context): Boolean {
        return ThemeManager.getInstance(context).isDarkTheme(context)
    }
    
    /**
     * Get the current theme name
     */
    fun getCurrentThemeName(context: Context): String {
        return ThemeManager.getInstance(context).getCurrentThemeName()
    }
} 