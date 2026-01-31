package com.habittracker.ml.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Workspace : Screen("workspace")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object AddHabit : Screen("add_habit")
    object Backup : Screen("backup")
    object TemplateLibrary : Screen("template_library")
    object ManageTemplates : Screen("manage_templates")
    object AdvancedInsights : Screen("advanced_insights")
    object EditTemplate : Screen("edit_template?templateId={templateId}") {
        fun createRoute(templateId: Int? = null) = if (templateId != null) "edit_template?templateId=$templateId" else "edit_template"
    }
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object PredictionDetails : Screen("prediction_details/{habitId}") {
        fun createRoute(habitId: Long) = "prediction_details/$habitId"
    }
}
