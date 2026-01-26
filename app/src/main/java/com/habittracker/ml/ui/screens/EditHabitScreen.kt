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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.data.local.preferences.AppPreferences
import com.habittracker.ml.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun EditHabitScreen(
    habitId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<HabitCategory?>(null) }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var reminderTime by remember { mutableStateOf("08:00") }
    var showError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentHabit by remember { mutableStateOf<Habit?>(null) }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { AppPreferences(context) }
    var categoriesData by remember { mutableStateOf(preferences.getCategories()) }

    LaunchedEffect(Unit) {
        categoriesData = preferences.getCategories()
    }

    val categories = remember(categoriesData, currentHabit?.category) {
        val enabled = categoriesData.filter { it.isEnabled }
        val selected = categoriesData.firstOrNull { it.id == currentHabit?.category }
        val combined = if (selected != null && !selected.isEnabled) enabled + selected else enabled
        combined.distinctBy { it.id }.toHabitCategories()
    }

    // Load habit data
    LaunchedEffect(habitId) {
        scope.launch {
            val habit = viewModel.getHabit(habitId)
            if (habit != null) {
                currentHabit = habit
                habitName = habit.name
                habitDescription = habit.description
                selectedCategory = categories.find { it.id == habit.category }
                val storedDays = parseRepeatDays(habit.repeatDays)
                frequencyType = if (storedDays.isNotEmpty()) {
                    FrequencyType.WEEKLY
                } else if (habit.targetFrequency >= 7) {
                    FrequencyType.DAILY
                } else {
                    FrequencyType.WEEKLY
                }
                reminderEnabled = habit.reminderEnabled
                reminderTime = habit.reminderTime ?: "08:00"
                selectedDays = when {
                    frequencyType == FrequencyType.DAILY -> DayOfWeek.values().toSet()
                    storedDays.isNotEmpty() -> storedDays
                    else -> defaultWeeklyDays(habit.targetFrequency)
                }

                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    } else {
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
                            text = "Edit Habit",
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
                                        selectedDays = defaultWeeklyDays(currentHabit?.targetFrequency ?: 5)
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
                EditReminderCard(
                    reminderEnabled = reminderEnabled,
                    onReminderToggle = { reminderEnabled = it },
                    reminderTime = reminderTime,
                    onTimeChange = { reminderTime = it }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Update Button
            item {
                Button(
                    onClick = {
                        if (habitName.isEmpty() || selectedCategory == null) {
                            showError = true
                        } else {
                            currentHabit?.let { habit ->
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
                                val updatedHabit = habit.copy(
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

                                viewModel.updateHabit(updatedHabit) {
                                    onNavigateBack()
                                }
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
                        text = "Update Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderCard(
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    reminderTime: String,
    onTimeChange: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val parts = reminderTime.split(":")
    val timePickerState = rememberTimePickerState(
        initialHour = parts[0].toIntOrNull() ?: 8,
        initialMinute = parts[1].toIntOrNull() ?: 0,
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
