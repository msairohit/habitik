package com.habitik.ui.screens.timeline

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.screens.home.HomeViewModel
import com.habitik.ui.screens.home.TaskStatusInfo
import com.habitik.ui.screens.home.DayScheduleInfo
import com.habitik.ui.theme.getCategoryColor
import com.habitik.ui.theme.getCategoryEmoji
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimelineScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit,
    onAddTaskAtTime: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()
    
    var viewMode by remember { mutableStateOf("LIST") } // "LIST" (Day List), "CALENDAR" (Day Grid), "WEEK" (Week Overview)
    
    // Find today's index in the weekly schedule
    val today = remember { LocalDate.now() }
    val todayIndex = remember(weeklySchedule) {
        val idx = weeklySchedule.indexOfFirst { it.date == today }
        if (idx != -1) idx else (today.dayOfWeek.value - 1).coerceIn(0, 6)
    }
    
    var selectedDayIndex by remember(todayIndex) { mutableIntStateOf(todayIndex) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    onAddTaskAtTime(time) 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Header Bar
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Toggle Tab Control on its own row for proper alignment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val activeBg = MaterialTheme.colorScheme.surface
                    val activeTint = MaterialTheme.colorScheme.primary
                    val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    
                    // List Mode Toggle
                    Surface(
                        onClick = { viewMode = "LIST" },
                        shape = RoundedCornerShape(11.dp),
                        color = if (viewMode == "LIST") activeBg else Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, tint = if (viewMode == "LIST") activeTint else inactiveTint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("List", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (viewMode == "LIST") activeTint else inactiveTint)
                        }
                    }

                    // Calendar Mode Toggle
                    Surface(
                        onClick = { viewMode = "CALENDAR" },
                        shape = RoundedCornerShape(11.dp),
                        color = if (viewMode == "CALENDAR") activeBg else Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, tint = if (viewMode == "CALENDAR") activeTint else inactiveTint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Calendar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (viewMode == "CALENDAR") activeTint else inactiveTint)
                        }
                    }
                    
                    // Week Mode Toggle
                    Surface(
                        onClick = { viewMode = "WEEK" },
                        shape = RoundedCornerShape(11.dp),
                        color = if (viewMode == "WEEK") activeBg else Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = if (viewMode == "WEEK") activeTint else inactiveTint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Week", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (viewMode == "WEEK") activeTint else inactiveTint)
                        }
                    }
                    
                    // History Mode Toggle
                    Surface(
                        onClick = { viewMode = "HISTORY" },
                        shape = RoundedCornerShape(11.dp),
                        color = if (viewMode == "HISTORY") activeBg else Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = if (viewMode == "HISTORY") activeTint else inactiveTint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("History", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (viewMode == "HISTORY") activeTint else inactiveTint)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = viewMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "view_mode_anim"
            ) { targetMode ->
                if (targetMode == "LIST" || targetMode == "CALENDAR") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Week Day Strip Navigator
                        if (weeklySchedule.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weeklySchedule.forEachIndexed { index, dayInfo ->
                                    val isSelected = index == selectedDayIndex
                                    val isDayToday = dayInfo.date == today
                                    
                                    val countDone = dayInfo.tasks.count { it.status == "DONE" }
                                    val countTotal = dayInfo.tasks.size
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else Color.Transparent
                                            )
                                            .clickable { selectedDayIndex = index }
                                            .padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = dayInfo.dayName.take(3).uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Surface(
                                            modifier = Modifier.size(34.dp),
                                            shape = CircleShape,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isDayToday -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            },
                                            border = if (isDayToday && !isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = dayInfo.date.dayOfMonth.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        
                                        Spacer(Modifier.height(6.dp))
                                        
                                        // Mini progress dots
                                        if (countTotal > 0) {
                                            val dotColor = if (countDone == countTotal) Color(0xFF4CAF50) else if (countDone > 0) Color(0xFFFFB800) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        } else {
                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Tasks details rendering
                        if (weeklySchedule.isNotEmpty() && selectedDayIndex in weeklySchedule.indices) {
                            val selectedDayInfo = weeklySchedule[selectedDayIndex]
                            
                            if (selectedDayInfo.tasks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🌿", fontSize = 48.sp)
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "No tasks scheduled for this day",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Tap the + button to create a new task.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                val anytimeTasks = remember(selectedDayInfo.tasks) { selectedDayInfo.tasks.filter { it.task.startTime == "ANYTIME" } }
                                val timedTasks = remember(selectedDayInfo.tasks) { selectedDayInfo.tasks.filter { it.task.startTime != "ANYTIME" } }

                                if (targetMode == "LIST") {
                                    // Day List chronological timeline view
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
                                    ) {
                                        if (anytimeTasks.isNotEmpty()) {
                                            item {
                                                AnytimeTasksSection(
                                                    anytimeTasks = anytimeTasks,
                                                    onTaskClick = onNavigateToDetail,
                                                    onDone = { task -> viewModel.onDoneTask(task) },
                                                    onSkip = { task -> viewModel.onSkipTask(task) },
                                                    onPause = { task -> viewModel.onPauseTask(task) },
                                                    onResume = { task -> viewModel.onResumeTask(task) },
                                                    onIncrement = { task -> viewModel.onIncrementTask(task, task.quantityIncrement) },
                                                    activeTask = uiState.currentTask,
                                                    remainingTime = uiState.remainingTime
                                                )
                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }
                                        itemsIndexed(timedTasks) { idx, item ->
                                            ChronologicalTimelineItem(
                                                item = item,
                                                isFirst = idx == 0,
                                                isLast = idx == timedTasks.size - 1,
                                                onTaskClick = onNavigateToDetail,
                                                onDone = { viewModel.onDoneTask(item.task) },
                                                onSkip = { viewModel.onSkipTask(item.task) },
                                                onPause = { viewModel.onPauseTask(item.task) },
                                                onResume = { viewModel.onResumeTask(item.task) },
                                                onIncrement = { viewModel.onIncrementTask(item.task, item.task.quantityIncrement) },
                                                activeTask = uiState.currentTask,
                                                remainingTime = uiState.remainingTime
                                            )
                                        }
                                    }
                                } else {
                                    // Calendar Day View (Absolute hour slots grid)
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(horizontal = 20.dp),
                                        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
                                    ) {
                                        if (anytimeTasks.isNotEmpty()) {
                                            item {
                                                AnytimeTasksSection(
                                                    anytimeTasks = anytimeTasks,
                                                    onTaskClick = onNavigateToDetail,
                                                    onDone = { task -> viewModel.onDoneTask(task) },
                                                    onSkip = { task -> viewModel.onSkipTask(task) },
                                                    onPause = { task -> viewModel.onPauseTask(task) },
                                                    onResume = { task -> viewModel.onResumeTask(task) },
                                                    onIncrement = { task -> viewModel.onIncrementTask(task, task.quantityIncrement) },
                                                    activeTask = uiState.currentTask,
                                                    remainingTime = uiState.remainingTime
                                                )
                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }
                                        item {
                                            CalendarDayView(
                                                items = timedTasks,
                                                isDayToday = selectedDayInfo.date == today,
                                                onTaskClick = onNavigateToDetail,
                                                onTimeClick = onAddTaskAtTime,
                                                onDone = { task -> viewModel.onDoneTask(task) },
                                                onSkip = { task -> viewModel.onSkipTask(task) },
                                                onPause = { task -> viewModel.onPauseTask(task) },
                                                onResume = { task -> viewModel.onResumeTask(task) },
                                                onIncrement = { task -> viewModel.onIncrementTask(task, task.quantityIncrement) },
                                                activeTask = uiState.currentTask,
                                                remainingTime = uiState.remainingTime
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (targetMode == "WEEK") {
                    // Weekly View (7 cards showing day schedules)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(weeklySchedule) { dayInfo ->
                            WeeklyDayOverviewCard(
                                dayInfo = dayInfo,
                                isToday = dayInfo.date == today,
                                onClick = {
                                    val index = weeklySchedule.indexOf(dayInfo)
                                    if (index != -1) {
                                        selectedDayIndex = index
                                        viewMode = "LIST" // Switch back to day list view
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // HISTORY view mode
                    HistoryCalendarView(
                        viewModel = viewModel,
                        onNavigateToDetail = onNavigateToDetail,
                        onAddTaskAtTime = onAddTaskAtTime
                    )
                }
            }
        }
    }
}

@Composable
fun ChronologicalTimelineItem(
    item: TaskStatusInfo,
    isFirst: Boolean,
    isLast: Boolean,
    onTaskClick: (Int) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onIncrement: () -> Unit,
    activeTask: TaskEntity?,
    remainingTime: String,
    readOnly: Boolean = false
) {
    val categoryColor = getCategoryColor(item.task.category)
    val isAnytime = item.task.startTime == "ANYTIME"
    val startTime = if (isAnytime) LocalTime.MIDNIGHT else LocalTime.parse(item.task.startTime)
    val endTime = startTime.plusMinutes(item.task.durationMin.toLong())
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val timeStr = if (isAnytime) "Anytime" else startTime.format(formatter)
    val endTimeStr = if (isAnytime) "" else endTime.format(formatter)
    
    val isRunning = activeTask?.id == item.task.id && item.status == "RESUMED"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.Top
    ) {
        // Time & Timeline path column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(75.dp)
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!isAnytime) {
                Text(
                    text = "${item.task.durationMin}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                val countText = when (item.task.measurementType) {
                    "COUNT" -> "${item.task.repeatCount}x"
                    "QUANTITY" -> "${item.task.goalValue.toInt()}${item.task.goalUnit}"
                    else -> "All Day"
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Timeline line with bullet anchor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Vertical connecting line
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        categoryColor.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }
                
                // Bullet anchor node
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.status == "DONE") Color(0xFF4CAF50)
                            else if (item.status == "SKIPPED") Color.Gray
                            else categoryColor
                        )
                        .padding(2.dp)
                ) {
                    if (item.status == "DONE") {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Task Card Content
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
                .clickable(enabled = !readOnly) { onTaskClick(item.task.id) },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = if (item.status == "DONE" || item.status == "SKIPPED") 0.4f else 0.85f
                )
            ),
            border = BorderStroke(
                1.dp, 
                if (isRunning) categoryColor 
                else categoryColor.copy(alpha = 0.22f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title and category badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getCategoryEmoji(item.task.category),
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item.task.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (item.status == "DONE" || item.status == "SKIPPED") 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Status Badge
                    val statusColor = when (item.status) {
                        "DONE" -> Color(0xFF4CAF50)
                        "SKIPPED" -> Color.Gray
                        "PAUSED" -> Color(0xFFFFB800)
                        "RESUMED", "IN PROGRESS", "STARTED" -> categoryColor
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = item.status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Duration & Time Window info
                Text(
                    text = "$timeStr - $endTimeStr",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(10.dp))
                
                // Progress tracker if started
                if (item.status != "PENDING" && item.status != "SKIPPED") {
                    val displayTimeText = if (isRunning) remainingTime else item.progressText
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item.task.measurementType == "TIME") "Time Remaining: $displayTimeText" else displayTimeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(item.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = categoryColor,
                        trackColor = categoryColor.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                
                // Quick actions row
                if (item.status != "DONE" && item.status != "SKIPPED" && !readOnly) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip action
                        IconButton(onClick = onSkip, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Spacer(Modifier.width(8.dp))
                        
                        // Start/Stop or Add actions
                        if (item.task.measurementType == "TIME") {
                            IconButton(
                                onClick = { if (isRunning) onPause() else onResume() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isRunning) "Pause" else "Start",
                                    tint = categoryColor
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onIncrement,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increment", tint = categoryColor)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        
                        // Done action
                        Button(
                            onClick = onDone,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = categoryColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Done", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayView(
    items: List<TaskStatusInfo>,
    isDayToday: Boolean,
    onTaskClick: (Int) -> Unit,
    onTimeClick: (String) -> Unit,
    onDone: (TaskEntity) -> Unit,
    onSkip: (TaskEntity) -> Unit,
    onPause: (TaskEntity) -> Unit,
    onResume: (TaskEntity) -> Unit,
    onIncrement: (TaskEntity) -> Unit,
    activeTask: TaskEntity?,
    remainingTime: String
) {
    val timedItems = remember(items) { items.filter { it.task.startTime != "ANYTIME" } }
    val hourHeight = 85.dp
    val now = LocalTime.now()

    // Tasks with overlap handling
    val processedTasks = remember(timedItems) {
        val sorted = timedItems.sortedBy { it.task.startTime }
        val groups = mutableListOf<MutableList<TaskStatusInfo>>()
        
        sorted.forEach { item ->
            val start = LocalTime.parse(item.task.startTime)
            val end = start.plusMinutes(item.task.durationMin.toLong())
            
            var foundGroup = false
            for (group in groups) {
                val overlaps = group.any { groupItem ->
                    val gStart = LocalTime.parse(groupItem.task.startTime)
                    val gEnd = gStart.plusMinutes(groupItem.task.durationMin.toLong())
                    start.isBefore(gEnd) && end.isAfter(gStart)
                }
                if (overlaps) {
                    group.add(item)
                    foundGroup = true
                    break
                }
            }
            if (!foundGroup) {
                groups.add(mutableListOf(item))
            }
        }
        
        val result = mutableListOf<Triple<TaskStatusInfo, Int, Int>>() // Item, ColIndex, TotalCols
        groups.forEach { group ->
            group.forEachIndexed { index, item ->
                result.add(Triple(item, index, group.size))
            }
        }
        result
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hourHeight * 24)
    ) {
        // Hour lines and clickable slots
        for (i in 0..23) {
            val hourStr = String.format("%02d:00", i)
            val timeLabel = if (i == 0) "12 AM" else if (i < 12) "$i AM" else if (i == 12) "12 PM" else "${i - 12} PM"
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeight)
                    .offset(y = hourHeight * i)
                    .zIndex(0f)
                    .clickable { onTimeClick(String.format("%02d:00", i)) },
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.width(50.dp).padding(top = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 18.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                )
            }
        }

        // Tasks cards placement on the timeline grid
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(hourHeight * 24)
        ) {
            val totalWidth = maxWidth - 60.dp

            processedTasks.forEach { (item, colIndex, totalCols) ->
                val task = item.task
                val startTime = LocalTime.parse(task.startTime)
                val startMinutes = startTime.hour * 60 + startTime.minute
                val duration = task.durationMin
                
                val yOffset = (startMinutes.toFloat() / 60f) * hourHeight.value
                val height = (duration.toFloat() / 60f) * hourHeight.value
                val color = getCategoryColor(task.category)

                val widthMultiplier = 1f / totalCols
                val taskWidth = totalWidth * widthMultiplier
                val xOffset = taskWidth * colIndex
                
                val isRunning = activeTask?.id == task.id && item.status == "RESUMED"

                Surface(
                    onClick = { onTaskClick(task.id) },
                    modifier = Modifier
                        .padding(start = 56.dp, end = 4.dp)
                        .offset(x = xOffset, y = yOffset.dp)
                        .width(taskWidth)
                        .height(height.dp.coerceAtLeast(42.dp))
                        .zIndex(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(
                        alpha = if (item.status == "DONE" || item.status == "SKIPPED") 0.45f else 0.92f
                    ),
                    border = BorderStroke(
                        if (isRunning) 2.dp else 1.dp,
                        if (isRunning) color else color.copy(alpha = 0.35f)
                    ),
                    shadowElevation = if (isRunning) 6.dp else 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        // Title / Emoji line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(getCategoryEmoji(task.category), fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = task.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (item.status == "DONE" || item.status == "SKIPPED")
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Compact status pill
                            if (height >= 55) {
                                Surface(
                                    color = when (item.status) {
                                        "DONE" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        "SKIPPED" -> Color.Gray.copy(alpha = 0.15f)
                                        "PAUSED" -> Color(0xFFFFB800).copy(alpha = 0.15f)
                                        "RESUMED" -> color.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = item.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = when (item.status) {
                                            "DONE" -> Color(0xFF2E7D32)
                                            "PAUSED" -> Color(0xFFF57C00)
                                            "RESUMED" -> color
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        }
                                    )
                                }
                            }
                        }

                        // Time window text
                        if (height >= 50) {
                            Spacer(Modifier.height(2.dp))
                            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                            val endAlertTime = startTime.plusMinutes(task.durationMin.toLong())
                            Text(
                                text = "${startTime.format(formatter)} - ${endAlertTime.format(formatter)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Progress bar for active/partially done tasks
                        if (height >= 75 && item.status != "PENDING" && item.status != "SKIPPED") {
                            Spacer(Modifier.height(4.dp))
                            val displayProgressText = if (isRunning) remainingTime else item.progressText
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayProgressText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${(item.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(CircleShape),
                                color = color,
                                trackColor = color.copy(alpha = 0.12f)
                            )
                        }

                        // Quick action buttons if room permits
                        if (height >= 100 && item.status != "DONE" && item.status != "SKIPPED" && totalCols == 1) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onSkip(task) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(6.dp))

                                if (task.measurementType == "TIME") {
                                    IconButton(
                                        onClick = { if (isRunning) onPause(task) else onResume(task) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isRunning) "Pause" else "Start",
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { onIncrement(task) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.12f))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = color, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { onDone(task) },
                                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Done", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Current time highlight line
        if (isDayToday) {
            val currentMinutes = now.hour * 60 + now.minute
            val currentYOffset = (currentMinutes.toFloat() / 60f) * hourHeight.value

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = currentYOffset.dp)
                    .zIndex(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 50.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Red, Color.Red.copy(alpha = 0f))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun WeeklyDayOverviewCard(
    dayInfo: DayScheduleInfo,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val countDone = dayInfo.tasks.count { it.status == "DONE" }
    val countTotal = dayInfo.tasks.size
    val progress = if (countTotal > 0) countDone.toFloat() / countTotal else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Day name and Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dayInfo.date.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isToday) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "TODAY",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = dayInfo.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Progress circle or badge
                if (countTotal > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$countDone / $countTotal DONE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (countDone == countTotal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (countDone == countTotal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    Text(
                        text = "REST DAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Sub-tasks preview
            if (dayInfo.tasks.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayInfo.tasks.take(4).forEach { item ->
                        val task = item.task
                        val timeStr = if (task.startTime == "ANYTIME") "Anytime" else {
                            LocalTime.parse(task.startTime).format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getCategoryEmoji(task.category), fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.status == "DONE") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (dayInfo.tasks.size > 4) {
                        Text(
                            text = "+ ${dayInfo.tasks.size - 4} more routines",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnytimeTasksSection(
    anytimeTasks: List<TaskStatusInfo>,
    onTaskClick: (Int) -> Unit,
    onDone: (TaskEntity) -> Unit,
    onSkip: (TaskEntity) -> Unit,
    onPause: (TaskEntity) -> Unit,
    onResume: (TaskEntity) -> Unit,
    onIncrement: (TaskEntity) -> Unit,
    activeTask: TaskEntity?,
    remainingTime: String,
    readOnly: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "⚡ Anytime Goals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            anytimeTasks.forEach { item ->
                AnytimeTaskCard(
                    item = item,
                    onClick = { onTaskClick(item.task.id) },
                    onDone = { onDone(item.task) },
                    onSkip = { onSkip(item.task) },
                    onIncrement = { onIncrement(item.task) },
                    readOnly = readOnly
                )
            }
        }
    }
}

@Composable
fun AnytimeTaskCard(
    item: TaskStatusInfo,
    onClick: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onIncrement: () -> Unit,
    readOnly: Boolean = false
) {
    val categoryColor = getCategoryColor(item.task.category)
    val isDone = item.status == "DONE"
    val isSkipped = item.status == "SKIPPED"
    
    Card(
        onClick = { if (!readOnly) onClick() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDone) Color.Transparent else categoryColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryEmoji(item.task.category), fontSize = 18.sp)
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isDone || isSkipped) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                
                val progressText = when (item.task.measurementType) {
                    "COUNT" -> "${item.progressText} · Anytime"
                    "QUANTITY" -> "${item.progressText} · Anytime"
                    else -> "Anytime / All Day"
                }
                
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Quick action buttons
            if (!isDone && !isSkipped && !readOnly) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.task.measurementType != "TIME") {
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = categoryColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = categoryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Done", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Done Badge
                val statusColor = when (item.status) {
                    "DONE" -> Color(0xFF4CAF50)
                    "SKIPPED" -> Color.Gray
                    "PAUSED" -> Color(0xFFFFB800)
                    "RESUMED", "IN PROGRESS", "STARTED" -> categoryColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        text = item.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCalendarView(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onAddTaskAtTime: (String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayScheduleInfo by viewModel.selectedDateSchedule.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val currentMonth = selectedDate.month
    val currentYear = selectedDate.year
    
    var calendarMonth by remember { mutableStateOf(currentMonth) }
    var calendarYear by remember { mutableStateOf(currentYear) }
    
    val yearMonth = remember(calendarMonth, calendarYear) { java.time.YearMonth.of(calendarYear, calendarMonth) }
    val firstDayOfMonth = remember(yearMonth) { yearMonth.atDay(1) }
    val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(firstDayOfMonth) { firstDayOfMonth.dayOfWeek.value } // 1 (Mon) to 7 (Sun)
    
    val emptyCells = firstDayOfWeek - 1
    val totalCells = emptyCells + daysInMonth
    
    var showMonthYearPickerDialog by remember { mutableStateOf(false) }
    val categoryColor = MaterialTheme.colorScheme.primary
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Month / Year Selector Header Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newYearMonth = yearMonth.minusMonths(1)
                                calendarMonth = newYearMonth.month
                                calendarYear = newYearMonth.year
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }
                        
                        Text(
                            text = "${calendarMonth.name.lowercase().replaceFirstChar { it.uppercase() }} $calendarYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable { showMonthYearPickerDialog = true }
                        )
                        
                        IconButton(
                            onClick = {
                                val newYearMonth = yearMonth.plusMonths(1)
                                calendarMonth = newYearMonth.month
                                calendarYear = newYearMonth.year
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Weekday Row: M T W T F S S
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        days.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Calendar Days Grid
                    val chunkedDays = (0 until totalCells).chunked(7)
                    chunkedDays.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { cellIndex ->
                                val dayNumber = cellIndex - emptyCells + 1
                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = java.time.LocalDate.of(calendarYear, calendarMonth, dayNumber)
                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == java.time.LocalDate.now()
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (isToday) MaterialTheme.colorScheme.surfaceVariant
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                viewModel.selectDate(cellDate)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) Color.White
                                                    else if (isToday) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Spacer(Modifier.size(36.dp))
                                }
                            }
                            // Pad out end of week
                            if (week.size < 7) {
                                repeat(7 - week.size) {
                                    Spacer(Modifier.size(36.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        
        // Selected Date Summary & Task details
        if (dayScheduleInfo != null) {
            val tasks = dayScheduleInfo!!.tasks
            val countTotal = tasks.size
            val countDone = tasks.count { it.status == "DONE" }
            val completionRate = if (countTotal > 0) (countDone.toFloat() / countTotal * 100).toInt() else 0
            
            item {
                Spacer(Modifier.height(12.dp))
                // Day summary header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$countDone of $countTotal tasks completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Circle Progress rate
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { if (countTotal > 0) countDone.toFloat() / countTotal else 0f },
                                modifier = Modifier.size(52.dp),
                                strokeWidth = 5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            Text(
                                text = "$completionRate%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Tasks checklist details
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🍃", fontSize = 44.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No tasks scheduled for this day",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val anytimeTasks = tasks.filter { it.task.startTime == "ANYTIME" }
                val timedTasks = tasks.filter { it.task.startTime != "ANYTIME" }
                
                if (anytimeTasks.isNotEmpty()) {
                    item {
                        AnytimeTasksSection(
                            anytimeTasks = anytimeTasks,
                            onTaskClick = onNavigateToDetail,
                            onDone = { task -> viewModel.onDoneTask(task) },
                            onSkip = { task -> viewModel.onSkipTask(task) },
                            onPause = { task -> viewModel.onPauseTask(task) },
                            onResume = { task -> viewModel.onResumeTask(task) },
                            onIncrement = { task -> viewModel.onIncrementTask(task, task.quantityIncrement) },
                            activeTask = uiState.currentTask,
                            remainingTime = uiState.remainingTime,
                            readOnly = true
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                
                itemsIndexed(timedTasks) { idx, item ->
                    ChronologicalTimelineItem(
                        item = item,
                        isFirst = idx == 0,
                        isLast = idx == timedTasks.size - 1,
                        onTaskClick = onNavigateToDetail,
                        onDone = { viewModel.onDoneTask(item.task) },
                        onSkip = { viewModel.onSkipTask(item.task) },
                        onPause = { viewModel.onPauseTask(item.task) },
                        onResume = { viewModel.onResumeTask(item.task) },
                        onIncrement = { viewModel.onIncrementTask(item.task, item.task.quantityIncrement) },
                        activeTask = uiState.currentTask,
                        remainingTime = uiState.remainingTime,
                        readOnly = true
                    )
                }
            }
        }
    }
    
    // Month/Year picker popup dialog
    if (showMonthYearPickerDialog) {
        var selectedPickerMonth by remember { mutableStateOf(calendarMonth.value) }
        var selectedPickerYear by remember { mutableStateOf(calendarYear) }
        
        AlertDialog(
            onDismissRequest = { showMonthYearPickerDialog = false },
            title = { Text("Jump to Date", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select Month", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            (1..6).forEach { m ->
                                val monthName = java.time.Month.of(m).name.take(3)
                                OutlinedButton(
                                    onClick = { selectedPickerMonth = m },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedPickerMonth == m) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (selectedPickerMonth == m) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, if (selectedPickerMonth == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Text(monthName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            (7..12).forEach { m ->
                                val monthName = java.time.Month.of(m).name.take(3)
                                OutlinedButton(
                                    onClick = { selectedPickerMonth = m },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedPickerMonth == m) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (selectedPickerMonth == m) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, if (selectedPickerMonth == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Text(monthName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    Text("Select Year", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedPickerYear-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                        Text(
                            text = selectedPickerYear.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { selectedPickerYear++ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendarMonth = java.time.Month.of(selectedPickerMonth)
                        calendarYear = selectedPickerYear
                        viewModel.selectDate(java.time.LocalDate.of(selectedPickerYear, selectedPickerMonth, 1))
                        showMonthYearPickerDialog = false
                    }
                ) {
                    Text("JUMP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthYearPickerDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
