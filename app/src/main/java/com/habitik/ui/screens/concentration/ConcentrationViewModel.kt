package com.habitik.ui.screens.concentration

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.entity.TaskEntity
import com.habitik.data.entity.TaskLogEntity
import com.habitik.data.repository.TaskLogRepository
import com.habitik.data.repository.TaskRepository
import com.habitik.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class ConcentrationUiState(
    val task: TaskEntity? = null,
    val log: TaskLogEntity? = null,
    val status: String = "PENDING",
    val remainingTime: String = "00:00",
    val progress: Float = 0f,
    val isPaused: Boolean = false,
    val isFocusing: Boolean = false
)

@HiltViewModel
class ConcentrationViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val logRepository: TaskLogRepository,
    val settingsManager: com.habitik.data.SettingsManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun setConcentrationModeActive(active: Boolean) {
        settingsManager.isConcentrationModeActive.value = active
    }

    private val _uiState = MutableStateFlow(ConcentrationUiState())
    val uiState: StateFlow<ConcentrationUiState> = _uiState.asStateFlow()

    private var currentTaskId: Int = 0

    fun loadTask(taskId: Int) {
        currentTaskId = taskId
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _uiState.update { it.copy(task = task) }
            updateState()
        }
        
        // Timer update ticker
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
        var isFocusing = false
        var status = latestLog?.status ?: "PENDING"

        val totalSeconds = task.durationMin * 60L
        var remainingSeconds = 0L
        
        when (latestLog?.status) {
            "RESUMED" -> {
                val instant = java.time.Instant.ofEpochMilli(latestLog.startedAt ?: System.currentTimeMillis())
                val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                
                val totalSecs = latestLog.remainingDurationSec ?: (latestLog.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                val endAt = startedAt.plusSeconds(totalSecs)
                
                remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
                isFocusing = true
                status = "RESUMED"
            }
            "PAUSED" -> {
                remainingSeconds = latestLog.remainingDurationSec ?: (latestLog.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                isFocusing = false
                status = "PAUSED"
            }
            "DONE", "SKIPPED" -> {
                remainingSeconds = 0
                isFocusing = false
                status = latestLog.status
            }
            else -> {
                val start = if (task.startTime == "ANYTIME") LocalTime.MIDNIGHT else LocalTime.parse(task.startTime)
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

        _uiState.update {
            it.copy(
                log = latestLog,
                status = status,
                remainingTime = remainingTime,
                progress = progressVal.coerceIn(0f, 1f),
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

            startTimerService(task.id, task.name, remainingSecs)

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
        stopTimerService()
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
                val start = if (task.startTime == "ANYTIME") LocalTime.MIDNIGHT else LocalTime.parse(task.startTime)
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
        if (_uiState.value.status == "DONE") return
        stopTimerService()
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            logRepository.insertLog(
                TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "DONE",
                    doneAt = System.currentTimeMillis(),
                    occurrence = 1,
                    completedValue = task.goalValue
                )
            )
        }
    }

    private fun startTimerService(taskId: Int, taskName: String, remainingSecs: Long) {
        val serviceIntent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_TIMER
            putExtra(TimerForegroundService.EXTRA_TASK_ID, taskId)
            putExtra(TimerForegroundService.EXTRA_TASK_NAME, taskName)
            putExtra(TimerForegroundService.EXTRA_REMAINING_SECS, remainingSecs)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopTimerService() {
        val serviceIntent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP_TIMER
        }
        context.stopService(serviceIntent)
    }
}
