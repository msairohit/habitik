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
    onNavigateToBuilder: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                                                       else item.progressText.split(" ").first()
                                                   } else item.progressText.split(" ").first(),
                                    progressLabel = if (item.task.measurementType == "TIME") "REMAINING" else "DONE",
                                    isDoneMode    = item.task.measurementType != "TIME",
                                    progress      = item.progress,
                                    status        = item.status,
                                    onSkip        = { viewModel.onSkipTask(item.task) },
                                    onDone        = { viewModel.onDoneTask(item.task) },
                                    onPause       = { viewModel.onPauseTask(item.task) },
                                    onResume      = { viewModel.onResumeTask(item.task) },
                                    onSnooze      = { viewModel.onSnoozeTask() },
                                    onIncrement   = { viewModel.onIncrementTask(item.task) }
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

            // Placeholder for widgets
            item {
                Text(
                    "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            item {
                WidgetPlaceholder("Daily Progress", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
            }
            item {
                WidgetPlaceholder("Habit Streak", Color(0xFFFFB800))
                Spacer(Modifier.height(16.dp))
            }
            item {
                WidgetPlaceholder("Energy Level", MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WidgetPlaceholder(title: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AnimatedBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgShift"
    )
    val bgColor = if (shift > 0.5f) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor))
    }
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
    onIncrement: () -> Unit
) {
    val color = getCategoryColor(task.category)
    val isPaused = status == "PAUSED"
    val isPending = status == "PENDING"
    val scale by rememberInfiniteTransition(label = "breathe").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breatheScale"
    )
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(40.dp))
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    shadowElevation = 60f
                    shape = RoundedCornerShape(40.dp)
                    clip = false
                    alpha = pulse
                }
                .background(color.copy(alpha = 0.55f))
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
