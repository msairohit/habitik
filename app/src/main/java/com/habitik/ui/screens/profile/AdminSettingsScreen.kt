package com.habitik.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.theme.getCategoryColor
import com.habitik.ui.theme.getCategoryEmoji
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    
    val notificationsEnabled by viewModel.settingsManager.notificationsEnabled
    val snoozeDurationMin by viewModel.settingsManager.snoozeDurationMin
    val isVibrationEnabled by viewModel.settingsManager.isVibrationEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin Portal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "System Notifications & Settings Console",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 50.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                
                // Section 1: Global System Settings
                item {
                    Text(
                        text = "Global App Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Notifications Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Push Notifications", fontWeight = FontWeight.Bold)
                                        Text("Toggle all routine alerts globally", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { viewModel.settingsManager.setNotificationsEnabled(it) }
                                )
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            
                            // Vibration Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Vibration Alert", fontWeight = FontWeight.Bold)
                                        Text("Haptic alarms on start triggers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = isVibrationEnabled,
                                    onCheckedChange = { viewModel.settingsManager.setVibrationEnabled(it) }
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

                            // Snooze Duration Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Snooze, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Snooze Duration", fontWeight = FontWeight.Bold)
                                            Text("Postpone notifications interval", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        text = "$snoozeDurationMin mins",
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Slider(
                                    value = snoozeDurationMin.toFloat(),
                                    onValueChange = { viewModel.settingsManager.setSnoozeDurationMin(it.toInt()) },
                                    valueRange = 1f..60f,
                                    steps = 11
                                )
                            }
                        }
                    }
                }

                // Section 2: Scheduled Alarms List
                item {
                    Text(
                        text = "Notification Trigger Registry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (tasks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "No active routines scheduled in system.",
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(tasks) { task ->
                        AdminTaskNotificationCard(
                            task = task,
                            onUpdateReminder = { min -> viewModel.updateTaskReminder(task, min) },
                            onToggleActive = { active -> viewModel.toggleTaskActive(task, active) },
                            onTestNotification = { isPreAlert -> viewModel.triggerTestNotification(task, isPreAlert) }
                        )
                    }
                }

                // Section 3: Developer Utilities
                item {
                    Text(
                        text = "System Management Console",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Reschedule All
                            Button(
                                onClick = {
                                    viewModel.forceRescheduleAll()
                                    Toast.makeText(context, "All system alarms rescheduled!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Rebuild & Reschedule All Alarms", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Seed Data
                            Button(
                                onClick = {
                                    viewModel.seedDemoData()
                                    Toast.makeText(context, "Mock routines added to database!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Seed Developer Demo Routines", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Reset App
                            Button(
                                onClick = {
                                    viewModel.resetDatabase()
                                    Toast.makeText(context, "System database reset completed!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Factory Reset Application DB", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTaskNotificationCard(
    task: TaskEntity,
    onUpdateReminder: (Int) -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onTestNotification: (Boolean) -> Unit
) {
    val categoryColor = getCategoryColor(task.category)
    val isAnytime = task.startTime == "ANYTIME"
    val startTime = if (isAnytime) LocalTime.MIDNIGHT else LocalTime.parse(task.startTime)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val formattedStartTime = if (isAnytime) "Anytime" else startTime.format(timeFormatter)
    
    val reminderText = if (isAnytime) {
        "Alarms Disabled"
    } else if (task.reminderMin > 0) {
        val triggerTime = startTime.minusMinutes(task.reminderMin.toLong()).format(timeFormatter)
        "Pre-alert: $triggerTime (${task.reminderMin}m lead)"
    } else "Pre-alert: Disabled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Task Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCategoryEmoji(task.category), fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(task.name, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isAnytime) "Anytime / All Day Goal" else "Starts at $formattedStartTime · $reminderText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Alarm enable switch (disable for anytime tasks since they don't have alarms anyway)
                if (!isAnytime) {
                    Switch(
                        checked = task.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(checkedThumbColor = categoryColor, checkedTrackColor = categoryColor.copy(alpha = 0.5f))
                    )
                }
            }
            
            if (!isAnytime) {
                Spacer(Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp))
                
                // Adjust Lead offset Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pre-Alert Offset", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (task.reminderMin > 0) "${task.reminderMin} mins before" else "None (Start alert only)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = categoryColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Slider(
                        value = task.reminderMin.toFloat(),
                        onValueChange = { onUpdateReminder(it.toInt()) },
                        valueRange = 0f..30f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = categoryColor, activeTrackColor = categoryColor)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Notification test triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test Pre alert
                    Button(
                        onClick = { onTestNotification(true) },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = task.reminderMin > 0 && task.isActive,
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor.copy(alpha = 0.1f), contentColor = categoryColor)
                    ) {
                        Text("Test Alert", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // Test Start alert
                    Button(
                        onClick = { onTestNotification(false) },
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = task.isActive,
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor, contentColor = Color.White)
                    ) {
                        Text("Test Live Run", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
