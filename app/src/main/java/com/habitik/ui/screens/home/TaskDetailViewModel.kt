package com.habitik.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.entity.TaskEntity
import com.habitik.data.entity.TaskLogEntity
import com.habitik.data.repository.TaskLogRepository
import com.habitik.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val log: TaskLogEntity? = null,
    val status: String = "PENDING",
    val remainingTime: String = "00:00",
    val progress: Float = 0f,
    val progressText: String = "",
    val isPaused: Boolean = false,
    val isFocusing: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val logRepository: TaskLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private var currentTaskId: Int = 0

    fun loadTask(taskId: Int) {
        currentTaskId = taskId
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _uiState.update { it.copy(task = task) }
            updateState()
        }
        
        // Ticker for progress
        viewModelScope.launch {
            while (true) {
                updateState()
                delay(1000)
            }
        }
    }

    private suspend fun updateState() {
        val task = _uiState.value.task ?: return
        val date = java.time.LocalDate.now().toString()
        val allLogs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
        val latestLog = allLogs.maxByOrNull { it.id }
        val now = LocalTime.now()

        var remainingTime = "00:00"
        var progressVal = 0f
        var progressText = ""
        var isFocusing = false
        var status = latestLog?.status ?: "PENDING"

        when (task.measurementType) {
            "COUNT" -> {
                val doneLogs = allLogs.filter { it.status == "DONE" }.size
                progressVal = if (task.repeatCount > 0) doneLogs.toFloat() / task.repeatCount else 0f
                val percent = (progressVal * 100).toInt()
                progressText = "$doneLogs / ${task.repeatCount} times ($percent%)"
                isFocusing = false
                
                status = if (doneLogs >= task.repeatCount) "DONE"
                         else if (doneLogs > 0) "RESUMED"
                         else "PENDING"
            }
            "QUANTITY" -> {
                val totalDone = allLogs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                progressVal = if (task.goalValue > 0) totalDone / task.goalValue else 0f
                val percent = (progressVal * 100).toInt()
                
                val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                val goalStr = if (task.goalValue % 1 == 0f) task.goalValue.toInt().toString() else task.goalValue.toString()
                
                progressText = "$doneStr${task.goalUnit} / $goalStr${task.goalUnit} ($percent%)"
                isFocusing = false
                
                status = if (totalDone >= task.goalValue) "DONE"
                         else if (totalDone > 0) "RESUMED"
                         else "PENDING"
            }
            else -> { // TIME
                var totalSeconds = task.durationMin * 60L
                var remainingSeconds = 0L
                
                when (latestLog?.status) {
                    "RESUMED" -> {
                        val instant = java.time.Instant.ofEpochMilli(latestLog?.startedAt ?: System.currentTimeMillis())
                        val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                        
                        val totalSecs = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                        val endAt = startedAt.plusSeconds(totalSecs)
                        
                        remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
                        isFocusing = true
                        status = "RESUMED"
                    }
                    "PAUSED" -> {
                        remainingSeconds = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                        isFocusing = false
                        status = "PAUSED"
                    }
                    "DONE", "SKIPPED" -> {
                        remainingSeconds = 0
                        isFocusing = false
                        status = latestLog?.status ?: "PENDING"
                    }
                    else -> {
                        val start = LocalTime.parse(task.startTime)
                        val end = start.plusMinutes(task.durationMin.toLong())
                        if (now.isBefore(start)) {
                            remainingSeconds = task.durationMin * 60L
                            isFocusing = false
                            status = "PENDING"
                        } else if (now.isAfter(end)) {
                            remainingSeconds = 0
                            isFocusing = false
                            status = "DONE"
                        } else {
                            // In window but PENDING
                            remainingSeconds = task.durationMin * 60L
                            isFocusing = false
                            status = "PENDING"
                        }
                    }
                }
                
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                remainingTime = String.format("%02d:%02d", mins, secs)
                progressVal = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds else 1f
                progressText = "${(progressVal * 100).toInt()}%"
            }
        }

        _uiState.update {
            it.copy(
                log = latestLog,
                status = status,
                remainingTime = remainingTime,
                progress = progressVal.coerceIn(0f, 1f),
                progressText = progressText,
                isPaused = status == "PAUSED",
                isFocusing = isFocusing
            )
        }
    }

    fun onStartFocus() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            val log = logRepository.getLatestLogForTask(task.id, date)
            val remainingMins = log?.remainingDurationMin ?: task.durationMin
            val remainingSecs = log?.remainingDurationSec ?: (remainingMins * 60L)

            logRepository.insertLog(
                TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "RESUMED",
                    startedAt = System.currentTimeMillis(),
                    remainingDurationMin = (remainingSecs / 60).toInt(),
                    remainingDurationSec = remainingSecs
                )
            )
        }
    }

    fun onPauseFocus() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            val now = LocalTime.now()
            val date = java.time.LocalDate.now().toString()
            val log = logRepository.getLatestLogForTask(task.id, date)
            
            val remainingSeconds: Long
            if (log?.status == "RESUMED") {
                val instant = java.time.Instant.ofEpochMilli(log.startedAt ?: System.currentTimeMillis())
                val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                val totalSecs = log.remainingDurationSec ?: (log.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                val endAt = startedAt.plusSeconds(totalSecs)
                remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
            } else {
                val start = LocalTime.parse(task.startTime)
                val end = start.plusMinutes(task.durationMin.toLong())
                remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, end))
            }

            logRepository.insertLog(
                TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "PAUSED",
                    remainingDurationMin = (remainingSeconds / 60).toInt(),
                    remainingDurationSec = remainingSeconds
                )
            )
        }
    }

    fun onDoneTask() {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            val logs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
            val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1
            
            logRepository.insertLog(
                TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "DONE",
                    doneAt = System.currentTimeMillis(),
                    occurrence = if (task.measurementType == "COUNT") nextOccurrence else 1,
                    completedValue = if (task.measurementType == "COUNT") 1f else task.goalValue
                )
            )
        }
    }

    fun onIncrementTask(value: Float = 1f) {
        val task = _uiState.value.task ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            val logs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
            val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1

            logRepository.insertLog(
                TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "DONE",
                    doneAt = System.currentTimeMillis(),
                    occurrence = nextOccurrence,
                    completedValue = value
                )
            )
        }
    }
}
