package com.example.chan

/**
 * Represents a menu item in the settings
 */
data class MenuItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int? = null,
    val action: () -> Unit
) 