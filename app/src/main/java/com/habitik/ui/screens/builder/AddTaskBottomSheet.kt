package com.habitik.ui.screens.builder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitik.data.entity.TaskEntity
import com.habitik.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    taskToEdit: TaskEntity? = null,
    initialStartTime: String? = null,
    existingTasks: List<TaskEntity> = emptyList(),
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
    var isAnytime        by remember { mutableStateOf(taskToEdit?.startTime == "ANYTIME") }

    // Frequency state
    val freqOptions = listOf("ONCE", "DAILY", "WEEKDAYS", "WEEKENDS", "CUSTOM")
    var frequency by remember { 
        mutableStateOf(
            if (taskToEdit?.repeatDays?.startsWith("[") == true) "CUSTOM" 
            else taskToEdit?.repeatDays ?: "DAILY"
        ) 
    }
    var selectedDays by remember { 
        mutableStateOf(
            if (taskToEdit?.repeatDays?.startsWith("[") == true) 
                taskToEdit.repeatDays.removeSurrounding("[", "]").split(",").map { it.trim().removeSurrounding("\"") }.toSet()
            else emptySet<String>()
        ) 
    }

    // Measurement state
    var measurementType by remember { mutableStateOf(taskToEdit?.measurementType ?: "TIME") }
    var repeatCount by remember { mutableStateOf(taskToEdit?.repeatCount?.toFloat() ?: 1f) }
    var goalValue by remember { mutableStateOf(taskToEdit?.goalValue?.toString() ?: "") }
    var goalUnit by remember { mutableStateOf(taskToEdit?.goalUnit ?: "") }
    var quantityIncrement by remember { mutableStateOf(taskToEdit?.quantityIncrement?.toString() ?: "1.0") }

    // Auto-toggle anytime for new count/quantity tasks
    LaunchedEffect(measurementType) {
        if (taskToEdit == null) {
            isAnytime = (measurementType == "COUNT" || measurementType == "QUANTITY")
        }
    }

    // Manual entry states for syncing
    var durationText by remember { mutableStateOf(duration.toInt().toString()) }
    var repeatCountText by remember { mutableStateOf(repeatCount.toInt().toString()) }

    // Sync manual text when slider moves
    LaunchedEffect(duration) {
        if (duration.toInt().toString() != durationText) {
            durationText = duration.toInt().toString()
        }
    }
    LaunchedEffect(repeatCount) {
        if (repeatCount.toInt().toString() != repeatCountText) {
            repeatCountText = repeatCount.toInt().toString()
        }
    }

    val hasEditTime = taskToEdit?.startTime != null && taskToEdit.startTime != "ANYTIME"
    val initialHour = (if (hasEditTime) taskToEdit?.startTime else initialStartTime)?.split(":")?.get(0)?.toIntOrNull() ?: 8
    val initialMinute = (if (hasEditTime) taskToEdit?.startTime else initialStartTime)?.split(":")?.get(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = true
    )

    // Conflict detection
    val conflictingTask by remember(timePickerState.hour, timePickerState.minute, duration, existingTasks, measurementType, isAnytime) {
        derivedStateOf {
            if (isAnytime) return@derivedStateOf null
            if (measurementType != "TIME") return@derivedStateOf null
            val newStart = timePickerState.hour * 60 + timePickerState.minute
            val newEnd = newStart + duration.toInt()
            existingTasks.find { t ->
                if (t.id == taskToEdit?.id) return@find false
                if (t.measurementType != "TIME") return@find false
                if (t.startTime == "ANYTIME") return@find false
                val shareDay = checkDayOverlap(frequency, selectedDays, t.repeatDays)
                if (!shareDay) return@find false

                val parts = t.startTime.split(":")
                val tStart = parts[0].toInt() * 60 + parts[1].toInt()
                val tEnd = tStart + t.durationMin
                (newStart < tEnd && newEnd > tStart)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        dragHandle       = {
            Box(
                Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape          = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Sheet title ───────────────────────────────────────────────
            Text(
                if (taskToEdit == null) "Create Task" else "Edit Task",
                style         = MaterialTheme.typography.displaySmall,
                fontWeight    = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                color         = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))

            // ── Task Name ─────────────────────────────────────────────────
            SectionLabel("WHAT ARE YOU DOING?")
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                TextField(
                    value         = name,
                    onValueChange = { name = it },
                    placeholder   = {
                        Text(
                            "e.g., Morning Meditations, Gym, Study...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(20.dp))

            // ── Card 1: Details (Category & Frequency) ────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Category
                    SectionLabel("CATEGORY")
                    Spacer(Modifier.height(10.dp))
                    val categories = listOf("WORK", "HEALTH", "PERSONAL", "FAMILY", "SPIRITUAL", "OTHER")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val catColor  = getCategoryColor(cat)
                            val selected  = selectedCategory == cat
                            Surface(
                                onClick  = { selectedCategory = cat },
                                shape    = RoundedCornerShape(14.dp),
                                color    = if (selected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                border   = BorderStroke(
                                    1.dp,
                                    if (selected) catColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                ),
                                modifier = if (selected) Modifier.shadow(6.dp, RoundedCornerShape(14.dp), spotColor = catColor.copy(alpha = 0.3f))
                                           else Modifier
                            ) {
                                Row(
                                    modifier          = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                                        color = if (selected) catColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // Frequency
                    SectionLabel("FREQUENCY")
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(freqOptions) { opt ->
                            val selected = frequency == opt
                            FilterChip(
                                selected = selected,
                                onClick = { frequency = opt },
                                label = { Text(opt) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    
                    if (frequency == "CUSTOM") {
                        Spacer(Modifier.height(12.dp))
                        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(days) { day ->
                                val selected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .shadow(if (selected) 4.dp else 0.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                        .clip(CircleShape)
                                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .border(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                                        .clickable { 
                                            selectedDays = if (selected) selectedDays - day else selectedDays + day
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.take(1),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // ── Card 2: Measurement & Timing ──────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("MEASUREMENT")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TIME", "COUNT", "QUANTITY").forEach { type ->
                            val selected = measurementType == type
                            val activeColor = MaterialTheme.colorScheme.secondary
                            Surface(
                                onClick = { measurementType = type },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                border = BorderStroke(1.dp, if (selected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        when(type) {
                                            "TIME" -> Icons.Default.Timer
                                            "COUNT" -> Icons.Default.Repeat
                                            else -> Icons.Default.BarChart
                                        },
                                        contentDescription = null,
                                        tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        type,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Conflict warning (only for TIME)
                    if (measurementType == "TIME" && conflictingTask != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Conflict Detected", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text("Overlaps with '${conflictingTask!!.name}'", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }

                    // Anytime Toggle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Anytime / All Day", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("No fixed start hour required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isAnytime,
                            onCheckedChange = { isAnytime = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.secondary)
                        )
                    }

                    if (!isAnytime) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        
                        // Timing fields
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                onClick  = { showTimePicker = true },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(16.dp),
                                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("START TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                                        style         = MaterialTheme.typography.titleMedium,
                                        fontWeight    = FontWeight.Black,
                                        color         = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            if (measurementType == "TIME") {
                                // Duration field
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(16.dp),
                                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("DURATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            BasicTextField(
                                                value = durationText,
                                                onValueChange = { 
                                                    durationText = it
                                                    it.toIntOrNull()?.let { valNew -> 
                                                        duration = valNew.toFloat().coerceIn(1f, 1440f) 
                                                    }
                                                },
                                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color      = MaterialTheme.colorScheme.onBackground
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.width(IntrinsicSize.Min)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "min (${formatDuration(duration.toInt())})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        if (measurementType == "TIME") {
                            Spacer(Modifier.height(10.dp))
                            Slider(
                                value = duration,
                                onValueChange = { duration = it },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    if (measurementType == "COUNT") {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            shape  = RoundedCornerShape(16.dp),
                            color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                Text("TIMES PER DAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    BasicTextField(
                                        value = repeatCountText,
                                        onValueChange = { 
                                            repeatCountText = it
                                            it.toIntOrNull()?.let { valNew -> 
                                                repeatCount = valNew.toFloat().coerceIn(1f, 100f) 
                                            }
                                        },
                                        textStyle = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color      = MaterialTheme.colorScheme.onBackground
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(IntrinsicSize.Min)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "TIMES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Slider(
                                    value = repeatCount,
                                    onValueChange = { repeatCount = it },
                                    valueRange = 1f..20f,
                                    steps = 19,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    } else if (measurementType == "QUANTITY") {
                        Spacer(Modifier.height(16.dp))

                        // ── Goal Amount row ────────────────────────────────
                        Surface(
                            shape  = RoundedCornerShape(16.dp),
                            color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("GOAL AMOUNT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicTextField(
                                        value = goalValue,
                                        onValueChange = { goalValue = it },
                                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (goalUnit.isNotEmpty()) {
                                        Text(
                                            goalUnit,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Unit chips ────────────────────────────────────
                        val unitOptions = listOf("ml", "l", "km", "m", "cups", "pages", "g", "kg", "cal", "times")
                        var isCustomUnit by remember { mutableStateOf(goalUnit.isNotEmpty() && goalUnit !in unitOptions) }
                        var customUnitText by remember { mutableStateOf(if (isCustomUnit) goalUnit else "") }

                        SectionLabel("UNIT")
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(unitOptions) { unit ->
                                val selected = !isCustomUnit && goalUnit == unit
                                Surface(
                                    onClick = {
                                        goalUnit = unit
                                        isCustomUnit = false
                                    },
                                    shape  = RoundedCornerShape(12.dp),
                                    color  = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Text(
                                        unit,
                                        style      = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                        color      = if (selected) MaterialTheme.colorScheme.secondary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            item {
                                // Custom chip
                                Surface(
                                    onClick = { isCustomUnit = true },
                                    shape  = RoundedCornerShape(12.dp),
                                    color  = if (isCustomUnit) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCustomUnit) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (isCustomUnit) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Custom",
                                            style      = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isCustomUnit) FontWeight.Black else FontWeight.SemiBold,
                                            color      = if (isCustomUnit) MaterialTheme.colorScheme.primary
                                                         else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (isCustomUnit) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape  = RoundedCornerShape(14.dp),
                                color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    BasicTextField(
                                        value = customUnitText,
                                        onValueChange = {
                                            customUnitText = it
                                            goalUnit = it
                                        },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        decorationBox = { inner ->
                                            if (customUnitText.isEmpty()) {
                                                Text("Enter unit (e.g. km, steps, oz…)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                            }
                                            inner()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Log Increment ─────────────────────────────────
                        SectionLabel("LOG INCREMENT")
                        Spacer(Modifier.height(8.dp))

                        // Compute sensible preset increments based on unit
                        val incrementPresets = remember(goalUnit) {
                            when (goalUnit.lowercase()) {
                                "ml"    -> listOf("50", "100", "150", "200", "250", "500")
                                "l"     -> listOf("0.1", "0.25", "0.5", "1")
                                "km"    -> listOf("0.5", "1", "2", "5", "10")
                                "m"     -> listOf("10", "50", "100", "200", "500")
                                "g"     -> listOf("10", "25", "50", "100", "250")
                                "kg"    -> listOf("0.1", "0.25", "0.5", "1")
                                "cal"   -> listOf("50", "100", "200", "250", "500")
                                "pages" -> listOf("1", "5", "10", "20")
                                "cups"  -> listOf("0.5", "1", "2")
                                "times" -> listOf("1", "2", "5", "10")
                                else    -> listOf("1", "5", "10", "25", "50")
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(incrementPresets) { preset ->
                                val selected = quantityIncrement == preset
                                Surface(
                                    onClick = { quantityIncrement = preset },
                                    shape  = RoundedCornerShape(12.dp),
                                    color  = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Text(
                                        "$preset ${goalUnit}",
                                        style      = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                        color      = if (selected) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        // Manual override field
                        Surface(
                            shape  = RoundedCornerShape(14.dp),
                            color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(10.dp))
                                BasicTextField(
                                    value = quantityIncrement,
                                    onValueChange = { quantityIncrement = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    decorationBox = { inner ->
                                        if (quantityIncrement.isEmpty()) {
                                            Text("Custom increment (e.g. 250)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (goalUnit.isNotEmpty()) {
                                    Text(
                                        goalUnit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // ── Card 3: Preferences & Alerts ──────────────────────────────
            Surface(
                shape  = RoundedCornerShape(24.dp),
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Important toggle
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape    = CircleShape,
                                color    = if (isImportant) Color(0xFFFFB800).copy(alpha = 0.15f)
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (isImportant) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint               = if (isImportant) Color(0xFFFFB800)
                                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Important Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Boosts your streaks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked         = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (!isAnytime) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                        // Pre-alert row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape    = CircleShape,
                                    color    = MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint               = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
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
                                modifier      = Modifier.width(100.dp),
                                colors        = SliderDefaults.colors(
                                    thumbColor       = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))

            // ── Save Button ───────────────────────────────────────────────
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = primaryColor.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (name.isNotBlank())
                            Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
                        else
                            SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    .border(
                        1.dp,
                        if (name.isNotBlank()) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled = name.isNotBlank()) {
                        val repeatDaysStr = if (frequency == "CUSTOM") {
                            selectedDays.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
                        } else frequency

                        val task = TaskEntity(
                            id          = taskToEdit?.id ?: 0,
                            name        = name,
                            category    = selectedCategory,
                            colorHex    = "",
                            startTime   = if (isAnytime) "ANYTIME" else String.format("%02d:%02d", timePickerState.hour, timePickerState.minute),
                            durationMin = if (isAnytime) 5 else duration.toInt(),
                            isFlexible  = false,
                            flexWindowEnd = null,
                            repeatDays  = repeatDaysStr,
                            repeatCount = repeatCount.toInt(),
                            measurementType = measurementType,
                            goalValue = goalValue.toFloatOrNull() ?: 0f,
                            goalUnit = goalUnit,
                            isImportant = isImportant,
                            reminderMin = if (isAnytime) 0 else reminderMin.toInt(),
                            quantityIncrement = quantityIncrement.toFloatOrNull() ?: 1f
                        )
                        onSave(task)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "SAVE TASK",
                    style         = MaterialTheme.typography.titleMedium,
                    fontWeight    = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color         = if (name.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton    = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CONFIRM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        ) {
            TimePicker(
                state  = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor          = MaterialTheme.colorScheme.surfaceVariant,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectorColor           = MaterialTheme.colorScheme.primary,
                    periodSelectorBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}


private fun formatDuration(mins: Int): String {
    val h = mins / 60
    val m = mins % 60
    return if (h > 0) {
        if (m > 0) "${h}h ${m}m" else "${h}h"
    } else {
        "$m min"
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun checkDayOverlap(freq1: String, customDays1: Set<String>, freq2: String): Boolean {
    val days1 = getDaysFromFreq(freq1, customDays1)
    val days2 = getDaysFromFreq(freq2, emptySet()) // freq2 is from DB, could be "DAILY" or JSON
    
    // If either is DAILY, they overlap
    if (freq1 == "DAILY" || freq2 == "DAILY") return true
    
    // If freq1 is ONCE, it overlaps if today is in freq2's days
    if (freq1 == "ONCE") {
        val today = java.time.LocalDate.now().dayOfWeek.name.take(3)
        return getDaysFromFreq(freq2, emptySet()).contains(today)
    }
    
    // Simple intersection
    return days1.intersect(days2).isNotEmpty()
}

private fun getDaysFromFreq(freq: String, custom: Set<String>): Set<String> {
    return when(freq) {
        "ONCE"     -> setOf(java.time.LocalDate.now().dayOfWeek.name.take(3))
        "DAILY"    -> setOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        "WEEKDAYS" -> setOf("MON", "TUE", "WED", "THU", "FRI")
        "WEEKENDS" -> setOf("SAT", "SUN")
        "CUSTOM"   -> custom
        else -> {
            if (freq.startsWith("[")) {
                freq.removeSurrounding("[", "]").split(",").map { it.trim().removeSurrounding("\"") }.toSet()
            } else setOf()
        }
    }
}

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
        containerColor   = MaterialTheme.colorScheme.surface
    )
}
