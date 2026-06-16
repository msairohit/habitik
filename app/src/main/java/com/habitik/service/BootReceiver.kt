package com.habitik.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.habitik.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling all alarms.")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeTasks = taskRepository.getAllActiveTasks().first()
                    alarmScheduler.scheduleAll(activeTasks)
                    Log.d("BootReceiver", "Successfully rescheduled ${activeTasks.size} tasks.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms: ${e.message}", e)
                }
            }
        }
    }
}
