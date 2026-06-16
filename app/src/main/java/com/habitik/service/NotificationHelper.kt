package com.habitik.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habitik.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "routine_notifications"
        const val CHANNEL_NAME = "Routine Reminders"
        const val TIMER_CHANNEL_ID = "timer_notifications_v2"
        const val TIMER_CHANNEL_NAME = "Timer Progress"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for routine tasks"
            }
            notificationManager.createNotificationChannel(channel)

            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Live countdown progress for tasks"
            }
            notificationManager.createNotificationChannel(timerChannel)
        }
    }

    fun getTimerNotification(taskId: Int, title: String, remainingSecs: Long): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId + 50000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(context, RoutineReceiver::class.java).apply {
            action = "ACTION_PAUSE_TIMER"
            putExtra("TASK_ID", taskId)
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 60000,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, RoutineReceiver::class.java).apply {
            action = "ACTION_DONE_TIMER"
            putExtra("TASK_ID", taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 70000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = remainingSecs / 60
        val seconds = remainingSecs % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Focusing: $title")
            .setContentText("Remaining: $timeStr")
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "Complete", donePendingIntent)
            .build()

        notification.flags = notification.flags or android.app.Notification.FLAG_NO_CLEAR or android.app.Notification.FLAG_ONGOING_EVENT
        return notification
    }

    fun showNotification(routineId: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            routineId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markDoneIntent = Intent(context, RoutineReceiver::class.java).apply {
            action = "ACTION_MARK_DONE"
            putExtra("ROUTINE_ID", routineId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            routineId + 1000,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system icon for now
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "Mark as Done", markDonePendingIntent)
            .build()

        notificationManager.notify(routineId, notification)
    }

    fun showTaskInProgressNotification(routineId: Int, title: String, durationMin: Int = 0) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            routineId + 30000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, RoutineReceiver::class.java).apply {
            action = "ACTION_MARK_DONE"
            putExtra("ROUTINE_ID", routineId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            routineId + 40000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("In Progress: $title")
            .setContentText("Task is running")
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_save, "Complete", completePendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && durationMin > 0) {
            val endTimeMillis = System.currentTimeMillis() + (durationMin * 60 * 1000L)
            builder.setUsesChronometer(true)
            builder.setChronometerCountDown(true)
            builder.setWhen(endTimeMillis)
        } else {
            builder.setUsesChronometer(true)
            builder.setWhen(System.currentTimeMillis())
        }

        val notification = builder.build()
        // Ensure it cannot be swiped away easily
        notification.flags = notification.flags or android.app.Notification.FLAG_NO_CLEAR or android.app.Notification.FLAG_ONGOING_EVENT

        notificationManager.notify(routineId + 20000, notification)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
