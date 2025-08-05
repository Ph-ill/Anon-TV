package com.example.chan

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener

class ThemeSettingsFragment : BrowseSupportFragment() {
    
    private lateinit var themeManager: ThemeManager
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var themeAdapter: ArrayObjectAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        themeManager = ThemeManager.getInstance(requireContext())
        title = "Theme Settings"
        
        // Clear any existing content
        adapter = null
        
        setupThemeMenu()
        setupClickListeners()
    }
    
    private fun setupThemeMenu() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        themeAdapter = ArrayObjectAdapter(CardPresenter())
        
        // Add theme selection menu items
        val themeHeader = HeaderItem(0, "Choose Theme")
        
        val lightThemeItem = MenuItem(
            id = "light_theme",
            title = "Light Theme",
            description = "Clean, bright interface with subtle gradients",
            icon = android.R.drawable.ic_menu_view,
            action = { 
                themeManager.setThemeMode(ThemeManager.ThemeMode.LIGHT)
                activity?.recreate()
            }
        )
        
        val darkThemeItem = MenuItem(
            id = "dark_theme",
            title = "Dark Theme", 
            description = "Easy on the eyes with dark backgrounds",
            icon = android.R.drawable.ic_menu_view,
            action = { 
                themeManager.setThemeMode(ThemeManager.ThemeMode.DARK)
                activity?.recreate()
            }
        )
        
        val systemThemeItem = MenuItem(
            id = "system_theme",
            title = "System Theme",
            description = "Follows your device's system theme setting",
            icon = android.R.drawable.ic_menu_view,
            action = { 
                themeManager.setThemeMode(ThemeManager.ThemeMode.SYSTEM)
                activity?.recreate()
            }
        )
        
        val currentThemeItem = MenuItem(
            id = "current_theme",
            title = "Current Theme: ${themeManager.getCurrentThemeName()}",
            description = "Tap to see current theme information",
            icon = android.R.drawable.ic_menu_info_details,
            action = { 
                // Show current theme info
            }
        )
        
        themeAdapter.add(lightThemeItem)
        themeAdapter.add(darkThemeItem)
        themeAdapter.add(systemThemeItem)
        themeAdapter.add(currentThemeItem)
        
        rowsAdapter.add(ListRow(themeHeader, themeAdapter))
        adapter = rowsAdapter
    }
    
    private fun setupClickListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is MenuItem -> item.action()
            }
        }
    }
} 