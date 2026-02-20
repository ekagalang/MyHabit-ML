package com.habittracker.ml.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habittracker.ml.ui.screens.HomeScreen
import com.habittracker.ml.ui.screens.ProfileScreen
import com.habittracker.ml.ui.screens.CategoryScreen
import com.habittracker.ml.ui.screens.SettingsScreen
import com.habittracker.ml.ui.screens.AddHabitScreen
import com.habittracker.ml.ui.screens.EditHabitScreen
import com.habittracker.ml.ui.screens.HabitDetailScreen
import com.habittracker.ml.ui.screens.WorkspaceScreen
import com.habittracker.ml.ui.screens.MLInsightsScreen
import com.habittracker.ml.ui.screens.AdvancedInsightsScreen
import com.habittracker.ml.ui.screens.BackupScreen
import com.habittracker.ml.ui.screens.TemplateLibraryScreen
import com.habittracker.ml.ui.screens.ManageTemplatesScreen
import com.habittracker.ml.ui.screens.EditTemplateScreen
import com.habittracker.ml.ui.screens.NotificationCenterScreen
import com.habittracker.ml.ui.screens.PredictionDetailsScreen
import com.habittracker.ml.ui.screens.LoginScreen
import com.habittracker.ml.ui.screens.RegisterScreen
import com.habittracker.ml.data.local.entities.HabitTemplate
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkipLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToEditHabit = { habitId ->
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onNavigateToWorkspace = {
                    navController.navigate(Screen.Workspace.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToMLInsights = {
                    navController.navigate(Screen.Insights.route)
                }
            )
        }

        composable(Screen.Insights.route) {
            MLInsightsScreen(
                onNavigateBack = { navController.navigateUp() },
                showBack = false,
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onNavigateToAdvancedInsights = {
                    navController.navigate(Screen.AdvancedInsights.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationCenterScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                onNavigateToManageTemplates = { navController.navigate(Screen.ManageTemplates.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoryScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Workspace.route) {
            WorkspaceScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddHabit = { navController.navigate(Screen.AddHabit.route) },
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                },
                onNavigateToPredictions = { id ->
                    navController.navigate(Screen.PredictionDetails.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.PredictionDetails.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            PredictionDetailsScreen(
                habitId = habitId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.AddHabit.route +
                    "?templateId={templateId}" +
                    "&templateName={templateName}" +
                    "&templateDesc={templateDesc}" +
                    "&templateCategory={templateCategory}" +
                    "&templateIcon={templateIcon}" +
                    "&templateFreq={templateFreq}" +
                    "&templateTime={templateTime}",
            arguments = listOf(
                navArgument("templateId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateDesc") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateCategory") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateIcon") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateFreq") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("templateTime") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            AddHabitScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTemplates = { navController.navigate(Screen.TemplateLibrary.route) },
                templateId = backStackEntry.arguments?.getString("templateId"),
                templateName = backStackEntry.arguments?.getString("templateName"),
                templateDesc = backStackEntry.arguments?.getString("templateDesc"),
                templateCategory = backStackEntry.arguments?.getString("templateCategory"),
                templateIcon = backStackEntry.arguments?.getString("templateIcon"),
                templateFreq = backStackEntry.arguments?.getString("templateFreq"),
                templateTime = backStackEntry.arguments?.getString("templateTime")
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            EditHabitScreen(
                habitId = habitId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TemplateLibrary.route) {
            TemplateLibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onTemplateSelected = { template ->
                    navController.navigate(
                        Screen.AddHabit.route +
                                "?templateId=${template.id}" +
                                "&templateName=${template.name}" +
                                "&templateDesc=${template.description}" +
                                "&templateCategory=${template.category}" +
                                "&templateIcon=${template.icon}" +
                                "&templateFreq=${template.defaultFrequency}" +
                                "&templateTime=${template.defaultReminderTime ?: ""}"
                    )
                }
            )
        }

        composable(Screen.AdvancedInsights.route) {
            AdvancedInsightsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageTemplates.route) {
            ManageTemplatesScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditTemplate = { templateId ->
                    navController.navigate(Screen.EditTemplate.createRoute(templateId))
                }
            )
        }

        composable(
            route = Screen.EditTemplate.route,
            arguments = listOf(
                navArgument("templateId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getInt("templateId")
            val id = if (templateId == -1) null else templateId
            EditTemplateScreen(
                templateId = id,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
