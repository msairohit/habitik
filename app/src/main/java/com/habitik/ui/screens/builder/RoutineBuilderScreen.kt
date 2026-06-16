package com.habitik.ui.screens.builder

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    viewModel: RoutineBuilderViewModel = hiltViewModel(),
    initialTime: String? = null,
    onSheetDismissed: (() -> Unit)? = null
) {
    val tasks by viewModel.tasks.collectAsState()
    val completedTasksInfo by viewModel.completedTasksInfo.collectAsState()
    var showAddSheet by rememberSaveable { mutableStateOf(initialTime != null) }
    var taskToEdit   by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var selectedFilterCategory by rememberSaveable { mutableStateOf<String?>(null) }

    // If initialTime is provided and we are not editing, pre-fill it for the sheet
    val effectiveInitialTime = if (taskToEdit == null) initialTime else null

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Filter tasks based on selection
    val filteredTasks = remember(tasks, selectedFilterCategory) {
        if (selectedFilterCategory == null) tasks
        else tasks.filter { it.category.uppercase() == selectedFilterCategory?.uppercase() }
    }

    // Stats calculations
    val totalDuration = remember(tasks) { tasks.sumOf { it.durationMin } }
    val totalDurationStr = remember(totalDuration) {
        val h = totalDuration / 60
        val m = totalDuration % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }
    val totalTasksCount = tasks.size

    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.08f),
                            secondaryColor.copy(alpha = 0.04f)
                        )
                    )
                )
        )


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor        = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        titleContentColor     = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            "Routine Builder",
                            style         = MaterialTheme.typography.headlineMedium,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    }
                )
            },
            floatingActionButton = {
                // Classy FAB with theme gradient fill
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = primaryColor.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(colors = listOf(primaryColor, secondaryColor)))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { taskToEdit = null; showAddSheet = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Task",
                            modifier = Modifier.size(28.dp),
                            tint     = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Stats Card at the Top of Screen
                if (tasks.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "TOTAL DURATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    totalDurationStr,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "ROUTINE TASKS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$totalTasksCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = secondaryColor
                                )
                            }
                        }
                    }

                    // Category Filter Row
                    val filterCategories = listOf("ALL", "WORK", "HEALTH", "PERSONAL", "FAMILY", "SPIRITUAL", "OTHER")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterCategories) { cat ->
                            val isSelected = (cat == "ALL" && selectedFilterCategory == null) || (selectedFilterCategory == cat)
                            val catColor = if (cat == "ALL") primaryColor else getCategoryColor(cat)
                            val emoji = if (cat == "ALL") "🎯" else getCategoryEmoji(cat)

                            Surface(
                                onClick = {
                                    selectedFilterCategory = if (cat == "ALL") null else cat
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) catColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) catColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 13.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        cat,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                val (completedOneTimeTasks, activeTasks) = remember(filteredTasks, completedTasksInfo) {
                    filteredTasks.partition { task ->
                        task.repeatDays == "ONCE" && completedTasksInfo.containsKey(task.id)
                    }
                }

                if (filteredTasks.isEmpty()) {
                    val isEmptyOverall = tasks.isEmpty()
                    EmptyState(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        message = if (isEmptyOverall) "No tasks yet" else "No matching tasks",
                        subMessage = if (isEmptyOverall) "Tap + to start building your day" else "Try selecting another filter category"
                    )
                } else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().weight(1f),
                        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (activeTasks.isNotEmpty()) {
                            items(activeTasks, key = { it.id }) { task ->
                                TaskListItem(
                                    task     = task,
                                    onEdit   = { taskToEdit = it; showAddSheet = true },
                                    onDelete = { taskToDelete = it }
                                )
                            }
                        }

                        if (completedOneTimeTasks.isNotEmpty()) {
                            item {
                                Text(
                                    "Completed One-Time Tasks",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }
                            items(completedOneTimeTasks, key = { it.id }) { task ->
                                val doneTime = completedTasksInfo[task.id]
                                TaskListItem(
                                    task     = task,
                                    onEdit   = { taskToEdit = it; showAddSheet = true },
                                    onDelete = { taskToDelete = it },
                                    isCompleted = true,
                                    completedAt = doneTime
                                )
                            }
                        }

                        item { Spacer(Modifier.height(88.dp)) }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTaskBottomSheet(
            taskToEdit = taskToEdit,
            initialStartTime = effectiveInitialTime,
            existingTasks = tasks,
            onDismiss  = {
                showAddSheet = false
                onSheetDismissed?.invoke()
            },
            onSave     = { task ->
                if (taskToEdit == null) viewModel.addTask(task)
                else viewModel.updateTask(task.copy(id = taskToEdit!!.id))
                showAddSheet = false
                onSheetDismissed?.invoke()
            }
        )
    }

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${taskToDelete?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it.id) }
                        taskToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    message: String = "No tasks yet",
    subMessage: String = "Tap + to start building your day"
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Glowing circle
            Box(
                Modifier
                    .size(110.dp)
                    .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier           = Modifier.size(40.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                message,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Task List Item
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListItem(
    task    : TaskEntity,
    onEdit  : (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    isCompleted: Boolean = false,
    completedAt: String? = null
) {
    val categoryColor = getCategoryColor(task.category)
    val emoji = getCategoryEmoji(task.category)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    onDelete(task)
                }
                false
            }
        ),
        backgroundContent = {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint               = MaterialTheme.colorScheme.onErrorContainer,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    ) {
        Surface(
            onClick         = { onEdit(task) },
            shape           = RoundedCornerShape(24.dp),
            color           = MaterialTheme.colorScheme.surface,
            border          = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            shadowElevation = 0.dp,
            modifier        = if (isCompleted) Modifier.alpha(0.55f) else Modifier
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category solid color icon box with centered emoji
                Box(
                    Modifier
                        .size(54.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = categoryColor.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.radialGradient(colors = listOf(categoryColor, categoryColor.copy(alpha = 0.8f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val infoText = if (isCompleted && !completedAt.isNullOrEmpty()) {
                            val timePart = if (task.startTime == "ANYTIME") "Anytime" else task.startTime
                            "Scheduled: $timePart  ·  Done: $completedAt"
                        } else {
                            val detail = when(task.measurementType) {
                                "TIME" -> {
                                    val timePart = if (task.startTime == "ANYTIME") "Anytime" else task.startTime
                                    "$timePart  ·  ${task.durationMin} min"
                                }
                                "COUNT" -> {
                                    val timePart = if (task.startTime == "ANYTIME") "Anytime" else "at ${task.startTime}"
                                    "${task.repeatCount} times/day · $timePart"
                                }
                                "QUANTITY" -> {
                                    val timePart = if (task.startTime == "ANYTIME") "Anytime" else "at ${task.startTime}"
                                    "${task.goalValue}${task.goalUnit} · $timePart"
                                }
                                else -> if (task.startTime == "ANYTIME") "Anytime" else task.startTime
                            }
                            val freqText = when {
                                task.repeatDays == "ONCE" -> "Once"
                                task.repeatDays == "DAILY" -> "Daily"
                                task.repeatDays == "WEEKDAYS" -> "Weekdays"
                                task.repeatDays == "WEEKENDS" -> "Weekends"
                                task.repeatDays.startsWith("[") -> "Custom"
                                else -> task.repeatDays
                            }
                            "$detail  ·  $freqText"
                        }

                        Text(
                            infoText,
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (task.isImportant) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Important",
                                tint               = Color(0xFFFFB800),
                                modifier           = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Category badge
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = categoryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.22f))
                ) {
                    Text(
                        task.category,
                        style         = MaterialTheme.typography.labelSmall,
                        color         = categoryColor,
                        fontWeight    = FontWeight.Black,
                        modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.6.sp
                    )
                }

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = { onDelete(task) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

