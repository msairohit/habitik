package com.habitik.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.components.CircularCountdownRing
import com.habitik.ui.screens.home.HomeViewModel
import com.habitik.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToBuilder: () -> Unit,
    onNavigateToConcentration: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val taskStreaks by viewModel.taskStreaks.collectAsState()
    val weeklyCompletionRate by viewModel.weeklyCompletionRate.collectAsState()
    val categoryCompletion by viewModel.categoryCompletion.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier         = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            contentPadding   = PaddingValues(top = 52.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item { HeaderSection(onNavigateToBuilder) }
            item { Spacer(Modifier.height(28.dp)) }

            // Main focus card
            item {
                when {
                    uiState.focusableTasks.isNotEmpty() -> {
                        val pagerState = rememberPagerState(pageCount = { uiState.focusableTasks.size })
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                pageSpacing = 16.dp
                            ) { page ->
                                val item = uiState.focusableTasks[page]
                                NowFocusCard(
                                    task          = item.task,
                                    remainingTime = if (item.status == "SKIPPED") "SKIPPED" 
                                                   else if (item.status == "DONE" && item.task.measurementType == "TIME") "DONE"
                                                   else if (item.task.measurementType == "TIME") {
                                                       if (item.task.id == uiState.currentTask?.id) uiState.remainingTime 
                                                       else item.remainingTime
                                                   } else item.remainingTime,
                                    progressLabel = if (item.task.measurementType == "TIME") "REMAINING" else "DONE",
                                    isDoneMode    = item.task.measurementType != "TIME",
                                    progress      = item.progress,
                                    status        = item.status,
                                    onSkip        = { viewModel.onSkipTask(item.task) },
                                    onDone        = { viewModel.onDoneTask(item.task) },
                                    onPause       = { viewModel.onPauseTask(item.task) },
                                    onResume      = { viewModel.onResumeTask(item.task) },
                                    onSnooze      = { viewModel.onSnoozeTask() },
                                    onIncrement   = { viewModel.onIncrementTask(item.task, item.task.quantityIncrement) },
                                    onEnterConcentration = { onNavigateToConcentration(item.task.id) }
                                )
                            }
                            
                            if (uiState.focusableTasks.size > 1) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.wrapContentHeight().fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(uiState.focusableTasks.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.3f)
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    uiState.isRestDay -> RestDayCard()
                    else -> WaitingCard(uiState.nextTasks.firstOrNull())
                }
                Spacer(Modifier.height(36.dp))
            }

            // Overview section

            item {
                Text(
                    "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                )
            }
            
            item {
                TodayProgressWidget(tasks = uiState.tasksWithStatus)
            }
            item {
                WeeklyCalendarWidget(completionRates = weeklyCompletionRate)
            }
            item {
                StreaksWidget(tasks = uiState.allTasks, streaks = taskStreaks)
            }
            item {
                CategoryBreakdownWidget(categoryCounts = categoryCompletion)
            }
        }
    }
}

@Composable
private fun TodayProgressWidget(tasks: List<com.habitik.ui.screens.home.TaskStatusInfo>) {
    val doneCount = tasks.count { it.status == "DONE" }
    val totalCount = tasks.size
    val fraction = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Today's Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$doneCount / $totalCount completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun WeeklyCalendarWidget(completionRates: Map<String, Float>) {
    val currentWeekDays = remember {
        val monday = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        (0..6).map { monday.plusDays(it.toLong()) }
    }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Weekly Consistency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                currentWeekDays.forEach { date ->
                    val dayOfWeek = date.dayOfWeek.name.take(3)
                    val isToday = date == java.time.LocalDate.now()
                    val rate = completionRates[date.toString()] ?: 0f
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rate >= 1f) MaterialTheme.colorScheme.primary
                                    else if (rate > 0f) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rate >= 1f) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = "${(rate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (rate > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun StreaksWidget(tasks: List<TaskEntity>, streaks: Map<Int, Int>) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Habit Streaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (tasks.isEmpty()) {
                Text("No tasks scheduled.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                tasks.forEach { task ->
                    val streak = streaks[task.id] ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(getCategoryEmoji(task.category), fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(task.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$streak days",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black,
                                color = if (streak > 0) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (streak > 0) {
                                Spacer(Modifier.width(4.dp))
                                Text("🔥", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownWidget(categoryCounts: Map<String, Int>) {
    val categories = listOf("WORK", "HEALTH", "PERSONAL", "FAMILY", "SPIRITUAL", "OTHER")
    val maxVal = categoryCounts.values.maxOrNull() ?: 1
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Category Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            categories.forEach { cat ->
                val count = categoryCounts[cat] ?: 0
                val color = getCategoryColor(cat)
                val fillFraction = if (maxVal > 0) count.toFloat() / maxVal else 0f
                
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("$count completed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { fillFraction },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = color,
                        trackColor = color.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedBackground(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                drawRect(primaryColor.copy(alpha = 0.05f))
            }
    )
}

@Composable
private fun HeaderSection(onNavigateToBuilder: () -> Unit) {
    val today = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
        )
    }
    Row(
        modifier             = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment    = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = today,
                style      = MaterialTheme.typography.labelLarge,
                color      = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = "Now Focusing",
                style         = MaterialTheme.typography.displaySmall,
                fontWeight    = FontWeight.Black,
                color         = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-1.5).sp
            )
        }

        Surface(
            onClick       = onNavigateToBuilder,
            modifier      = Modifier.size(48.dp),
            shape         = CircleShape,
            color         = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            border        = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Builder",
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun NowFocusCard(
    task: TaskEntity,
    remainingTime: String,
    progressLabel: String,
    isDoneMode: Boolean,
    progress: Float,
    status: String,
    onSkip: () -> Unit,
    onDone: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSnooze: () -> Unit,
    onIncrement: () -> Unit,
    onEnterConcentration: () -> Unit
) {
    val color = getCategoryColor(task.category)
    val isPaused = status == "PAUSED"
    val isPending = status == "PENDING"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(40.dp))
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(color.copy(alpha = 0.15f))
        )
        Box(Modifier.matchParentSize().background(color))
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Text(
                    text = task.category.uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(text = getCategoryEmoji(task.category), fontSize = 54.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(text = task.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(24.dp))
            CircularCountdownRing(
                progress = progress,
                centerText = remainingTime,
                subText = progressLabel,
                isDoneMode = isDoneMode,
                brush = SolidColor(Color.White),
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(32.dp))
            if (status == "DONE") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("TASK COMPLETED", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassActionButton(Icons.Default.SkipNext, "Skip", Color.White.copy(alpha = 0.14f), Color.White, Modifier.weight(1f), onSkip)
                    
                    if (task.measurementType == "TIME") {
                        when {
                            isPending -> GlassActionButton(Icons.Default.Timer, "Start", Color.White.copy(alpha = 0.25f), Color.White, Modifier.weight(1f), onResume)
                            isPaused -> GlassActionButton(Icons.Default.Timer, "Resume", Color.White.copy(alpha = 0.25f), Color.White, Modifier.weight(1f), onResume)
                            else -> GlassActionButton(Icons.Default.Timer, "Stop", Color.White.copy(alpha = 0.14f), Color.White, Modifier.weight(1f), onPause)
                        }
                    } else {
                        GlassActionButton(Icons.Default.Add, "Add", Color.White.copy(alpha = 0.25f), Color.White, Modifier.weight(1f), onIncrement)
                    }
                    
                    GlassActionButton(Icons.Default.Check, "Done", Color.White, color, Modifier.weight(1.2f), onDone)
                }
            }
        }
        if (task.measurementType == "TIME") {
            IconButton(
                onClick = onEnterConcentration,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Concentration Mode",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun GlassActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, containerColor: Color, contentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(22.dp), color = containerColor, contentColor = contentColor, border = if (containerColor != Color.White) BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)) else null) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
        }
    }
}

@Composable
fun RestDayCard() {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(40.dp)).background(MaterialTheme.colorScheme.primary).padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("All Done! 🎉", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Black)
            Text("Time to rest and recharge.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.82f))
        }
    }
}

@Composable
fun WaitingCard(nextTask: TaskEntity?) {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(40.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TAKE A BREATH", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text(nextTask?.name ?: "No tasks scheduled", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
        }
    }
}
