package com.habitik.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitik.data.HabitikDatabase
import com.habitik.data.entity.TaskEntity
import com.habitik.data.entity.TaskLogEntity
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import android.content.Intent
import android.net.Uri
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.habitik.MainActivity

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = HabitikDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val taskLogDao = database.taskLogDao()

        provideContent {
            GlanceTheme {
                val date = LocalDate.now()
                val dayOfWeek = date.dayOfWeek.name.take(3) // MON, TUE, etc.
                
                // Get all active tasks for today
                val activeTasks by taskDao.getAllActiveTasks().collectAsState(initial = emptyList())
                val todayLogs by taskLogDao.getLogsForDate(date.toString()).collectAsState(initial = emptyList())

                // Filter tasks matching today's repeatDays
                val dayTasks = activeTasks.filter { task ->
                    val taskCreationDate = java.time.Instant.ofEpochMilli(task.createdAt)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    if (date.isBefore(taskCreationDate)) return@filter false
                    
                    when {
                        task.repeatDays == "ONCE" -> {
                            taskCreationDate == date
                        }
                        task.repeatDays == "DAILY" -> true
                        task.repeatDays == "WEEKDAYS" -> !setOf("SAT", "SUN").contains(dayOfWeek)
                        task.repeatDays == "WEEKENDS" -> setOf("SAT", "SUN").contains(dayOfWeek)
                        task.repeatDays.startsWith("[") -> task.repeatDays.contains(dayOfWeek)
                        else -> false
                    }
                }.sortedBy { it.startTime }

                val completedCount = dayTasks.count { task ->
                    isTaskFullyCompleted(task, todayLogs.filter { it.taskId == task.id })
                }
                
                WidgetContent(
                    tasks = dayTasks,
                    logs = todayLogs,
                    completedCount = completedCount,
                    totalCount = dayTasks.size
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        tasks: List<TaskEntity>,
        logs: List<TaskLogEntity>,
        completedCount: Int,
        totalCount: Int
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E1E1E))) // Modern dark-themed container
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Habitik Daily",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "$completedCount / $totalCount Completed",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB0B0B0)),
                            fontSize = 12.sp
                        )
                    )
                }
                
                // Refresh Button
                Button(
                    text = "↻",
                    onClick = actionRunCallback<RefreshAction>(),
                    modifier = GlanceModifier.size(36.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks for today! 🍃",
                        style = TextStyle(color = ColorProvider(Color.Gray))
                    )
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(tasks) { task ->
                        val taskLogs = logs.filter { it.taskId == task.id }
                        val isDone = isTaskFullyCompleted(task, taskLogs)
                        
                        TaskItemRow(task = task, taskLogs = taskLogs, isDone = isDone)
                    }
                }
            }
        }
    }

    @Composable
    private fun TaskItemRow(task: TaskEntity, taskLogs: List<TaskLogEntity>, isDone: Boolean) {
        val context = LocalContext.current
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("habitik://task_detail/${task.id}")
        ).apply {
            setClass(context, MainActivity::class.java)
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(ColorProvider(Color(0xFF2C2C2C)))
                .padding(8.dp)
                .clickable(actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Name and Emoji
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = getCategoryEmoji(task.category) + " " + task.name,
                    style = TextStyle(
                        color = if (isDone) ColorProvider(Color.Gray) else ColorProvider(Color.White),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                
                // Progress Description
                val progressText = getTaskProgressText(task, taskLogs)
                Text(
                    text = "${task.startTime} · $progressText",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFB0B0B0)),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Action Buttons
            if (isDone) {
                Text(
                    text = "✓",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF4CAF50)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.measurementType != "TIME") {
                        Button(
                            text = "+",
                            onClick = actionRunCallback<IncrementAction>(
                                actionParametersOf(
                                    TaskIdKey to task.id
                                )
                            ),
                            modifier = GlanceModifier.size(32.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                    Button(
                        text = "Done",
                        onClick = actionRunCallback<CompleteAction>(
                            actionParametersOf(
                                TaskIdKey to task.id
                            )
                        ),
                        modifier = GlanceModifier.height(32.dp)
                    )
                }
            }
        }
    }

    private fun isTaskFullyCompleted(task: TaskEntity, logs: List<TaskLogEntity>): Boolean {
        if (task.measurementType == "TIME") {
            val latestLog = logs.maxByOrNull { it.id }
            return latestLog?.status == "DONE"
        }
        
        return when (task.measurementType) {
            "COUNT" -> {
                val doneCount = logs.count { it.status == "DONE" }
                doneCount >= task.repeatCount
            }
            "QUANTITY" -> {
                val totalDone = logs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                totalDone >= task.goalValue
            }
            else -> false
        }
    }

    private fun getTaskProgressText(task: TaskEntity, logs: List<TaskLogEntity>): String {
        val latestLog = logs.maxByOrNull { it.id }
        if (latestLog?.status == "SKIPPED") return "Skipped"
        
        if (task.measurementType == "TIME") {
            if (latestLog?.status == "DONE") return "DONE"
            val remainingSecs = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
            val completedSecs = Math.max(0L, (task.durationMin * 60L) - remainingSecs)
            val completedMins = completedSecs / 60
            return "$completedMins/${task.durationMin} mins"
        }
        
        val isDone = isTaskFullyCompleted(task, logs)
        if (isDone) return "DONE"

        return when (task.measurementType) {
            "COUNT" -> {
                val doneCount = logs.count { it.status == "DONE" }
                "$doneCount/${task.repeatCount} times"
            }
            "QUANTITY" -> {
                val totalDone = logs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                val goalStr = if (task.goalValue % 1 == 0f) task.goalValue.toInt().toString() else task.goalValue.toString()
                "$doneStr/$goalStr ${task.goalUnit}"
            }
            else -> "Pending"
        }
    }

    private fun getCategoryEmoji(category: String): String {
        return when (category.uppercase()) {
            "HEALTH" -> "🥗"
            "WORK" -> "💻"
            "PERSONAL" -> "🧘"
            "FAMILY" -> "🏠"
            "SPIRITUAL" -> "🙏"
            else -> "🎯"
        }
    }
}

val TaskIdKey = ActionParameters.Key<Int>("task_id")

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        HabitWidget().updateAll(context)
    }
}

class CompleteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        val database = HabitikDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val taskLogDao = database.taskLogDao()
        
        val task = taskDao.getTaskById(taskId) ?: return
        val date = LocalDate.now().toString()
        
        // Insert a DONE log entry
        val newLog = TaskLogEntity(
            taskId = taskId,
            logDate = date,
            status = "DONE",
            doneAt = System.currentTimeMillis(),
            completedValue = if (task.measurementType == "QUANTITY") task.goalValue else null,
            remainingDurationSec = 0
        )
        taskLogDao.insertLog(newLog)
        
        // Refresh all widget instances
        HabitWidget().updateAll(context)
    }
}

class IncrementAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdKey] ?: return
        val database = HabitikDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val taskLogDao = database.taskLogDao()
        
        val task = taskDao.getTaskById(taskId) ?: return
        val date = LocalDate.now().toString()
        
        val logs = taskLogDao.getLogsForTask(taskId).first().filter { it.logDate == date }
        val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1

        if (task.measurementType == "COUNT") {
            // For COUNT tasks, each increment registers a new log with DONE status
            val newLog = TaskLogEntity(
                taskId = taskId,
                logDate = date,
                status = "DONE",
                doneAt = System.currentTimeMillis(),
                occurrence = nextOccurrence,
                completedValue = 1f
            )
            taskLogDao.insertLog(newLog)
        } else if (task.measurementType == "QUANTITY") {
            // For QUANTITY tasks, add increment
            val newLog = TaskLogEntity(
                taskId = taskId,
                logDate = date,
                status = "DONE",
                doneAt = System.currentTimeMillis(),
                occurrence = nextOccurrence,
                completedValue = task.quantityIncrement
            )
            taskLogDao.insertLog(newLog)
        }
        
        // Refresh all widget instances
        HabitWidget().updateAll(context)
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}

object HabitWidgetHelper {
    suspend fun updateAll(context: Context) {
        HabitWidget().updateAll(context)
    }
}
