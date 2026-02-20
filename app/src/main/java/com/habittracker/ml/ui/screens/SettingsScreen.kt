package com.habittracker.ml.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.withTransaction
import com.google.gson.Gson
import com.habittracker.ml.data.local.database.HabitDatabase
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.local.preferences.AuthPreferences
import com.habittracker.ml.workers.SyncWorker
import com.habittracker.ml.ui.theme.*
import com.habittracker.ml.utils.ThemeManager
import kotlinx.coroutines.launch
import android.app.Activity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ExportPayload(
    val habits: List<Habit>,
    val checkIns: List<CheckIn>,
    val exportedAt: Long,
    val versionName: String,
    val versionCode: Int
)

private data class AppVersion(
    val name: String,
    val code: Int
)

private fun getAppVersion(context: Context): AppVersion {
    val pm = context.packageManager
    val pkg = context.packageName
    val info = if (Build.VERSION.SDK_INT >= 33) {
        pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(pkg, 0)
    }
    val code = if (Build.VERSION.SDK_INT >= 28) {
        info.longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        info.versionCode
    }
    return AppVersion(info.versionName ?: "1.0", code)
}

private fun themeLabel(mode: Int): String {
    return when (mode) {
        ThemeManager.MODE_LIGHT -> "Light"
        ThemeManager.MODE_DARK -> "Dark"
        else -> "System"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToManageTemplates: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val authPreferences = remember { AuthPreferences(context) }
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    val appVersion = remember { getAppVersion(context) }

    var notificationsEnabled by remember { mutableStateOf(preferences.notificationsEnabled) }
    var dailySummaryEnabled by remember { mutableStateOf(preferences.dailySummaryEnabled) }
    var reminderTime by remember { mutableStateOf(preferences.defaultReminderTime) }
    var themeMode by remember { mutableStateOf(ThemeManager.getThemeMode(context)) }
    var categoriesData by remember { mutableStateOf(preferences.getCategories()) }

    LaunchedEffect(Unit) {
        categoriesData = preferences.getCategories()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (json.isNullOrBlank()) {
                    Toast.makeText(context, "File is empty", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val payload = gson.fromJson(json, ExportPayload::class.java)
                val database = HabitDatabase.getDatabase(context)
                database.withTransaction {
                    database.clearAllTables()
                    payload.habits.forEach { database.habitDao().insertHabit(it) }
                    payload.checkIns.forEach { database.checkInDao().insertCheckIn(it) }
                }
                viewModel.refreshData()
                Toast.makeText(context, "Data restored", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun exportData() {
        scope.launch {
            try {
                val database = HabitDatabase.getDatabase(context)
                val habits = database.habitDao().getAllActiveHabitsSync()
                val checkIns = database.checkInDao().getAllCheckIns()
                val payload = ExportPayload(
                    habits = habits,
                    checkIns = checkIns,
                    exportedAt = System.currentTimeMillis(),
                    versionName = appVersion.name,
                    versionCode = appVersion.code
                )
                val json = gson.toJson(payload)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TEXT, json)
                }
                context.startActivity(Intent.createChooser(intent, "Share data"))
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BackgroundLight,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )

                    Box(modifier = Modifier.size(40.dp))
                }
            }
        }

        item {
            HorizontalDivider()
        }

        // (Show Onboarding Again moved to Appearance section)

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            ProfileQuickCard(
                name = preferences.profileName,
                subtitle = preferences.profileEmail,
                onClick = onNavigateToProfile
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Account & Sync Section
        item {
            SectionHeader(
                icon = Icons.Default.Cloud,
                title = "Account & Sync",
                iconColor = Color(0xFF3B82F6)
            )
        }

        item {
            SettingsCard {
                Column {
                    if (authPreferences.isLoggedIn()) {
                        SettingItem(
                            icon = Icons.Default.Person,
                            title = authPreferences.getUserName(),
                            subtitle = authPreferences.getUserEmail()
                        )

                        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                        val lastSync = authPreferences.getLastSyncTime()
                        val lastSyncText = if (lastSync > 0) {
                            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync))
                        } else {
                            "Never"
                        }

                        SettingNavigationItem(
                            icon = Icons.Default.Sync,
                            title = "Sync Now",
                            subtitle = "Last sync: $lastSyncText",
                            onClick = {
                                SyncWorker.triggerManualSync(context)
                                Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                        SettingNavigationItem(
                            icon = Icons.Default.Logout,
                            title = "Logout",
                            subtitle = "Sign out from your account",
                            onClick = {
                                authPreferences.clearAuth()
                                SyncWorker.cancelPeriodicSync(context)
                                onNavigateToLogin()
                            },
                            textColor = AccentError
                        )
                    } else {
                        SettingNavigationItem(
                            icon = Icons.Default.Login,
                            title = "Sign In",
                            subtitle = "Login to sync your data across devices",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionHeader(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                iconColor = Highlight
            )
        }

        item {
            SettingsCard {
                Column {
                    SettingToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Daily Reminders",
                        subtitle = "Get notified for your habits",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            preferences.notificationsEnabled = it
                        }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingToggleItem(
                        icon = Icons.Default.Email,
                        title = "Daily Summary",
                        subtitle = "Evening recap of your progress",
                        checked = dailySummaryEnabled,
                        onCheckedChange = {
                            dailySummaryEnabled = it
                            preferences.dailySummaryEnabled = it
                        }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.DateRange,
                        title = "Reminder Time",
                        subtitle = formatTime(reminderTime),
                        onClick = { showReminderTimePicker = true }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionHeader(
                icon = Icons.Default.Settings,
                title = "Appearance",
                iconColor = Primary
            )
        }

        item {
            SettingsCard {
                Column {
                    SettingNavigationItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Theme",
                        subtitle = themeLabel(themeMode),
                        onClick = { showThemeDialog = true }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Category,
                        title = "Categories",
                        subtitle = "${categoriesData.count { it.isEnabled }} total",
                        onClick = onNavigateToCategories
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Category,
                        title = "Manage Templates",
                        subtitle = "Create & edit custom templates",
                        onClick = onNavigateToManageTemplates
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Star,
                        title = "App Icon",
                        subtitle = "Default",
                        onClick = { Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show() }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Show Onboarding Again",
                        subtitle = "Replay the welcome tutorial",
                        onClick = {
                            val prefs = AppPreferences(context)
                            prefs.isOnboardingCompleted = false

                            // Restart app
                            val intent = context.packageManager
                                .getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionHeader(
                icon = Icons.Default.Lock,
                title = "Data & Privacy",
                iconColor = Color(0xFF8B5CF6)
            )
        }

        item {
            SettingsCard {
                Column {
                    SettingNavigationItem(
                        icon = Icons.Default.Share,
                        title = "Export Data",
                        subtitle = "Download your habits & check-ins",
                        onClick = { showExportDialog = true }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Backup,
                        title = "Backup & Restore",
                        subtitle = "Export or import your data",
                        onClick = { onNavigateToBackup() }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Delete,
                        title = "Delete All Data",
                        subtitle = "Permanently remove everything",
                        onClick = { showDeleteDialog = true },
                        textColor = AccentError
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionHeader(
                icon = Icons.Default.Info,
                title = "About",
                iconColor = Color(0xFF10B981)
            )
        }

        item {
            SettingsCard {
                Column {
                    SettingNavigationItem(
                        icon = Icons.Default.Star,
                        title = "Rate MyHabit",
                        subtitle = "Leave a review on Play Store",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://details?id=${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "Share App",
                        subtitle = "Tell friends about MyHabit",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out MyHabit - Build better habits!")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingNavigationItem(
                        icon = Icons.Default.Email,
                        title = "Send Feedback",
                        subtitle = "Help us improve",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:feedback@myhabit.com")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "${appVersion.name} (Build ${appVersion.code})"
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AccentError,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Delete All Data?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will permanently delete all your habits, check-ins, and settings. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val database = HabitDatabase.getDatabase(context)
                                database.clearAllTables()
                                viewModel.refreshData()
                                Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentError
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Export Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Your habits and check-ins will be exported as a JSON file. You can use this to backup or transfer your data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportData()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeOptionRow(
                        title = "Light",
                        selected = themeMode == ThemeManager.MODE_LIGHT,
                        onClick = {
                            themeMode = ThemeManager.MODE_LIGHT
                            preferences.themeMode = themeMode
                            ThemeManager.saveThemeMode(context, themeMode)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "Dark",
                        selected = themeMode == ThemeManager.MODE_DARK,
                        onClick = {
                            themeMode = ThemeManager.MODE_DARK
                            preferences.themeMode = themeMode
                            ThemeManager.saveThemeMode(context, themeMode)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "System",
                        selected = themeMode == ThemeManager.MODE_SYSTEM,
                        onClick = {
                            themeMode = ThemeManager.MODE_SYSTEM
                            preferences.themeMode = themeMode
                            ThemeManager.saveThemeMode(context, themeMode)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    if (showReminderTimePicker) {
        val parts = remember(reminderTime) { reminderTime.split(":") }
        val timePickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            title = { Text("Reminder Time") },
            text = {
                TimeInput(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF5F5F5),
                        selectorColor = Primary,
                        containerColor = SurfaceLight,
                        periodSelectorBorderColor = BorderLight,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = TextMuted,
                        periodSelectorSelectedContainerColor = Primary,
                        periodSelectorUnselectedContainerColor = Color(0xFFF5F5F5),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = TextMuted,
                        timeSelectorSelectedContainerColor = Primary.copy(alpha = 0.1f),
                        timeSelectorUnselectedContainerColor = Color(0xFFF5F5F5),
                        timeSelectorSelectedContentColor = Primary,
                        timeSelectorUnselectedContentColor = TextMuted
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        reminderTime = newTime
                        preferences.defaultReminderTime = newTime
                        showReminderTimePicker = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup & Restore") },
            text = { Text("Backup will export JSON. Restore will replace your current data.") },
            confirmButton = {
                Button(
                    onClick = {
                        exportData()
                        showBackupDialog = false
                    }
                ) { Text("Backup") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    restoreLauncher.launch(arrayOf("application/json", "text/plain"))
                }) { Text("Restore") }
            }
        )
    }
}



@Composable
fun ProfileQuickCard(
    name: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Composable
fun SettingNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = TextMain
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (textColor == AccentError) AccentError else Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (textColor == AccentError) AccentError.copy(alpha = 0.7f) else TextMuted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = title)
    }
}
