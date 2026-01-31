package com.habittracker.ml.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.data.local.preferences.CustomCategory
import com.habittracker.ml.ui.theme.*
import com.habittracker.ml.ui.habits.HabitsViewModel
import com.habittracker.ml.ui.habits.HabitsViewModelFactory
import com.habittracker.ml.data.repository.HabitRepository
import com.habittracker.ml.data.local.database.HabitDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HabitCategory(
    val id: String,
    val name: String,
    val color: Color
)

enum class FrequencyType {
    DAILY, WEEKLY
}

enum class DayOfWeek(val initial: String, val fullName: String) {
    SUNDAY("S", "Sunday"),
    MONDAY("M", "Monday"),
    TUESDAY("T", "Tuesday"),
    WEDNESDAY("W", "Wednesday"),
    THURSDAY("T", "Thursday"),
    FRIDAY("F", "Friday"),
    SATURDAY("S", "Saturday")
}

fun defaultHabitCategories(): List<HabitCategory> {
    return listOf(
        HabitCategory("health", "Health", CategoryHealth),
        HabitCategory("mindfulness", "Mindfulness", CategoryMindfulness),
        HabitCategory("productivity", "Productivity", CategoryProductivity),
        HabitCategory("fitness", "Fitness", CategoryLearning)
    )
}

fun List<CustomCategory>.toHabitCategories(): List<HabitCategory> {
    return map {
        HabitCategory(
            id = it.id,
            name = it.name,
            color = Color(AndroidColor.parseColor(it.colorHex))
        )
    }
}

fun categoryToHex(category: HabitCategory): String {
    return when (category.id) {
        "health" -> "#10B981"
        "mindfulness" -> "#8B5CF6"
        "productivity" -> "#6366F1"
        "fitness" -> "#F59E0B"
        else -> String.format("#%06X", 0xFFFFFF and category.color.toArgb())
    }
}

@Composable
fun AddHabitScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    templateId: String? = null,
    templateName: String? = null,
    templateDesc: String? = null,
    templateCategory: String? = null,
    templateIcon: String? = null,
    templateFreq: String? = null,
    templateTime: String? = null,
    viewModel: HabitsViewModel = viewModel(
        factory = HabitsViewModelFactory(
            HabitRepository(
                HabitDatabase.getDatabase(LocalContext.current).habitDao(),
                HabitDatabase.getDatabase(LocalContext.current).checkInDao(),
                HabitDatabase.getDatabase(LocalContext.current).habitTemplateDao()
            ),
            LocalContext.current
        )
    )
) {
    var habitName by remember { mutableStateOf(templateName ?: "") }
    var habitDescription by remember { mutableStateOf(templateDesc ?: "") }
    var selectedIcon by remember { mutableStateOf(templateIcon ?: "💪") }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var selectedDays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { 
        mutableStateOf(
            if (templateTime.isNullOrBlank()) "08:00" else templateTime
        ) 
    }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    var categoriesData by remember { mutableStateOf(preferences.getCategories()) }

    LaunchedEffect(Unit) {
        categoriesData = preferences.getCategories()
    }

    val categories = remember(categoriesData) {
        categoriesData.filter { it.isEnabled }.toHabitCategories()
    }

    // Initialize selectedCategory based on templateCategory or default
    var selectedCategory by remember(categories) {
        mutableStateOf(
            if (templateCategory != null) {
                categories.find { it.id == templateCategory || it.name == templateCategory }
                    ?: categories.firstOrNull()
            } else {
                categories.firstOrNull { it.id == "health" } ?: categories.firstOrNull()
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
            // Header
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
                            text = "Create Habit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        Box(modifier = Modifier.size(40.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Habit Name
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "HABIT NAME",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceLight,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = habitName,
                                onValueChange = {
                                    habitName = it
                                    showError = false
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "e.g. Morning Run",
                                        color = Color(0xFFD1D5DB),
                                        fontSize = 18.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceLight,
                                    unfocusedContainerColor = SurfaceLight,
                                    disabledContainerColor = SurfaceLight,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextMain,
                                    unfocusedTextColor = TextMain
                                ),
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true
                            )

                            // Edit icon
                            if (habitName.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                    }

                    if (showError && habitName.isEmpty()) {
                        Text(
                            text = "Please enter a habit name",
                            color = AccentError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Description
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceLight,
                        shadowElevation = 2.dp
                    ) {
                        TextField(
                            value = habitDescription,
                            onValueChange = { habitDescription = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            placeholder = {
                                Text(
                                    "What motivates you to do this?",
                                    color = Color(0xFFD1D5DB)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLight,
                                unfocusedContainerColor = SurfaceLight,
                                disabledContainerColor = SurfaceLight,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            maxLines = 3
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Category Selection
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.size) { index ->
                            CategoryChip(
                                category = categories[index],
                                isSelected = selectedCategory == categories[index],
                                onClick = { selectedCategory = categories[index] }
                            )
                        }

                        item {
                            // Add custom category button
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceLight,
                                border = BorderStroke(2.dp, BorderLight.copy(alpha = 0.8f)),
                                shadowElevation = 1.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(
                                            context,
                                            "Manage categories in Settings",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add category",
                                        tint = Primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (showError && selectedCategory == null) {
                        Text(
                            text = "Please select a category",
                            color = AccentError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Frequency Toggle
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "FREQUENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            FrequencyToggleButton(
                                text = "Daily",
                                isSelected = frequencyType == FrequencyType.DAILY,
                                onClick = {
                                    frequencyType = FrequencyType.DAILY
                                    selectedDays = DayOfWeek.values().toSet()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            FrequencyToggleButton(
                                text = "Weekly",
                                isSelected = frequencyType == FrequencyType.WEEKLY,
                                onClick = {
                                    frequencyType = FrequencyType.WEEKLY
                                    if (selectedDays.isEmpty() || selectedDays.size == DayOfWeek.values().size) {
                                        selectedDays = defaultWeeklyDays(5)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Day Selector for Weekly
                    AnimatedVisibility(
                        visible = frequencyType == FrequencyType.WEEKLY,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Repeat on",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DayOfWeek.values().forEach { day ->
                                    DayCheckbox(
                                        day = day,
                                        isSelected = selectedDays.contains(day),
                                        onClick = {
                                            selectedDays = if (selectedDays.contains(day)) {
                                                selectedDays - day
                                            } else {
                                                selectedDays + day
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            // Reminder Card
            item {
                ReminderCard(
                    reminderEnabled = reminderEnabled,
                    onReminderToggle = { reminderEnabled = it },
                    reminderTime = reminderTime,
                    onTimeChange = { reminderTime = it }
                )
            }

            // Template Button
            if (templateId == null) {
                item {
                    OutlinedButton(
                        onClick = onNavigateToTemplates,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.LibraryBooks, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Use Template")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Save Button
            item {
                Button(
                    onClick = {
                        if (habitName.isEmpty() || selectedCategory == null) {
                            showError = true
                        } else {
                            val targetFrequency = if (frequencyType == FrequencyType.DAILY) {
                                7
                            } else {
                                selectedDays.size.coerceAtLeast(1)
                            }
                            val repeatDays = if (frequencyType == FrequencyType.WEEKLY) {
                                serializeRepeatDays(selectedDays)
                            } else {
                                null
                            }
                            val colorHex = categoriesData.firstOrNull { it.id == selectedCategory?.id }?.colorHex
                                ?: categoryToHex(selectedCategory!!)
                            val habit = Habit(
                                name = habitName,
                                description = habitDescription.ifEmpty { "No description" },
                                category = selectedCategory!!.id,
                                targetFrequency = targetFrequency,
                                reminderEnabled = reminderEnabled,
                                reminderTime = if (reminderEnabled) reminderTime else null,
                                repeatDays = repeatDays,
                                icon = when (selectedCategory!!.id) {
                                    "health" -> "💪"
                                    "mindfulness" -> "🧘"
                                    "productivity" -> "🎯"
                                    "fitness" -> "🏃"
                                    else -> "⭐"
                                },
                                color = colorHex
                            )

                            viewModel.insertHabit(habit) {
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    reminderTime: String,
    onTimeChange: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val parts = reminderTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Highlight.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Highlight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Set Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                        Text(
                            text = "Get notified to stay on track",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE5E7EB)
                    )
                )
            }

            AnimatedVisibility(
                visible = reminderEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = BorderLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reminder Time",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FAFB),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.clickable {
                                showTimePicker = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceLight
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(18.dp)
                                    )
                                }

                                Text(
                                    text = formatTime(reminderTime),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                color = SurfaceLight,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Time",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showTimePicker = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = TextMuted)
                        }

                        Button(
                            onClick = {
                                onTimeChange(
                                    String.format(
                                        "%02d:%02d",
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                )
                                showTimePicker = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: HabitCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) category.color else SurfaceLight,
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        border = if (!isSelected) BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f)) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = when (category.id) {
                        "health" -> Icons.Default.FavoriteBorder
                        "mindfulness" -> Icons.Default.FavoriteBorder
                        "productivity" -> Icons.Default.Star
                        "fitness" -> Icons.Default.FavoriteBorder
                        else -> Icons.Default.Star
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextMuted
            )
        }
    }
}

@Composable
fun FrequencyToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SurfaceLight else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextMain else TextMuted
            )
        }
    }
}

@Composable
fun DayCheckbox(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Surface(
        modifier = Modifier
            .size(36.dp)
            .scale(scale),
        shape = CircleShape,
        color = if (isSelected) Highlight else Color(0xFFF5F5F5),
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = day.initial,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextMuted
            )
        }
    }
}

fun formatTime(time: String): String {
    return try {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
    } catch (e: Exception) {
        time
    }
}
