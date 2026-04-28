package com.habitik.ui.screens.builder

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun RoutineBuilderScreen(viewModel: RoutineBuilderViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var taskToEdit   by remember { mutableStateOf<TaskEntity?>(null) }

    // ── Animated gradient background ────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "builderBg")
    val shift by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label         = "builderShift"
    )
    val bgBrush = Brush.radialGradient(
        colors = listOf(
            GlowCyan.copy(alpha = 0.15f + 0.06f * shift),
            GlowViolet.copy(alpha = 0.10f + 0.05f * (1f - shift)),
            BgBase
        ),
        center = Offset(900f + 200f * (1f - shift), 600f + 150f * shift),
        radius = 1300f
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Box(Modifier.fillMaxSize().background(BgBase))
        Box(Modifier.fillMaxSize().background(bgBrush))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor        = Color.Transparent,
                        scrolledContainerColor = BgSurface.copy(alpha = 0.92f),
                        titleContentColor     = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            "Routine Builder",
                            style         = MaterialTheme.typography.displayMedium,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    }
                )
            },
            floatingActionButton = {
                // Gradient FAB
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(20.dp, RoundedCornerShape(22.dp), spotColor = GlowViolet.copy(alpha = 0.6f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(GlowViolet, GlowCyan)))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { taskToEdit = null; showAddSheet = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Task",
                            modifier = Modifier.size(30.dp),
                            tint     = Color.White
                        )
                    }
                }
            }
        ) { padding ->
            if (tasks.isEmpty()) {
                EmptyState(Modifier.fillMaxSize().padding(padding))
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(padding),
                    contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskListItem(
                            task     = task,
                            onEdit   = { taskToEdit = it; showAddSheet = true },
                            onDelete = { viewModel.deleteTask(it.id) }
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTaskBottomSheet(
            taskToEdit = taskToEdit,
            onDismiss  = { showAddSheet = false },
            onSave     = { task ->
                if (taskToEdit == null) viewModel.addTask(task)
                else viewModel.updateTask(task.copy(id = taskToEdit!!.id))
                showAddSheet = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Glowing circle
            Box(
                Modifier
                    .size(120.dp)
                    .shadow(24.dp, CircleShape, spotColor = GlowViolet.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(GlowViolet.copy(alpha = 0.3f), BgCard))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier           = Modifier.size(48.dp),
                    tint               = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "No tasks yet",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap + to start building your day",
                style = MaterialTheme.typography.bodyLarge,
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
    onDelete: (TaskEntity) -> Unit
) {
    val categoryColor = getCategoryColor(task.category)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(task); true } else false
            }
        ),
        backgroundContent = {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                        )
                    )
                    .padding(end = 28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint               = MaterialTheme.colorScheme.onErrorContainer,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
    ) {
        Surface(
            onClick         = { onEdit(task) },
            shape           = RoundedCornerShape(28.dp),
            color           = Color.White.copy(alpha = 0.05f),
            border          = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category gradient icon box
                Box(
                    Modifier
                        .size(56.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = categoryColor.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(getCategoryGradient(task.category))
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${task.startTime}  ·  ${task.durationMin} min",
                            style      = MaterialTheme.typography.bodyMedium,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (task.isImportant) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Important",
                                tint               = Color(0xFFFFB800),
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Category badge
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = categoryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.25f))
                ) {
                    Text(
                        task.category,
                        style         = MaterialTheme.typography.labelSmall,
                        color         = categoryColor,
                        fontWeight    = FontWeight.Black,
                        modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
