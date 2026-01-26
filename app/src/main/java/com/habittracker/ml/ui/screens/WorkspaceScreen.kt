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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.ml.data.local.entities.CheckIn
import com.habittracker.ml.data.local.entities.Habit
import com.habittracker.ml.ui.theme.*
import com.habittracker.ml.utils.StreakCalculator

enum class WorkspaceFilter {
    ALL, HEALTH, MINDFULNESS, PRODUCTIVITY, FITNESS
}

enum class WorkspaceSort {
    RECENT, NAME, STREAK
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WorkspaceScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToHabitDetail: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedFilter by remember { mutableStateOf(WorkspaceFilter.ALL) }
    var selectedSort by remember { mutableStateOf(WorkspaceSort.RECENT) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var habitCheckInsMap by remember { mutableStateOf<Map<Long, List<CheckIn>>>(emptyMap()) }

    LaunchedEffect(uiState.habits, uiState.todayCheckIns) {
        val allHabitsWithCheckIns = viewModel.getAllHabitsWithCheckIns()
        habitCheckInsMap = allHabitsWithCheckIns.associate { it.habit.id to it.checkIns }
    }

    // Filter and sort habits
    val filteredHabits = remember(uiState.habits, selectedFilter, selectedSort, searchQuery) {
        var filtered = uiState.habits

        // Apply search
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply category filter
        if (selectedFilter != WorkspaceFilter.ALL) {
            filtered = filtered.filter {
                it.category == selectedFilter.name.lowercase()
            }
        }

        // Apply sorting
        when (selectedSort) {
            WorkspaceSort.RECENT -> filtered.sortedByDescending { it.createdAt }
            WorkspaceSort.NAME -> filtered.sortedBy { it.name }
            WorkspaceSort.STREAK -> filtered // TODO: Sort by streak
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BackgroundLight,
                    shadowElevation = 0.dp
                ) {
                    Column {
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Workspace",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                                Text(
                                    text = "${filteredHabits.size} habits",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }

                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Filter",
                                    tint = TextMain,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Search Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceLight,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextMain
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search habits...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextMuted
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Category Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        text = "All",
                        count = uiState.habits.size,
                        isSelected = selectedFilter == WorkspaceFilter.ALL,
                        onClick = { selectedFilter = WorkspaceFilter.ALL }
                    )

                    FilterChip(
                        text = "Health",
                        count = uiState.habits.count { it.category == "health" },
                        isSelected = selectedFilter == WorkspaceFilter.HEALTH,
                        onClick = { selectedFilter = WorkspaceFilter.HEALTH },
                        color = CategoryHealth
                    )

                    FilterChip(
                        text = "Mind",
                        count = uiState.habits.count { it.category == "mindfulness" },
                        isSelected = selectedFilter == WorkspaceFilter.MINDFULNESS,
                        onClick = { selectedFilter = WorkspaceFilter.MINDFULNESS },
                        color = CategoryMindfulness
                    )

                    FilterChip(
                        text = "Work",
                        count = uiState.habits.count { it.category == "productivity" },
                        isSelected = selectedFilter == WorkspaceFilter.PRODUCTIVITY,
                        onClick = { selectedFilter = WorkspaceFilter.PRODUCTIVITY },
                        color = CategoryProductivity
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Sort Options
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SORT BY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortButton(
                            text = "Recent",
                            isSelected = selectedSort == WorkspaceSort.RECENT,
                            onClick = { selectedSort = WorkspaceSort.RECENT }
                        )

                        SortButton(
                            text = "Name",
                            isSelected = selectedSort == WorkspaceSort.NAME,
                            onClick = { selectedSort = WorkspaceSort.NAME }
                        )

                        SortButton(
                            text = "Streak",
                            isSelected = selectedSort == WorkspaceSort.STREAK,
                            onClick = { selectedSort = WorkspaceSort.STREAK }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Habits List
            if (filteredHabits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "No habits found"
                                else
                                    "No habits yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Try a different search"
                                else
                                    "Tap + to create your first habit",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
                    val checkIns = habitCheckInsMap[habit.id].orEmpty()
                    WorkspaceHabitCard(
                        habit = habit,
                        checkIns = checkIns,
                        onClick = { onNavigateToHabitDetail(habit.id) },
                        onCheckIn = {
                            viewModel.checkInHabit(habit.id)
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddHabit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Habit",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = SurfaceLight
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkspaceFilter.values().forEach { filter ->
                        FilterOption(
                            text = filter.name,
                            isSelected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                                showFilterSheet = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SORT BY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkspaceSort.values().forEach { sort ->
                        FilterOption(
                            text = sort.name,
                            isSelected = selectedSort == sort,
                            onClick = {
                                selectedSort = sort
                                showFilterSheet = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WorkspaceHabitCard(
    habit: Habit,
    checkIns: List<com.habittracker.ml.data.local.entities.CheckIn>,
    onClick: () -> Unit,
    onCheckIn: () -> Unit
) {
    val currentStreak = StreakCalculator.calculateCurrentStreak(checkIns)
    val isCheckedInToday = checkIns.any {
        it.date == com.habittracker.ml.utils.DateUtils.getCurrentDate()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = getCategoryColor(habit.category).copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = habit.icon,
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getCategoryColor(habit.category).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = habit.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = getCategoryColor(habit.category),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    if (currentStreak > 0) {
                        Text(
                            text = "🔥 $currentStreak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${habit.targetFrequency}x per week • ${checkIns.size} check-ins",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Check-in Button
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isCheckedInToday)
                    Color(0xFF10B981).copy(alpha = 0.1f)
                else
                    Primary.copy(alpha = 0.1f),
                onClick = { if (!isCheckedInToday) onCheckIn() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCheckedInToday)
                            Icons.Default.Check
                        else
                            Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isCheckedInToday)
                            Color(0xFF10B981)
                        else
                            Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = Primary
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else Color(0xFFF5F5F5),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextMuted
            )

            Surface(
                shape = CircleShape,
                color = if (isSelected)
                    Color.White.copy(alpha = 0.2f)
                else
                    Color(0xFFE5E7EB)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SortButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Primary else Color.Transparent,
        border = if (!isSelected)
            androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        else null,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextMuted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Primary else TextMain,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
