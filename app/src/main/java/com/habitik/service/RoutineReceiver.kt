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
import javax.inject.Inject

@AndroidEntryPoint
class RoutineReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: RoutineRepository

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

                NotificationHelper(context).showNotification(
                    routineId = taskId,
                    title     = "⏰ Starting Soon: $taskName",
                    message   = "Your task starts in $reminderMin minute${if (reminderMin == 1) "" else "s"}. Get ready!"
                )
            }

            // ── On-start alert: fires exactly at task startTime ────────────────────
            AlarmScheduler.ACTION_TASK_START -> {
                val taskId   = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1)
                val taskName = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME) ?: "Routine"
                val durationMin = intent.getIntExtra(AlarmScheduler.EXTRA_DURATION_MIN, 0)

                Log.d("RoutineReceiver", "Start-alert → task='$taskName' id=$taskId duration=$durationMin")

                // Use a distinct notification ID (offset) so it doesn't replace the pre-alert
                NotificationHelper(context).showTaskInProgressNotification(
                    routineId = taskId,
                    title     = taskName,
                    durationMin = durationMin
                )
            }

            // ── Mark Done action from notification button ───────────────────────────
            "ACTION_MARK_DONE" -> {
                val routineId = intent.getIntExtra("ROUTINE_ID", -1)
                if (routineId != -1) {
                    Log.d("RoutineReceiver", "Marking routine $routineId as done")
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.markAsDone(routineId, true)
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.cancel(routineId + 20000)
                    notificationManager.cancel(routineId)
                }
            }
        }
    }
}
