package com.habittracker.ml.data.local.preferences

data class CustomCategory(
    val id: String,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)
