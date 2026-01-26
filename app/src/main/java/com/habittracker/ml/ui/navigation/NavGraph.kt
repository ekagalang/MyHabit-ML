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

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
                    navController.navigate(Screen.HabitDetail.route + "/$habitId")
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) }
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
                    navController.navigate(Screen.HabitDetail.route + "/$habitId")
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
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
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
    }
}
