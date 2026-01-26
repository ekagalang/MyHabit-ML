package com.habittracker.ml.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.graphics.Color as AndroidColor
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.ui.theme.*
import com.habittracker.ml.utils.DateUtils
import com.habittracker.ml.utils.StreakCalculator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    var habit by remember { mutableStateOf<Habit?>(null) }
    var checkIns by remember { mutableStateOf<List<CheckIn>>(emptyList()) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var totalCheckIns by remember { mutableStateOf(0) }
    var completionRate by remember { mutableStateOf(0f) }
    var isCheckedInToday by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Load data
    LaunchedEffect(habitId, uiState.todayCheckIns) {
        val habitWithCheckIns = viewModel.getHabitWithCheckIns(habitId)
        if (habitWithCheckIns != null) {
            habit = habitWithCheckIns.habit
            checkIns = habitWithCheckIns.checkIns.sortedByDescending { it.timestamp }
            currentStreak = StreakCalculator.calculateCurrentStreak(habitWithCheckIns.checkIns)
            longestStreak = StreakCalculator.calculateLongestStreak(habitWithCheckIns.checkIns)
            totalCheckIns = habitWithCheckIns.checkIns.size
            completionRate = StreakCalculator.calculateCompletionRate(
                habitWithCheckIns.checkIns,
                habitWithCheckIns.habit.targetFrequency,
                30
            )
            val today = DateUtils.getCurrentDate()
            isCheckedInToday = habitWithCheckIns.checkIns.any { it.date == today }
        }
    }

    if (habit == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            text = habit!!.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        IconButton(
                            onClick = onNavigateToEdit,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = TextMain,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Habit Info Card
            item {
                HabitInfoCard(
                    habit = habit!!,
                    currentStreak = currentStreak
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Future Self Prediction Card
            item {
                FutureSelfCard(
                    currentStreak = currentStreak,
                    completionRate = completionRate
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Stats Cards
            item {
                StatsGrid(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    totalCheckIns = totalCheckIns,
                    completionRate = completionRate
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Calendar Section
            item {
                CalendarSection(
                    checkIns = checkIns,
                    habit = habit!!
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                CheckInActionSection(
                    isCheckedInToday = isCheckedInToday,
                    onCheckIn = {
                        if (!isCheckedInToday) {
                            viewModel.checkInHabit(habitId)
                            isCheckedInToday = true
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // History Section
            item {
                Text(
                    text = "HISTORY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                )
            }

            // Check-in History
            items(checkIns.take(10)) { checkIn ->
                CheckInHistoryItem(checkIn = checkIn)
            }

            if (checkIns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No check-ins yet.\nStart your streak today!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Delete Button
            item {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentError
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentError)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Habit",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Habit?") },
            text = { Text("This action cannot be undone. All check-in history will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habitId)
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = AccentError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HabitInfoCard(
    habit: Habit,
    currentStreak: Int
) {
    val habitColor = getHabitColor(habit)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = getCategoryColor(habit.category).copy(alpha = 0.15f),
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(habit.category),
                        contentDescription = habit.category,
                        tint = getCategoryColor(habit.category),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Category badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = habitColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = habit.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = habitColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Frequency info
            Text(
                text = "DAILY • ${habit.targetFrequency}X PER WEEK",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )

            if (habit.description.isNotEmpty() && habit.description != "No description") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Streak Display
            if (currentStreak > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Streak",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "$currentStreak day streak",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun FutureSelfCard(
    currentStreak: Int,
    completionRate: Float
) {
    val prediction = (completionRate * 100).toInt()
    val targetLevel = when {
        prediction >= 90 -> "Master"
        prediction >= 70 -> "Expert"
        prediction >= 50 -> "Proficient"
        else -> "Beginner"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp
    ) {
        Box {
            // Gradient blur circles
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
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

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 200.dp, y = 100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Highlight.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Insight",
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "INSIGHT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Future Self",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Based on your current streak, you are $prediction% likely to hit $targetLevel status by next Friday.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { /* TODO: Navigate to detailed predictions */ },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Text(
                        text = "View Prediction Details",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(
    currentStreak: Int,
    longestStreak: Int,
    totalCheckIns: Int,
    completionRate: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = currentStreak.toString(),
            label = "Current",
            icon = Icons.Default.Whatshot,
            color = Color(0xFFF59E0B)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            value = longestStreak.toString(),
            label = "Best",
            icon = Icons.Default.Star,
            color = Color(0xFF8B5CF6)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            value = totalCheckIns.toString(),
            label = "Total",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun CalendarSection(
    checkIns: List<CheckIn>,
    habit: Habit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "PROGRESS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceLight,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                MiniCalendar(
                    checkIns = checkIns,
                    habitColor = getHabitColor(habit)
                )
            }
        }
    }
}

@Composable
fun CheckInActionSection(
    isCheckedInToday: Boolean,
    onCheckIn: () -> Unit
) {
    Button(
        onClick = onCheckIn,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCheckedInToday) Color(0xFF10B981) else Primary,
            disabledContainerColor = Color(0xFF10B981)
        ),
        enabled = !isCheckedInToday,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = if (isCheckedInToday) Icons.Default.Check else Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isCheckedInToday) "Completed Today" else "Check-in Now",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class CalendarCell(
    val dayOfMonth: Int?,
    val dateString: String?
)

@Composable
fun MiniCalendar(
    checkIns: List<CheckIn>,
    habitColor: Color
) {
    val today = DateUtils.getCurrentDate()
    val checkInDates = remember(checkIns) { checkIns.map { it.date }.toSet() }
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var monthOffset by remember { mutableStateOf(0) }
    val displayCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }

    val firstDayOfWeek = displayCalendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = 42

    val cells = remember(displayCalendar.timeInMillis, checkInDates) {
        List(totalCells) { index ->
            val dayOfMonth = index - (firstDayOfWeek - 1) + 1
            if (dayOfMonth in 1..daysInMonth) {
                val cellCalendar = displayCalendar.clone() as Calendar
                cellCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                CalendarCell(
                    dayOfMonth = dayOfMonth,
                    dateString = dateFormatter.format(cellCalendar.time)
                )
            } else {
                CalendarCell(dayOfMonth = null, dateString = null)
            }
        }
    }

    // Month/Year header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthFormatter.format(displayCalendar.time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { monthOffset -= 1 }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = TextMuted
                )
            }
            IconButton(onClick = { monthOffset += 1 }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = TextMuted
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Day headers
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (week in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val cell = cells[week * 7 + dayIndex]
                    if (cell.dayOfMonth == null || cell.dateString == null) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                        )
                    } else {
                        val isCheckedIn = checkInDates.contains(cell.dateString)
                        val isToday = cell.dateString == today
                        val backgroundColor = when {
                            isCheckedIn -> habitColor
                            isToday -> Primary.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(backgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cell.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCheckedIn) Color.White else TextMain,
                                    fontWeight = if (isCheckedIn || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInHistoryItem(checkIn: CheckIn) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check icon
            Surface(
                shape = CircleShape,
                color = Color(0xFF10B981).copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(checkIn.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "Completed at ${checkIn.completedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                if (!checkIn.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${checkIn.note}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted
                )
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "health" -> CategoryHealth
        "mindfulness" -> CategoryMindfulness
        "productivity" -> CategoryProductivity
        "fitness" -> CategoryLearning
        else -> Primary
    }
}

fun getHabitColor(habit: Habit): Color {
    return try {
        Color(AndroidColor.parseColor(habit.color))
    } catch (e: Exception) {
        getCategoryColor(habit.category)
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "health" -> Icons.Default.FavoriteBorder
        "mindfulness" -> Icons.Default.SelfImprovement
        "productivity" -> Icons.Default.TaskAlt
        "fitness" -> Icons.AutoMirrored.Filled.DirectionsRun
        else -> Icons.Default.Star
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

