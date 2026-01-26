package com.habittracker.ml.ui.screens

fun slugifyCategory(name: String): String {
    return name.lowercase()
        .trim()
        .replace("\\s+".toRegex(), "_")
        .replace("[^a-z0-9_]".toRegex(), "")
        .ifBlank { "category" }
}
