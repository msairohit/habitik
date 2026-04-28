package com.habitik.ui.screens.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.entity.TaskEntity
import com.habitik.data.repository.TaskRepository
import com.habitik.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineBuilderViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = repository.getAllActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            // Schedule alarm for the newly added task (id is assigned by Room)
            val saved = repository.getTaskById(id.toInt())
            if (saved != null) alarmScheduler.scheduleTask(saved)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            // Reschedule with updated time/reminderMin
            alarmScheduler.scheduleTask(task)
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.softDeleteTask(id)
            alarmScheduler.cancelTask(id)
        }
    }

    suspend fun checkConflict(startTime: String, duration: Int): TaskEntity? {
        // Simple conflict detection logic
        // Convert times to minutes for easier comparison
        val newStart = timeToMinutes(startTime)
        val newEnd = newStart + duration

        val currentTasks = tasks.value
        return currentTasks.find { task ->
            val taskStart = timeToMinutes(task.startTime)
            val taskEnd = taskStart + task.durationMin
            
            // Check if intervals overlap
            (newStart < taskEnd && newEnd > taskStart)
        }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
}
