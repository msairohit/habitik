package com.habitik.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Int,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateToConcentration: (Int) -> Unit = {},
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    if (uiState.task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val task = uiState.task!!
    val color = getCategoryColor(task.category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Big Icon/Emoji
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryEmoji(task.category), fontSize = 64.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = task.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = task.category.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Info Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailInfoItem(
                    icon = Icons.Default.Schedule,
                    label = "Scheduled",
                    value = task.startTime,
                    modifier = Modifier.weight(1f)
                )
                DetailInfoItem(
                    icon = if (task.measurementType == "COUNT") Icons.Default.Repeat 
                           else if (task.measurementType == "QUANTITY") Icons.Default.Straighten
                           else Icons.Default.Timer,
                    label = if (task.measurementType == "COUNT") "Goal" 
                            else if (task.measurementType == "QUANTITY") "Goal"
                            else "Duration",
                    value = if (task.measurementType == "COUNT") "${task.repeatCount} times"
                            else if (task.measurementType == "QUANTITY") "${task.goalValue} ${task.goalUnit}"
                            else "${task.durationMin}m",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailInfoItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Status",
                    value = uiState.status,
                    modifier = Modifier.weight(1f),
                    valueColor = if (uiState.status == "DONE") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
                if (task.measurementType == "TIME") {
                    DetailInfoItem(
                        icon = Icons.Default.HourglassEmpty,
                        label = "Pending",
                        value = uiState.remainingTime,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    DetailInfoItem(
                        icon = Icons.Default.BarChart,
                        label = "Type",
                        value = task.measurementType,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(uiState.progressText, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = uiState.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }

            Spacer(Modifier.weight(1f))

            // Action Button
            when {
                uiState.status == "SKIPPED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text("SKIPPED", color = Color.Gray, fontWeight = FontWeight.Black)
                        }
                    }
                }
                uiState.status == "DONE" || uiState.progress >= 1f -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("COMPLETED", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black)
                        }
                    }
                }
                else -> {
                    if (task.measurementType == "TIME") {
                        Button(
                            onClick = {
                                if (uiState.isFocusing) viewModel.onPauseFocus()
                                else viewModel.onStartFocus()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isFocusing) Color.White.copy(alpha = 0.1f) else color,
                                contentColor = if (uiState.isFocusing) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                        ) {
                            Icon(
                                if (uiState.isFocusing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (uiState.isFocusing) "PAUSE FOCUS" else "START FOCUS",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { onNavigateToConcentration(task.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen Focus",
                                tint = color
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "FULLSCREEN CONCENTRATION",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = color
                            )
                        }
                    } else if (task.measurementType == "COUNT") {
                        Button(
                            onClick = { viewModel.onIncrementTask(1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("INCREMENT", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    } else if (task.measurementType == "QUANTITY") {
                        var showCustomDialog by remember { mutableStateOf(false) }
                        var customValueText by remember { mutableStateOf("") }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val unit = task.goalUnit.lowercase()
                                val baseIncrement = if (task.quantityIncrement > 0f) task.quantityIncrement else 1f
                                val amounts = when {
                                    task.quantityIncrement > 0f && task.quantityIncrement != 1f -> {
                                        listOf(baseIncrement, baseIncrement * 2f, baseIncrement * 4f)
                                    }
                                    unit.contains("ml") || unit.contains("water") -> listOf(100f, 250f, 500f)
                                    unit.contains("g") || unit.contains("kcal") -> listOf(50f, 100f, 200f)
                                    else -> listOf(1f, 5f, 10f)
                                }
                                
                                amounts.forEach { amount ->
                                    OutlinedButton(
                                        onClick = { viewModel.onIncrementTask(amount) },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.3f))
                                    ) {
                                        val displayVal = if (amount % 1f == 0f) amount.toInt().toString() else amount.toString()
                                        Text("+$displayVal", fontWeight = FontWeight.Bold, color = color)
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showCustomDialog = true },
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = color)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("ADD CUSTOM", fontWeight = FontWeight.Black)
                            }
                        }

                        if (showCustomDialog) {
                            AlertDialog(
                                onDismissRequest = { showCustomDialog = false },
                                title = { Text("Add Custom ${task.goalUnit}", fontWeight = FontWeight.Black) },
                                text = {
                                    OutlinedTextField(
                                        value = customValueText,
                                        onValueChange = { customValueText = it },
                                        label = { Text("Amount") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            customValueText.toFloatOrNull()?.let {
                                                viewModel.onIncrementTask(it)
                                            }
                                            showCustomDialog = false
                                            customValueText = ""
                                        }
                                    ) {
                                        Text("ADD", fontWeight = FontWeight.Bold, color = color)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCustomDialog = false }) {
                                        Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = { viewModel.onDoneTask() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MARK AS COMPLETE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
