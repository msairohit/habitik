package com.habitik.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.entity.TaskEntity
import com.habitik.data.repository.TaskRepository
import com.habitik.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val currentTask: TaskEntity? = null,
    val remainingTime: String = "00:00",
    val progress: Float = 1f,
    val nextTasks: List<TaskEntity> = emptyList(),
    val isRestDay: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        scheduleAllAlarms()
        startTicker()
    }

    /** Schedule pre-alert alarms for every active task whenever the task list changes. */
    private fun scheduleAllAlarms() {
        viewModelScope.launch {
            repository.getAllActiveTasks()
                .collect { tasks ->
                    alarmScheduler.scheduleAll(tasks)
                }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                updateCurrentTask()
                delay(1000)
            }
        }
    }

    private suspend fun updateCurrentTask() {
        val now = LocalTime.now()
        val tasks = repository.getAllActiveTasks().first()
        
        if (tasks.isEmpty()) {
            _uiState.update { it.copy(isRestDay = true) }
            return
        }

        // Simple sorting and filtering for the current time
        val sortedTasks = tasks.sortedBy { it.startTime }
        val current = sortedTasks.find { task ->
            val start = LocalTime.parse(task.startTime)
            val end = start.plusMinutes(task.durationMin.toLong())
            now.isAfter(start) && now.isBefore(end)
        }

        val next = sortedTasks.filter { task ->
            LocalTime.parse(task.startTime).isAfter(now)
        }.take(2)

        if (current != null) {
            val start = LocalTime.parse(current.startTime)
            val end = start.plusMinutes(current.durationMin.toLong())
            val totalSeconds = ChronoUnit.SECONDS.between(start, end)
            val remainingSeconds = ChronoUnit.SECONDS.between(now, end)
            
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            
            _uiState.update { 
                it.copy(
                    currentTask = current,
                    remainingTime = String.format("%02d:%02d", minutes, seconds),
                    progress = remainingSeconds.toFloat() / totalSeconds.toFloat(),
                    nextTasks = next,
                    isRestDay = false
                )
            }
        } else {
            _uiState.update { 
                it.copy(
                    currentTask = null,
                    nextTasks = next,
                    isRestDay = next.isEmpty() && tasks.isNotEmpty() // All tasks done
                )
            }
        }
    }
}
