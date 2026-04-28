package com.habitik.ui.screens.builder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    taskToEdit: TaskEntity? = null,
    onDismiss : () -> Unit,
    onSave    : (TaskEntity) -> Unit
) {
    val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name             by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: "WORK") }
    var duration         by remember { mutableStateOf(taskToEdit?.durationMin?.toFloat() ?: 30f) }
    var isImportant      by remember { mutableStateOf(taskToEdit?.isImportant ?: false) }
    var reminderMin      by remember { mutableStateOf(taskToEdit?.reminderMin?.toFloat() ?: 5f) }
    var showTimePicker   by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour   = taskToEdit?.startTime?.split(":")?.get(0)?.toInt() ?: 8,
        initialMinute = taskToEdit?.startTime?.split(":")?.get(1)?.toInt() ?: 0,
        is24Hour      = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        dragHandle       = {
            // Custom drag handle – gradient pill
            Box(
                Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(GlowViolet, GlowCyan)))
            )
        },
        containerColor = BgSurface,
        shape          = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Sheet title ───────────────────────────────────────────────
            Text(
                if (taskToEdit == null) "Create Task" else "Edit Task",
                style         = MaterialTheme.typography.displaySmall,
                fontWeight    = FontWeight.Black,
                letterSpacing = (-1).sp,
                color         = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(28.dp))

            // ── Task Name ─────────────────────────────────────────────────
            SectionLabel("WHAT ARE YOU DOING?")
            Spacer(Modifier.height(8.dp))
            TextField(
                value         = name,
                onValueChange = { name = it },
                placeholder   = {
                    Text(
                        "Task name…",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                },
                modifier  = Modifier.fillMaxWidth(),
                colors    = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = GlowViolet,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                ),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )
            Spacer(Modifier.height(28.dp))

            // ── Category ──────────────────────────────────────────────────
            SectionLabel("CATEGORY")
            Spacer(Modifier.height(12.dp))
            val categories = listOf("WORK", "HEALTH", "PERSONAL", "FAMILY", "SPIRITUAL", "OTHER")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { cat ->
                    val catColor  = getCategoryColor(cat)
                    val selected  = selectedCategory == cat
                    Surface(
                        onClick  = { selectedCategory = cat },
                        shape    = RoundedCornerShape(18.dp),
                        color    = if (selected) catColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        border   = BorderStroke(
                            1.dp,
                            if (selected) catColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = if (selected) Modifier.shadow(8.dp, RoundedCornerShape(18.dp), spotColor = catColor.copy(alpha = 0.4f))
                                   else Modifier
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                cat,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                                color      = if (selected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))

            // ── Time & Duration ───────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Time picker trigger
                Surface(
                    onClick  = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(22.dp),
                    color    = Color.White.copy(alpha = 0.05f),
                    border   = BorderStroke(1.dp, GlowViolet.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("START TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                            style         = MaterialTheme.typography.titleLarge,
                            fontWeight    = FontWeight.Black,
                            color         = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                // Duration
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(22.dp),
                    color    = Color.White.copy(alpha = 0.05f),
                    border   = BorderStroke(1.dp, GlowCyan.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("DURATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${duration.toInt()} MIN",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Duration slider
            Slider(
                value         = duration,
                onValueChange = { duration = it },
                valueRange    = 15f..240f,
                steps         = 14,
                colors        = SliderDefaults.colors(
                    thumbColor        = GlowViolet,
                    activeTrackColor  = GlowViolet,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(Modifier.height(28.dp))

            // ── Options card ──────────────────────────────────────────────
            Surface(
                shape  = RoundedCornerShape(24.dp),
                color  = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Important toggle
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape    = CircleShape,
                                color    = if (isImportant) Color(0xFFFFB800).copy(alpha = 0.15f)
                                           else Color.White.copy(alpha = 0.07f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isImportant) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint               = if (isImportant) Color(0xFFFFB800)
                                                             else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Important Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Boosts your streaks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked         = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GlowViolet)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color.White.copy(alpha = 0.07f))

                    // Pre-alert row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape    = CircleShape,
                                color    = GlowCyan.copy(alpha = 0.13f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint               = GlowCyan
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Pre-alert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${reminderMin.toInt()} minutes before", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Slider(
                            value         = reminderMin,
                            onValueChange = { reminderMin = it },
                            valueRange    = 0f..30f,
                            steps         = 5,
                            modifier      = Modifier.width(110.dp),
                            colors        = SliderDefaults.colors(
                                thumbColor       = GlowCyan,
                                activeTrackColor = GlowCyan
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(36.dp))

            // ── Save Button ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = GlowViolet.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (name.isNotBlank())
                            Brush.horizontalGradient(listOf(GlowViolet, GlowCyan))
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = if (name.isNotBlank()) 0.2f else 0.05f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick  = {
                        val task = TaskEntity(
                            name        = name,
                            category    = selectedCategory,
                            colorHex    = "",
                            startTime   = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                            durationMin = duration.toInt(),
                            isFlexible  = false,
                            flexWindowEnd = null,
                            repeatDays  = "DAILY",
                            repeatCount = 1,
                            isImportant = isImportant,
                            reminderMin = reminderMin.toInt()
                        )
                        onSave(task)
                    },
                    modifier = Modifier.fillMaxSize(),
                    enabled  = name.isNotBlank()
                ) {
                    Text(
                        "SAVE TASK",
                        style         = MaterialTheme.typography.titleLarge,
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color         = Color.White
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton    = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CONFIRM", fontWeight = FontWeight.Bold, color = GlowViolet)
                }
            }
        ) {
            TimePicker(
                state  = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor          = BgCard,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectorColor           = GlowViolet,
                    periodSelectorBorderColor = GlowViolet
                )
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style         = MaterialTheme.typography.labelLarge,
        color         = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton   : @Composable () -> Unit,
    content         : @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton    = confirmButton,
        title            = { Text("Set Start Time", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
        text             = { content() },
        shape            = RoundedCornerShape(32.dp),
        containerColor   = BgCard
    )
}
