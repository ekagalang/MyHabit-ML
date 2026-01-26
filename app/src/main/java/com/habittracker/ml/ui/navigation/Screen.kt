package com.habittracker.ml.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Workspace : Screen("workspace")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}
