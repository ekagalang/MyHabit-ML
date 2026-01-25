package com.habittracker.ml.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habittracker.ml.ui.navigation.NavGraph
import com.habittracker.ml.ui.navigation.Screen
// import com.habittracker.ml.ui.theme.* // Menggunakan warna yang sama dengan HomeScreen untuk konsistensi
private val AppPrimary = Color(0xFF2ED1A2)
private val AppBackgroundLight = Color(0xFFF8FAFD)
private val AppTextMuted = Color(0xFF73777F)

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Menggunakan Scaffold dasar, tapi bottom bar-nya kita buat custom "floating" di dalam Box utama
    Scaffold(
        containerColor = AppBackgroundLight
        // Kita tidak menggunakan parameter bottomBar di sini karena kita ingin efek floating/overlay
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            // Konten Utama (NavGraph)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding standar dari Scaffold (jika ada)
            ) {
                NavGraph(navController = navController)
            }

            // Floating Navigation Bar (Overlay di bawah)
            FloatingBottomNavigation(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun FloatingBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = modifier
            .padding(bottom = 24.dp)
            .width(280.dp)
            .shadow(
                elevation = 16.dp,
                spotColor = Color.Black.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .background(Color.White.copy(alpha = 0.95f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingNavItem(
                icon = Icons.Outlined.Home,
                selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            FloatingNavItem(
                icon = Icons.Outlined.BarChart,
                selected = currentDestination?.hierarchy?.any { it.route == Screen.Insights.route } == true,
                onClick = {
                    navController.navigate(Screen.Insights.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            FloatingNavItem(
                icon = Icons.Outlined.Person,
                selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                onClick = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun FloatingNavItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(if (selected) AppBackgroundLight else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) AppPrimary else AppTextMuted,
            modifier = Modifier.size(26.dp)
        )
    }
}