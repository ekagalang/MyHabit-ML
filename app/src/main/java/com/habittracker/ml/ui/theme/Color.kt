package com.habittracker.ml.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Primary Colors
val Primary = Color(0xFF2ED1A2)
val PrimaryDark = Color(0xFF24B890)
val PrimaryLight = Color(0xFF5EDDB5)
val Highlight = Color(0xFF703EFF)

// Background Colors
val BackgroundLightDefault = Color(0xFFF8FAFD)
val BackgroundDark = Color(0xFF121212)

// Surface Colors
val SurfaceLightDefault = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceVariantLightDefault = Color(0xFFF5F5F5)
val SurfaceVariantDark = Color(0xFF2C3038)

// Text Colors
val TextMainLight = Color(0xFF1A1C1E)
val TextMutedLight = Color(0xFF73777F)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextDark = Color(0xFFFFFFFF)
val TextDarkMuted = Color(0xFFB0B0B0)

// Accent Colors
val AccentSuccess = Color(0xFF10B981)
val AccentWarning = Color(0xFFF59E0B)
val AccentError = Color(0xFFEF4444)
val AccentInfo = Color(0xFF3B82F6)

// Category Colors
val CategoryHealth = Color(0xFF10B981)
val CategoryProductivity = Color(0xFF6366F1)
val CategoryLearning = Color(0xFFF59E0B)
val CategoryMindfulness = Color(0xFF8B5CF6)
val CategorySocial = Color(0xFFEC4899)

// Border Colors
val BorderLightDefault = Color(0xFFE5E7EB)
val BorderDark = Color(0xFF374151)

// Theme-aware aliases (mutable so UI can react)
var BackgroundLight by mutableStateOf(BackgroundLightDefault)
var SurfaceLight by mutableStateOf(SurfaceLightDefault)
var SurfaceVariantLight by mutableStateOf(SurfaceVariantLightDefault)
var TextMain by mutableStateOf(TextMainLight)
var TextMuted by mutableStateOf(TextMutedLight)
var BorderLight by mutableStateOf(BorderLightDefault)
