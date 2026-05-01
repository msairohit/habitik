package com.habitik.ui.screens.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.screens.home.HomeViewModel
import com.habitik.ui.screens.home.TaskStatusInfo
import com.habitik.ui.theme.*
import androidx.compose.ui.zIndex
import java.time.LocalTime

@Composable
fun TimelineScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit,
    onAddTaskAtTime: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddTaskAtTime(LocalTime.now().toString()) },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Daily Timeline",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    TimelineView(
                        items = uiState.tasksWithStatus,
                        onTaskClick = onNavigateToDetail,
                        onTimeClick = onAddTaskAtTime
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineView(
    items: List<TaskStatusInfo>,
    onTaskClick: (Int) -> Unit,
    onTimeClick: (String) -> Unit
) {
    val hourHeight = 80.dp
    val now = LocalTime.now()

    // Tasks with overlap handling
    val processedTasks = remember(items) {
        val sorted = items.sortedBy { it.task.startTime }
        val groups = mutableListOf<MutableList<TaskStatusInfo>>()
        
        sorted.forEach { item ->
            val start = LocalTime.parse(item.task.startTime)
            val end = start.plusMinutes(item.task.durationMin.toLong())
            
            var foundGroup = false
            for (group in groups) {
                // Check if overlaps with ANY task in the group
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
            val time = LocalTime.of(i, 0)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeight)
                    .offset(y = hourHeight * i)
                    .zIndex(0f) // Behind tasks
                    .clickable { onTimeClick(hourStr) },
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = hourStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.width(45.dp).padding(top = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 18.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(hourHeight * 24)
        ) {
            val totalWidth = maxWidth - 59.dp // Accounting for 55.dp start padding and 4.dp end padding

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
                
                Surface(
                    onClick = { onTaskClick(task.id) },
                    modifier = Modifier
                        .padding(start = 55.dp, end = 4.dp)
                        .offset(x = xOffset, y = yOffset.dp)
                        .width(taskWidth)
                        .height(height.dp.coerceAtLeast(30.dp))
                        .zIndex(1f), // On top of hour lines
                    shape = RoundedCornerShape(16.dp),
                    color = color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(top = 2.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getCategoryEmoji(task.category), fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                
                                // Status badge
                                if (height > 25 && totalCols == 1) { // Only show full badge if not crowded
                                    val percent = (item.progress * 100).toInt()
                                    val badgeText = if (item.status == "PAUSED" || item.status == "RESUMED") 
                                        "${item.status} ($percent%)" else item.status
                                        
                                    Surface(
                                        color = when(item.status) {
                                            "DONE" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            "SKIPPED" -> Color.Gray.copy(alpha = 0.2f)
                                            "PAUSED" -> Color(0xFFFFB800).copy(alpha = 0.2f)
                                            "RESUMED" -> color.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = when(item.status) {
                                                "DONE" -> Color(0xFF2E7D32)
                                                "PAUSED" -> Color(0xFFF57C00)
                                                "RESUMED" -> color
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        
                        if (height > 40) { // Lowered from 50
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.progressText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(Modifier.height(6.dp))
                            // Small progress bar
                            LinearProgressIndicator(
                                progress = item.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = color,
                                trackColor = color.copy(alpha = 0.1f)
                            )
                        } else if (height > 20) { // Lowered from 35
                            Text(
                                text = item.progressText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Current time highlight
        val currentMinutes = now.hour * 60 + now.minute
        val currentYOffset = (currentMinutes.toFloat() / 60f) * hourHeight.value

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = currentYOffset.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
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
