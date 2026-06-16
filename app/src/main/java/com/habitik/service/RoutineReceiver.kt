package com.habitik.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.habitik.data.RoutineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class RoutineReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: RoutineRepository

    @Inject
    lateinit var taskRepository: com.habitik.data.repository.TaskRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var taskLogRepository: com.habitik.data.repository.TaskLogRepository

    @Inject
    lateinit var settingsManager: com.habitik.data.SettingsManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("RoutineReceiver", "onReceive: action=$action")

        when (action) {

            // ── Pre-alert: fires reminderMin minutes before task starts ─────────────
            AlarmScheduler.ACTION_ROUTINE_ALARM -> {
                val taskId     = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1)
                val taskName   = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME) ?: "Routine"
                val reminderMin = intent.getIntExtra(AlarmScheduler.EXTRA_REMINDER_MIN, 5)

                Log.d("RoutineReceiver", "Pre-alert → task='$taskName' id=$taskId in ${reminderMin}m")

                CoroutineScope(Dispatchers.IO).launch {
                    val task = taskRepository.getTaskById(taskId)
                    if (task != null && task.isActive) {
                        if (settingsManager.notificationsEnabled.value) {
                            notificationHelper.showNotification(
                                routineId = taskId,
                                title     = "⏰ Starting Soon: ${task.name}",
                                message   = "Your task starts in $reminderMin minute${if (reminderMin == 1) "" else "s"}. Get ready!"
                            )
                        }
                        alarmScheduler.scheduleTask(task)
                    } else {
                        Log.d("RoutineReceiver", "Skipping notification and cancelling ghost alarm for task id=$taskId")
                        alarmScheduler.cancelTask(taskId)
                    }
                }
            }

            // ── On-start alert: fires exactly at task startTime ────────────────────
            AlarmScheduler.ACTION_TASK_START -> {
                val taskId   = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1)
                val taskName = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME) ?: "Routine"
                val durationMin = intent.getIntExtra(AlarmScheduler.EXTRA_DURATION_MIN, 0)

                Log.d("RoutineReceiver", "Start-alert → task='$taskName' id=$taskId duration=$durationMin")

                CoroutineScope(Dispatchers.IO).launch {
                    val task = taskRepository.getTaskById(taskId)
                    if (task != null && task.isActive) {
                        if (settingsManager.notificationsEnabled.value) {
                            notificationHelper.showNotification(
                                routineId = taskId + 20000,
                                title     = "🏃 Time to Start: ${task.name}",
                                message   = "Your task is scheduled for now (${task.startTime}). Tap to open and start focusing!"
                            )
                        }
                        alarmScheduler.scheduleTask(task)
                    } else {
                        Log.d("RoutineReceiver", "Skipping notification and cancelling ghost alarm for task id=$taskId")
                        alarmScheduler.cancelTask(taskId)
                    }
                }
            }

            // ── Mark Done action from notification button ───────────────────────────
            "ACTION_MARK_DONE" -> {
                val routineId = intent.getIntExtra("ROUTINE_ID", -1)
                if (routineId != -1) {
                    Log.d("RoutineReceiver", "Marking routine $routineId as done")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val task = taskRepository.getTaskById(routineId)
                            if (task != null) {
                                val date = java.time.LocalDate.now().toString()
                                val logs = taskLogRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
                                val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1

                                taskLogRepository.insertLog(
                                    com.habitik.data.entity.TaskLogEntity(
                                        taskId = task.id,
                                        logDate = date,
                                        status = "DONE",
                                        doneAt = System.currentTimeMillis(),
                                        occurrence = if (task.measurementType == "COUNT") nextOccurrence else 1,
                                        completedValue = if (task.measurementType == "COUNT") 1f else task.goalValue
                                    )
                                )
                                Log.d("RoutineReceiver", "TaskLog successfully written for task ${task.id}")
                            }
                            repository.markAsDone(routineId, true)
                        } catch (e: Exception) {
                            Log.e("RoutineReceiver", "Error saving task log: ${e.message}", e)
                        }
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.cancel(routineId + 20000)
                    notificationManager.cancel(routineId)
                }
            }

            "ACTION_PAUSE_TIMER" -> {
                val taskId = intent.getIntExtra("TASK_ID", -1)
                if (taskId != -1) {
                    Log.d("RoutineReceiver", "Pausing timer from notification for task $taskId")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val task = taskRepository.getTaskById(taskId)
                            if (task != null) {
                                val date = java.time.LocalDate.now().toString()
                                val log = taskLogRepository.getLatestLogForTask(task.id, date)
                                val remainingSeconds: Long
                                if (log?.status == "RESUMED" && log.startedAt != null) {
                                    val instant = java.time.Instant.ofEpochMilli(log.startedAt)
                                    val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                                    val totalSecs = log.remainingDurationSec ?: (log.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                                    val endAt = startedAt.plusSeconds(totalSecs)
                                    remainingSeconds = Math.max(0, java.time.temporal.ChronoUnit.SECONDS.between(java.time.LocalTime.now(), endAt))
                                } else {
                                    remainingSeconds = task.durationMin * 60L
                                }

                                taskLogRepository.insertLog(
                                    com.habitik.data.entity.TaskLogEntity(
                                        taskId = task.id,
                                        logDate = date,
                                        status = "PAUSED",
                                        remainingDurationMin = (remainingSeconds / 60).toInt(),
                                        remainingDurationSec = remainingSeconds
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("RoutineReceiver", "Error pausing timer: ${e.message}", e)
                        }
                    }
                    context.stopService(Intent(context, TimerForegroundService::class.java))
                }
            }

            "ACTION_DONE_TIMER" -> {
                val taskId = intent.getIntExtra("TASK_ID", -1)
                if (taskId != -1) {
                    Log.d("RoutineReceiver", "Completing timer from notification for task $taskId")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val task = taskRepository.getTaskById(taskId)
                            if (task != null) {
                                val date = java.time.LocalDate.now().toString()
                                taskLogRepository.insertLog(
                                    com.habitik.data.entity.TaskLogEntity(
                                        taskId = task.id,
                                        logDate = date,
                                        status = "DONE",
                                        doneAt = System.currentTimeMillis(),
                                        occurrence = 1,
                                        completedValue = task.goalValue
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("RoutineReceiver", "Error completing task: ${e.message}", e)
                        }
                    }
                    context.stopService(Intent(context, TimerForegroundService::class.java))
                }
            }
        }
    }
}
