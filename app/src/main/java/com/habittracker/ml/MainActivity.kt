package com.habittracker.ml

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.habittracker.ml.utils.NotificationHelper
import android.app.AlarmManager
import android.os.PowerManager
import android.net.Uri
import android.provider.Settings
import android.content.Context
import android.content.Intent
import com.habittracker.ml.utils.WorkManagerHelper
import com.habittracker.ml.utils.ThemeManager
import androidx.activity.compose.setContent
import com.habittracker.ml.ui.MainScreen
import com.habittracker.ml.ui.theme.MyHabitTheme

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigation buttons
    private lateinit var navHome: LinearLayout
    private lateinit var navStats: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var iconHome: TextView
    private lateinit var iconStats: TextView
    private lateinit var iconProfile: TextView

    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme
        ThemeManager.applyTheme(ThemeManager.getThemeMode(this))

        super.onCreate(savedInstanceState)

        // Use Compose instead of XML
        setContent {
            val isDark = ThemeManager.isDarkModeActive(this@MainActivity)
            MyHabitTheme(darkTheme = isDark) {
                MainScreen()
            }
        }

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        checkExactAlarmPermission()
    }

    private fun setupBottomNavigation() {
        // Initialize views
        navHome = findViewById(R.id.navHome)
        navStats = findViewById(R.id.navStats)
        navProfile = findViewById(R.id.navProfile)

        iconHome = findViewById(R.id.iconHome)
        iconStats = findViewById(R.id.iconStats)
        iconProfile = findViewById(R.id.iconProfile)

        // Set initial active state
        setActiveNavItem(navHome, iconHome)

        // Click listeners
        navHome.setOnClickListener {
            navigateToHome()
        }

        navStats.setOnClickListener {
            navigateToStats()
        }

        navProfile.setOnClickListener {
            navigateToProfile()
        }

        // Listen to navigation changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.habitsListFragment -> setActiveNavItem(navHome, iconHome)
                R.id.insightsFragment -> setActiveNavItem(navStats, iconStats)
                R.id.settingsFragment -> setActiveNavItem(navProfile, iconProfile)
            }
        }
    }

    private fun navigateToHome() {
        if (navController.currentDestination?.id != R.id.habitsListFragment) {
            navController.navigate(R.id.habitsListFragment)
        }
        setActiveNavItem(navHome, iconHome)
    }

    private fun navigateToStats() {
        if (navController.currentDestination?.id != R.id.insightsFragment) {
            // Check if insights fragment exists in nav graph
            try {
                navController.navigate(R.id.insightsFragment)
            } catch (e: Exception) {
                // If insights fragment doesn't exist yet, show message
                Toast.makeText(this, "Insights screen coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
        setActiveNavItem(navStats, iconStats)
    }

    private fun navigateToProfile() {
        if (navController.currentDestination?.id != R.id.settingsFragment) {
            navController.navigate(R.id.settingsFragment)
        }
        setActiveNavItem(navProfile, iconProfile)
    }

    private fun setActiveNavItem(activeLayout: LinearLayout, activeIcon: TextView) {
        // Reset all
        resetNavItem(navHome, iconHome)
        resetNavItem(navStats, iconStats)
        resetNavItem(navProfile, iconProfile)

        // Set active
        activeLayout.setBackgroundResource(R.drawable.bg_nav_active)
        activeIcon.alpha = 1.0f

        // Scale animation
        activeIcon.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .start()
    }

    private fun resetNavItem(layout: LinearLayout, icon: TextView) {
        layout.background = null
        icon.alpha = 0.5f
        icon.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(200)
            .start()
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Battery Optimization")
                    .setMessage("To ensure reminders work correctly on this device, please allow the app to run in the background.")
                    .setPositiveButton("Allow") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation to user
                    Toast.makeText(
                        this,
                        "Notification permission needed for habit reminders",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Please enable exact alarms in settings",
                    Toast.LENGTH_LONG
                ).show()

                // Open settings
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}
