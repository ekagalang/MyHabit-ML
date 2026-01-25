package com.habittracker.ml.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
    object AddHabit : Screen("add_habit")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}