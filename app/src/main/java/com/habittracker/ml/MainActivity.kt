package com.habittracker.ml

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.habittracker.ml.utils.NotificationHelper
import android.app.AlarmManager
import android.os.PowerManager
import android.net.Uri
import android.provider.Settings
import android.content.Context
import android.content.Intent
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
