package com.habitik.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.habitik.data.repository.TaskLogRepository
import com.habitik.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class TimerForegroundService : Service() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var logRepository: TaskLogRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 9999
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_NAME = "EXTRA_TASK_NAME"
        const val EXTRA_REMAINING_SECS = "EXTRA_REMAINING_SECS"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val taskId = intent?.getIntExtra(EXTRA_TASK_ID, -1) ?: -1
        val taskName = intent?.getStringExtra(EXTRA_TASK_NAME) ?: "Routine"
        val remainingSecs = intent?.getLongExtra(EXTRA_REMAINING_SECS, 0L) ?: 0L

        if (action == ACTION_START_TIMER && taskId != -1) {
            startTimer(taskId, taskName, remainingSecs)
        } else if (action == ACTION_STOP_TIMER) {
            stopTimer()
        }

        return START_NOT_STICKY
    }

    private fun startTimer(taskId: Int, taskName: String, initialSecs: Long) {
        timerJob?.cancel()
        var remainingSecs = initialSecs
        
        val initialNotification = notificationHelper.getTimerNotification(taskId, taskName, remainingSecs)
        startForeground(NOTIFICATION_ID, initialNotification)

        timerJob = serviceScope.launch {
            while (isActive && remainingSecs > 0) {
                delay(1000)
                remainingSecs--

                if (remainingSecs <= 0) {
                    val date = java.time.LocalDate.now().toString()
                    val task = taskRepository.getTaskById(taskId)
                    if (task != null) {
                        logRepository.insertLog(
                            com.habitik.data.entity.TaskLogEntity(
                                taskId = taskId,
                                logDate = date,
                                status = "DONE",
                                doneAt = System.currentTimeMillis(),
                                occurrence = 1,
                                completedValue = task.goalValue
                            )
                        )
                    }
                    stopTimer()
                    break
                }

                val notification = notificationHelper.getTimerNotification(taskId, taskName, remainingSecs)
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
