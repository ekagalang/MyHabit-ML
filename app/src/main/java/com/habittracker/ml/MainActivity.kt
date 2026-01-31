package com.habittracker.ml

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.ui.MainScreen
import com.habittracker.ml.ui.theme.MyHabitTheme
import com.habittracker.ml.utils.DailySummaryScheduler
import com.habittracker.ml.utils.NotificationHelper
import com.habittracker.ml.utils.ThemeManager
import com.habittracker.ml.utils.WorkManagerHelper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.habittracker.ml.ui.screens.OnboardingScreen

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ Notification permission granted")
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "⚠️ Notification permission denied")
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme BEFORE super.onCreate()
        ThemeManager.applyTheme(ThemeManager.getThemeMode(this))

        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 MainActivity onCreate started")

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use Compose
        setContent {
            val themeMode by ThemeManager.themeModeState
            val isDark = when (themeMode) {
                ThemeManager.MODE_DARK -> true
                ThemeManager.MODE_LIGHT -> false
                ThemeManager.MODE_SYSTEM -> isSystemInDarkTheme()
                else -> isSystemInDarkTheme()
            }
            val appPreferences = remember { AppPreferences(this@MainActivity) }
            var showOnboarding by remember { mutableStateOf(!appPreferences.isOnboardingCompleted) }

            MyHabitTheme(darkTheme = isDark) {
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !isDark
                    )
                }

                if (showOnboarding) {
                    OnboardingScreen(
                        onFinish = {
                            appPreferences.completeOnboarding()
                            showOnboarding = false
                        }
                    )
                } else {
                    MainScreen()
                }
            }
        }

        // ========== CRITICAL: Initialize all background services ==========
        initializeApp()
    }

    private fun initializeApp() {
        Log.d(TAG, "🔧 Initializing app services...")

        // 1. Create notification channel (must be done before any notification)
        NotificationHelper.createNotificationChannel(this)
        Log.d(TAG, "✅ Notification channel created")

        // 2. Request notification permission (Android 13+)
        requestNotificationPermission()

        // 3. Schedule daily summary (if enabled)
        scheduleDailySummary()

        // 4. Schedule weekly ML predictions
        scheduleWeeklyPredictions()

        // 5. Check battery optimization (delayed to not overwhelm user)
        // Uncomment if you want to prompt user about battery optimization
        // checkBatteryOptimization()

        Log.d(TAG, "✅ App initialization complete")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "📱 onResume - checking exact alarm permission")
        checkExactAlarmPermission()
    }

    // ========== NOTIFICATION PERMISSION (Android 13+) ==========

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d(TAG, "ℹ️ Showing rationale for notification permission")
                    Toast.makeText(
                        this,
                        "Notification permission needed for habit reminders",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d(TAG, "📲 Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "✅ Notification permission not required (Android < 13)")
        }
    }

    // ========== EXACT ALARM PERMISSION (Android 12+) ==========

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Exact alarm permission missing")
                Toast.makeText(
                    this,
                    "Please enable exact alarms in settings for accurate reminders",
                    Toast.LENGTH_LONG
                ).show()

                // Open settings
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error opening exact alarm settings", e)
                }
            } else {
                Log.d(TAG, "✅ Exact alarm permission granted")
            }
        } else {
            Log.d(TAG, "✅ Exact alarm permission not required (Android < 12)")
        }
    }

    // ========== DAILY SUMMARY SCHEDULING ==========

    private fun scheduleDailySummary() {
        val appPreferences = AppPreferences(this)

        if (appPreferences.dailySummaryEnabled) {
            val summaryTime = appPreferences.dailySummaryTime
            Log.d(TAG, "📅 Scheduling daily summary at: $summaryTime")

            val scheduler = DailySummaryScheduler(this)
            scheduler.scheduleDailySummary(summaryTime)

            Log.d(TAG, "✅ Daily summary scheduled")
        } else {
            Log.d(TAG, "ℹ️ Daily summary is disabled in settings")
        }
    }

    // ========== WEEKLY PREDICTION SCHEDULING ==========

    private fun scheduleWeeklyPredictions() {
        Log.d(TAG, "🤖 Scheduling weekly ML predictions")
        WorkManagerHelper.scheduleWeeklyPredictions(this)
        Log.d(TAG, "✅ Weekly predictions scheduled")
    }

    // ========== BATTERY OPTIMIZATION (OPTIONAL) ==========

    /**
     * Prompts user to disable battery optimization for this app.
     * This helps ensure notifications and background tasks work reliably.
     *
     * Uncomment the call in initializeApp() if you want to use this.
     */
    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "⚠️ Battery optimization is enabled")

                // Show dialog explaining why we need this
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Battery Optimization")
                    .setMessage(
                        "To ensure habit reminders work correctly even when your device is idle, " +
                                "please allow MyHabit to run in the background without battery restrictions."
                    )
                    .setPositiveButton("Allow") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error opening battery optimization settings", e)
                        }
                    }
                    .setNegativeButton("Not Now", null)
                    .show()
            } else {
                Log.d(TAG, "✅ Battery optimization is disabled (good!)")
            }
        }
    }

    // ========== LIFECYCLE LOGGING ==========

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "📱 onStart")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "📱 onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "📱 onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "📱 onDestroy")
    }
}
