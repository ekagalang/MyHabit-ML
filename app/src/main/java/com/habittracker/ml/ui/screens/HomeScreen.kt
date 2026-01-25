package com.habittracker.ml.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.Habit

// --- DEFINISI WARNA (DITAMBAHKAN KEMBALI AGAR TIDAK ERROR) ---
private val AppPrimary = Color(0xFF2ED1A2)
private val AppPrimaryDark = Color(0xFF24b890)
private val AppHighlight = Color(0xFF703EFF)
private val AppBackgroundLight = Color(0xFFF8FAFD)
private val AppSurfaceDark = Color(0xFF1E1E1E)
private val AppTextMain = Color(0xFF1A1C1E)
private val AppTextMuted = Color(0xFF73777F)

@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToHabitDetail: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold hanya untuk FAB. Navigasi bawah ada di MainScreen.
    Scaffold(
        containerColor = AppBackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddHabit,
                containerColor = AppPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    // Offset dikurangi karena navbar overlay
                    .offset(y = (-80).dp)
                    .shadow(elevation = 10.dp, spotColor = AppPrimary, shape = RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                // Padding bawah agar konten tidak tertutup Navbar di MainScreen
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // 1. Header Section
                item {
                    TopHeaderSection()
                }

                // 2. Calendar Strip
                item {
                    CalendarStripSection()
                }

                // 3. Daily Progress Hero Card
                item {
                    DailyProgressCard(
                        completedCount = uiState.completedToday,
                        totalCount = uiState.totalHabits.takeIf { it > 0 } ?: 5
                    )
                }

                // 4. Section Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Habits",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTextMain
                        )
                        Text(
                            text = "Manage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 5. Habits List
                if (uiState.habits.isEmpty()) {
                    item {
                        EmptyStateCompact(onAddHabit = onNavigateToAddHabit)
                    }
                } else {
                    items(uiState.habits) { habit ->
                        ModernHabitCard(
                            habit = habit,
                            isCheckedIn = viewModel.isHabitCheckedInToday(habit.id),
                            onCheckIn = { viewModel.checkInHabit(habit.id) },
                            onClick = { onNavigateToHabitDetail(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

// --- KOMPONEN UI ---

@Composable
fun TopHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .border(2.dp, AppPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("A", fontWeight = FontWeight.Bold, color = AppTextMain)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(AppPrimary, CircleShape)
                        .border(2.dp, AppBackgroundLight, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Good Morning",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Alex",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTextMain
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { }
                .background(Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = AppTextMain
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppHighlight, CircleShape)
                    .border(1.dp, AppBackgroundLight, CircleShape)
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
            )
        }
    }
}

@Composable
fun CalendarStripSection() {
    val days = listOf("Mon" to "12", "Tue" to "13", "Wed" to "14", "Thu" to "15", "Fri" to "16", "Sat" to "17")
    val selectedIndex = 2 // Wed 14

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(days.size) { index ->
            val isSelected = index == selectedIndex
            val (dayName, dateNum) = days[index]

            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AppPrimary else Color.White)
                    .shadow(
                        elevation = if (isSelected) 8.dp else 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = if (isSelected) AppPrimary.copy(alpha = 0.5f) else Color.Transparent
                    )
                    .clickable { },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else AppTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateNum,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else AppTextMain
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                } else if (index < selectedIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(6.dp).background(AppPrimary.copy(alpha = 0.2f), CircleShape))
                }
            }
        }
    }
}

@Composable
fun DailyProgressCard(completedCount: Int, totalCount: Int) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(AppSurfaceDark)
    ) {
        // Decorative Blurs
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .size(150.dp)
                .background(Brush.radialGradient(listOf(AppPrimary.copy(alpha = 0.3f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 20.dp)
                .size(150.dp)
                .background(Brush.radialGradient(listOf(AppHighlight.copy(alpha = 0.3f), Color.Transparent)))
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Progress",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You're on fire! 🔥",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$completedCount of $totalCount completed",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            // Circular Progress
            Box(contentAlignment = Alignment.Center) {
                CircularProgress(
                    percentage = progress,
                    radius = 40.dp,
                    strokeWidth = 8.dp,
                    color = AppPrimary
                )
                Text(
                    text = "$percentage%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ModernHabitCard(
    habit: Habit,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
    onClick: () -> Unit
) {
    val categoryColor = when(habit.name.length % 3) {
        0 -> Color(0xFF10B981) // Hijau
        1 -> Color(0xFFF97316) // Orange
        else -> AppHighlight    // Ungu
    }

    val categoryName = when(habit.name.length % 3) {
        0 -> "Health"
        1 -> "Mindfulness"
        else -> "Productivity"
    }

    val categoryBg = categoryColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(elevation = 4.dp, spotColor = Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(categoryBg, RoundedCornerShape(100))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoryName.uppercase(),
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AppTextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgress(
                        percentage = 0.7f,
                        radius = 28.dp,
                        strokeWidth = 3.dp,
                        color = if (isCheckedIn) AppTextMuted.copy(alpha=0.3f) else categoryColor,
                        trackColor = Color.Gray.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (habit.id.toInt() % 10 + 1).toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AppTextMain
                        )
                        Text(
                            text = "days",
                            fontSize = 9.sp,
                            color = AppTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if(habit.id % 2 == 0L) Icons.Default.LocalFireDepartment else Icons.Outlined.BarChart,
                            contentDescription = null,
                            tint = if(habit.id % 2 == 0L) Color(0xFFF97316) else AppHighlight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = habit.description.ifEmpty { "15 mins" },
                            fontSize = 12.sp,
                            color = AppTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCheckedIn) categoryColor else AppBackgroundLight)
                        .border(
                            1.dp,
                            if (isCheckedIn) categoryColor else Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onCheckIn() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check",
                        tint = if (isCheckedIn) Color.White else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCompact(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
            .alpha(0.6f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.height(4.dp).width(120.dp).background(Color.Gray.copy(alpha=0.2f), CircleShape))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keep going, you're doing great!",
            fontSize = 12.sp,
            color = AppTextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CircularProgress(
    percentage: Float,
    radius: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    color: Color,
    trackColor: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = Modifier.size(radius * 2)) {
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360 * percentage,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}