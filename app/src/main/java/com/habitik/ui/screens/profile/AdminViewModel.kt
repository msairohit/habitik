package com.habitik.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.HabitikDatabase
import com.habitik.data.SettingsManager
import com.habitik.data.entity.TaskEntity
import com.habitik.data.repository.TaskRepository
import com.habitik.service.AlarmScheduler
import com.habitik.service.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val database: HabitikDatabase,
    val settingsManager: SettingsManager,
    private val taskRepository: TaskRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getAllActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTaskReminder(task: TaskEntity, newReminderMin: Int) {
        viewModelScope.launch {
            val updatedTask = task.copy(reminderMin = newReminderMin)
            taskRepository.updateTask(updatedTask)
            alarmScheduler.scheduleTask(updatedTask)
        }
    }

    fun toggleTaskActive(task: TaskEntity, active: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isActive = active)
            taskRepository.updateTask(updatedTask)
            if (active) {
                alarmScheduler.scheduleTask(updatedTask)
            } else {
                alarmScheduler.cancelTask(task.id)
            }
        }
    }

    fun triggerTestNotification(task: TaskEntity, isPreAlert: Boolean) {
        if (isPreAlert) {
            notificationHelper.showNotification(
                routineId = task.id,
                title = "⏰ Starting Soon: ${task.name} (Test)",
                message = "Your task starts in ${task.reminderMin} minutes. Get ready!"
            )
        } else {
            notificationHelper.showTaskInProgressNotification(
                routineId = task.id,
                title = "${task.name} (Test)",
                durationMin = task.durationMin
            )
        }
    }

    fun forceRescheduleAll() {
        viewModelScope.launch {
            val activeTasks = tasks.value
            alarmScheduler.scheduleAll(activeTasks)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            // Cancel all alarms
            tasks.value.forEach { alarmScheduler.cancelTask(it.id) }
            // Clear all database tables
            database.clearAllTables()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            val demoTasks = listOf(
                TaskEntity(
                    name = "Morning Meditation",
                    category = "HEALTH",
                    colorHex = "#4CAF50",
                    startTime = "07:00",
                    durationMin = 15,
                    isFlexible = false,
                    flexWindowEnd = null,
                    repeatDays = "DAILY",
                    reminderMin = 5
                ),
                TaskEntity(
                    name = "Coding Project",
                    category = "WORK",
                    colorHex = "#2196F3",
                    startTime = "09:00",
                    durationMin = 90,
                    isFlexible = true,
                    flexWindowEnd = "10:30",
                    repeatDays = "WEEKDAYS",
                    reminderMin = 10
                ),
                TaskEntity(
                    name = "Hydration Goal",
                    category = "HEALTH",
                    colorHex = "#00BCD4",
                    startTime = "ANYTIME",
                    durationMin = 5,
                    isFlexible = false,
                    flexWindowEnd = null,
                    repeatDays = "DAILY",
                    measurementType = "QUANTITY",
                    goalValue = 3000f,
                    goalUnit = "ml",
                    quantityIncrement = 250f,
                    reminderMin = 0
                ),
                TaskEntity(
                    name = "Read 10 Pages",
                    category = "PERSONAL",
                    colorHex = "#9C27B0",
                    startTime = "21:30",
                    durationMin = 30,
                    isFlexible = false,
                    flexWindowEnd = null,
                    repeatDays = "DAILY",
                    reminderMin = 5
                )
            )
            demoTasks.forEach { 
                val id = taskRepository.insertTask(it)
                alarmScheduler.scheduleTask(it.copy(id = id.toInt()))
            }
        }
    }
}
