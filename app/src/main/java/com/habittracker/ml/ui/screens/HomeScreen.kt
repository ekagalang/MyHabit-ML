package com.habittracker.ml.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import androidx.compose.ui.text.style.TextAlign
import com.habittracker.ml.ui.components.MoodPicker
import com.habittracker.ml.ui.components.EnergyLevelPicker
import com.habittracker.ml.ui.components.StressLevelPicker
import com.habittracker.ml.ui.components.LocationPicker
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.habittracker.ml.ui.components.CheckInSuccessAnimation
import com.habittracker.ml.ui.components.StreakMilestoneDialog
import com.habittracker.ml.utils.StreakCalculator

@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToEditHabit: (Long) -> Unit = {},
    onNavigateToHabitDetail: (Long) -> Unit = {},
    onNavigateToWorkspace: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToMLInsights: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCheckInDialog by remember { mutableStateOf(false) }
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var selectedHabitForAnimation by remember { mutableStateOf<Habit?>(null) }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var streakMilestone by remember { mutableStateOf(0) }
    var pendingMilestoneHabitId by remember { mutableStateOf<Long?>(null) }

    // Refresh every time screen is displayed
    LaunchedEffect(key1 = true) {
        viewModel.refreshData()
    }

    // Refresh when screen becomes visible
    DisposableEffect(Unit) {
        onDispose {
            viewModel.refreshData()
        }
    }

    LaunchedEffect(pendingMilestoneHabitId) {
        val habitId = pendingMilestoneHabitId ?: return@LaunchedEffect
        val habitWithCheckIns = viewModel.getHabitWithCheckIns(habitId)
        val checkIns = habitWithCheckIns?.checkIns ?: emptyList()
        val currentStreak = StreakCalculator.calculateCurrentStreak(checkIns)
        if (currentStreak > 0 &&
            (currentStreak % 7 == 0 || currentStreak == 30 || currentStreak == 100)
        ) {
            streakMilestone = currentStreak
            showMilestoneDialog = true
        }
        pendingMilestoneHabitId = null
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Surface(
                color = BackgroundLight.copy(alpha = 0.96f),
                shadowElevation = 2.dp
            ) {
                Column {
                    DashboardHeader(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToNotifications = onNavigateToNotifications
                    )
                    HorizontalDivider(
                        color = BorderLight.copy(alpha = 0.6f),
                        thickness = 1.dp
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // Date selector
            item {
                DateSelector()
            }

            // Progress Card (Glassmorphism)
            item {
                ProgressCard(
                    completedToday = uiState.completedToday,
                    totalHabits = uiState.totalHabits,
                    totalStreak = uiState.totalStreak
                )
            }

            // ML Insights Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { onNavigateToMLInsights() },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 4.dp
                ) {
                    Box {
                        // Gradient circles
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = (-30).dp, y = (-30).dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Primary.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Highlight.copy(alpha = 0.2f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "🤖", fontSize = 28.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "✨ AI POWERED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        letterSpacing = 1.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "ML Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "View predictions & analysis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // My Habits Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MY HABITS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )

                    TextButton(
                        onClick = onNavigateToWorkspace,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Habits List
            if (uiState.habits.isEmpty()) {
                item {
                    EmptyState(onAddHabit = onNavigateToAddHabit)
                }
            } else {
                items(uiState.habits) { habit ->
                    val habitWithCheckIns by produceState<com.habittracker.ml.data.local.entities.HabitWithCheckIns?>(
                        initialValue = null,
                        habit.id,
                        uiState.todayCheckIns
                    ) {
                        value = viewModel.getHabitWithCheckIns(habit.id)
                    }

                    ModernHabitCard(
                        habit = habit,
                        streak = habitWithCheckIns?.let {
                            StreakCalculator.calculateCurrentStreak(it.checkIns)
                        } ?: 0,
                        isCheckedIn = viewModel.isHabitCheckedInToday(habit.id),
                        onCheckIn = {
                            selectedHabit = habit
                            showCheckInDialog = true
                        },
                        onEdit = { onNavigateToEditHabit(habit.id) },
                        onClick = { onNavigateToHabitDetail(habit.id) }
                    )
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Success Animation Overlay
        if (showSuccessAnimation && selectedHabitForAnimation != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                CheckInSuccessAnimation(
                    habitName = selectedHabitForAnimation?.name ?: "",
                    onAnimationEnd = {
                        showSuccessAnimation = false
                        selectedHabitForAnimation = null
                    }
                )
            }
        }

// Milestone Dialog
        if (showMilestoneDialog) {
            StreakMilestoneDialog(
                streak = streakMilestone,
                habitName = selectedHabitForAnimation?.name ?: "",
                onDismiss = { showMilestoneDialog = false }
            )
        }
    }

    val dialogHabit = selectedHabit
    if (showCheckInDialog && dialogHabit != null) {
        var note by remember { mutableStateOf("") }
        var mood by remember { mutableStateOf<String?>(null) }
        var energyLevel by remember { mutableStateOf<Int?>(null) }
        var stressLevel by remember { mutableStateOf<Int?>(null) }
        var location by remember { mutableStateOf<String?>(null) }
        var showAdvancedOptions by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showCheckInDialog = false
                selectedHabit = null
            },
            title = {
                Text(
                    text = "Check-in: ${dialogHabit.name}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mood Picker
                    MoodPicker(
                        selectedMood = mood,
                        onMoodSelected = { mood = it }
                    )

                    HorizontalDivider()

                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Add note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Advanced Options Toggle
                    TextButton(
                        onClick = { showAdvancedOptions = !showAdvancedOptions },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showAdvancedOptions) "Hide advanced options" else "Show advanced options",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            imageVector = if (showAdvancedOptions)
                                Icons.Default.ExpandLess
                            else
                                Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Advanced Options
                    if (showAdvancedOptions) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            EnergyLevelPicker(
                                selectedLevel = energyLevel,
                                onLevelSelected = { energyLevel = it }
                            )

                            HorizontalDivider()

                            StressLevelPicker(
                                selectedLevel = stressLevel,
                                onLevelSelected = { stressLevel = it }
                            )

                            HorizontalDivider()

                            LocationPicker(
                                selectedLocation = location,
                                onLocationSelected = { location = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date())
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date())

                        // Calculate if late
                        val reminderTime = dialogHabit.reminderTime ?: "08:00"
                        val isLate = isCheckInLate(currentTime, reminderTime)
                        val minutesLate = calculateMinutesLate(currentTime, reminderTime)

                        val checkIn = CheckIn(
                            habitId = dialogHabit.id,
                            date = currentDate,
                            completedAt = currentTime,
                            note = note.ifBlank { null },
                            mood = mood,
                            isLate = isLate,
                            minutesLate = if (isLate) minutesLate else 0,
                            energyLevel = energyLevel,
                            stressLevel = stressLevel,
                            location = location,
                            weather = null // Can add weather API later
                        )

                        viewModel.checkInHabit(checkIn)
                        showCheckInDialog = false
                        selectedHabit = null

                        // Show success animation
                        showSuccessAnimation = true
                        selectedHabitForAnimation = dialogHabit

                        // Check for streak milestone
                        pendingMilestoneHabitId = dialogHabit.id
                    }
                ) {
                    Text("Check-in")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCheckInDialog = false
                        selectedHabit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DashboardHeader(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile + Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with online indicator
                Box(
                    modifier = Modifier.clickable(onClick = onNavigateToProfile)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, Primary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "😊",
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Online indicator
                    Surface(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = Primary,
                        border = BorderStroke(2.dp, BackgroundLight)
                    ) {}
                }

                Column {
                    Text(
                        text = greeting.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Alex",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextMain,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notifications
            Box(
                modifier = Modifier.clickable(onClick = onNavigateToNotifications)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceLight,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextMain,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Notification badge
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp),
                    shape = CircleShape,
                    color = Highlight
                ) {}
            }
        }
    }
}

@Composable
fun DateSelector() {
    val days = remember {
        val today = Calendar.getInstance()
        (0..5).map { offset ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, offset - 2) // -2 to start from 2 days ago
            DateItem(
                dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time),
                dayNumber = calendar.get(Calendar.DAY_OF_MONTH),
                isToday = offset == 2 // Middle one is today
            )
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(days) { index, day ->
            DateCard(
                day = day,
                isToday = index == 2
            )
        }
    }
}

data class DateItem(val dayName: String, val dayNumber: Int, val isToday: Boolean)

@Composable
fun DateCard(day: DateItem, isToday: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isToday) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Surface(
        modifier = Modifier
            .width(60.dp)
            .height(84.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        color = if (isToday) Primary else SurfaceLight,
        shadowElevation = if (isToday) 8.dp else 2.dp,
        border = if (isToday) BorderStroke(2.dp, Primary.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) Color.White.copy(alpha = 0.9f) else TextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = if (isToday) Color.White else TextMain,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier.size(6.dp),
                shape = CircleShape,
                color = if (isToday) Color.White else Primary.copy(alpha = 0.3f)
            ) {}
        }
    }
}

@Composable
fun ProgressCard(
    completedToday: Int,
    totalHabits: Int,
    totalStreak: Int
) {
    val progress = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f
    val percentage = (progress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Background blurs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(32.dp))
        ) {
            // Dark gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1a1c1e),
                                Color(0xFF2c2e30)
                            )
                        )
                    )
            )

            // Blur effects
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(Primary.copy(alpha = 0.25f), CircleShape)
                    .blur(60.dp)
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 40.dp)
                    .background(Highlight.copy(alpha = 0.3f), CircleShape)
                    .blur(60.dp)
            )
        }

        // Content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Daily Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Primary.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "✨ Nice work",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "You're on a roll",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Keep the streak alive today",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "$completedToday of $totalHabits completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Circular progress
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = Primary,
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
}

@Composable
fun ModernHabitCard(
    habit: Habit,
    streak: Int,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCheckedIn) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val categoryColor = when (habit.category) {
        "health" -> CategoryHealth
        "productivity" -> CategoryProductivity
        "learning" -> CategoryLearning
        "mindfulness" -> CategoryMindfulness
        "social" -> CategorySocial
        else -> Primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp,
        border = if (isCheckedIn) BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top row: Category badge + Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = habit.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Habit",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular streak indicator
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { min(streak / 30f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = categoryColor,
                            strokeWidth = 3.dp,
                            trackColor = BorderLight
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = streak.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMain,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "days",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Habit info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isCheckedIn) {
                            Text(
                                text = "✓ Completed today!",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentSuccess,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "${habit.icon} ${habit.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Check button
                Surface(
                    modifier = Modifier
                        .requiredSize(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            enabled = !isCheckedIn,
                            onClick = onCheckIn
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCheckedIn) categoryColor else categoryColor.copy(alpha = 0.1f),
                    border = BorderStroke(
                        2.dp,
                        if (isCheckedIn) categoryColor else BorderLight
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCheckedIn) "✓" else "○",
                            fontSize = 26.sp,
                            color = if (isCheckedIn) Color.White else categoryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )

        Text(
            text = "Tap the + button below to create your first habit!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

private fun isCheckInLate(currentTime: String, reminderTime: String): Boolean {
    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val current = format.parse(currentTime)?.time ?: 0
        val reminder = format.parse(reminderTime)?.time ?: 0
        current > reminder + (30 * 60 * 1000) // 30 minutes grace period
    } catch (e: Exception) {
        false
    }
}

private fun calculateMinutesLate(currentTime: String, reminderTime: String): Int {
    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val current = format.parse(currentTime)?.time ?: 0
        val reminder = format.parse(reminderTime)?.time ?: 0
        val diff = current - reminder
        if (diff > 0) (diff / (1000 * 60)).toInt() else 0
    } catch (e: Exception) {
        0
    }
}
