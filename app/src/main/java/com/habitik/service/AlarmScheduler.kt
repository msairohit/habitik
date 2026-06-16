package com.habitik.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.habitik.data.entity.TaskEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    companion object {
        const val ACTION_ROUTINE_ALARM = "ACTION_ROUTINE_ALARM"   // pre-alert
        const val ACTION_TASK_START    = "ACTION_TASK_START"      // on-start alert

        const val EXTRA_TASK_ID    = "TASK_ID"
        const val EXTRA_TASK_NAME  = "TASK_NAME"
        const val EXTRA_REMINDER_MIN = "REMINDER_MIN"
        const val EXTRA_DURATION_MIN = "DURATION_MIN"

        // Offset so pre-alert and start-alarm PendingIntent request codes never collide
        private const val START_ALARM_OFFSET = 10_000
    }

    /**
     * Schedule BOTH alarms for a task:
     *  1. Pre-alert  → startTime - reminderMin
     *  2. Start alert → startTime
     *
     * If the trigger time has already passed today it schedules for tomorrow.
     */
    fun scheduleTask(task: TaskEntity) {
        if (!task.isActive) {
            cancelTask(task.id)
            return
        }

        val startTime = runCatching { LocalTime.parse(task.startTime, timeFormatter) }.getOrNull()
        if (startTime == null) {
            Log.w("AlarmScheduler", "Invalid startTime '${task.startTime}' for task ${task.id}")
            return
        }

        val now = LocalDateTime.now()

        // ── 1. Pre-alert ──────────────────────────────────────────────────────────
        if (task.reminderMin > 0) {
            val preAlertAt = nextTrigger(
                startTime = startTime,
                now = now,
                task = task,
                isPreAlert = true
            )
            if (preAlertAt != null) {
                setAlarm(
                    triggerMillis = preAlertAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    pendingIntent = buildPendingIntent(
                        requestCode = task.id,
                        action      = ACTION_ROUTINE_ALARM,
                        task        = task
                    )
                )
                Log.d("AlarmScheduler", "Pre-alert scheduled for '${task.name}' at $preAlertAt (${task.reminderMin}m before)")
            }
        }

        // ── 2. Task-start alert ───────────────────────────────────────────────────
        val startAt = nextTrigger(
            startTime = startTime,
            now  = now,
            task = task,
            isPreAlert = false
        )
        if (startAt != null) {
            setAlarm(
                triggerMillis = startAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                pendingIntent = buildPendingIntent(
                    requestCode = task.id + START_ALARM_OFFSET,
                    action      = ACTION_TASK_START,
                    task        = task
                )
            )
            Log.d("AlarmScheduler", "Start-alert scheduled for '${task.name}' at $startAt")
        }
    }

    /** Cancel BOTH alarms (pre-alert + start) for a specific task. */
    fun cancelTask(taskId: Int) {
        cancelSingle(taskId,                   ACTION_ROUTINE_ALARM)
        cancelSingle(taskId + START_ALARM_OFFSET, ACTION_TASK_START)
    }

    /** Schedule alarms for ALL given tasks (called on app start / list refresh). */
    fun scheduleAll(tasks: List<TaskEntity>) {
        tasks.forEach { scheduleTask(it) }
    }

    // ── Private helpers ────────────────────────────────────────────────────────────

    /** Find the next valid trigger time based on task frequency/days. */
    private fun nextTrigger(startTime: LocalTime, now: LocalDateTime, task: TaskEntity, isPreAlert: Boolean): LocalDateTime? {
        var date = LocalDate.now()
        // Loop up to 8 days to find the next scheduled occurrence that is in the future
        for (i in 0..8) {
            if (isTaskScheduledForDay(task, date)) {
                val taskDateTime = LocalDateTime.of(date, startTime)
                val triggerDateTime = if (isPreAlert) {
                    taskDateTime.minusMinutes(task.reminderMin.toLong())
                } else {
                    taskDateTime
                }
                if (triggerDateTime.isAfter(now)) {
                    return triggerDateTime
                }
            }
            date = date.plusDays(1)
        }
        return null
    }

    private fun isTaskScheduledForDay(task: TaskEntity, date: LocalDate): Boolean {
        val dayName = date.dayOfWeek.name.take(3) // "MON", "TUE", etc.
        val freq = task.repeatDays
        
        return when {
            freq == "ONCE" -> date == LocalDate.now() // For ONCE, we only schedule for today
            freq == "DAILY" -> true
            freq == "WEEKDAYS" -> !setOf("SAT", "SUN").contains(dayName)
            freq == "WEEKENDS" -> setOf("SAT", "SUN").contains(dayName)
            freq.startsWith("[") -> freq.contains(dayName)
            else -> false
        }
    }

    private fun setAlarm(triggerMillis: Long, pendingIntent: PendingIntent) {
        try {
            // setAlarmClock is the only API that guarantees exact delivery out of Doze mode on modern Android
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException: ${e.message}")
            // Fallback if permission is somehow entirely revoked
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun cancelSingle(requestCode: Int, action: String) {
        val intent = Intent(context, RoutineReceiver::class.java).apply { this.action = action }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            Log.d("AlarmScheduler", "Cancelled alarm action=$action requestCode=$requestCode")
        }
    }

    private fun buildPendingIntent(requestCode: Int, action: String, task: TaskEntity): PendingIntent {
        val intent = Intent(context, RoutineReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_TASK_ID,     task.id)
            putExtra(EXTRA_TASK_NAME,   task.name)
            putExtra(EXTRA_REMINDER_MIN, task.reminderMin)
            putExtra(EXTRA_DURATION_MIN, task.durationMin)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
