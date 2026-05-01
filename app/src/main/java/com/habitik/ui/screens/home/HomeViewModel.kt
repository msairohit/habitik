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

data class TaskStatusInfo(
    val task: TaskEntity,
    val status: String,          // PENDING | DONE | SKIPPED | PAUSED | RESUMED
    val progressText: String,    // "20/30 mins", "2/8 times", etc.
    val progress: Float          // 0.0 to 1.0
)

data class HomeUiState(
    val currentTask: TaskEntity? = null,
    val remainingTime: String = "00:00",
    val progressLabel: String = "REMAINING",
    val isDoneMode: Boolean = false,
    val progress: Float = 1f,
    val nextTasks: List<TaskEntity> = emptyList(),
    val tasksWithStatus: List<TaskStatusInfo> = emptyList(),
    val focusableTasks: List<TaskStatusInfo> = emptyList(), // Tasks to show in the pager
    val allTasks: List<TaskEntity> = emptyList(),
    val isRestDay: Boolean = false,
    val isPaused: Boolean = false,
    val selectedTask: TaskEntity? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val logRepository: com.habitik.data.repository.TaskLogRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: com.habitik.service.NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Cleanup notifications on start in case of stale ghosts
        notificationHelper.cancelAllNotifications()
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
        val date = java.time.LocalDate.now().toString()
        val tasks = repository.getAllActiveTasks().first()
        
        val dayOfWeek = java.time.LocalDate.now().dayOfWeek.name.take(3)
        val filteredTasks = tasks.filter { task ->
            when {
                task.repeatDays == "ONCE" -> {
                    // Only show if it was created today OR has logs for today OR has NO logs at all (newly created)
                    // But if it has logs for a PREVIOUS day, it's done.
                    val logs = logRepository.getLogsForTask(task.id).first()
                    val hasLogsBeforeToday = logs.any { it.logDate < date }
                    !hasLogsBeforeToday
                }
                task.repeatDays == "DAILY" -> true
                task.repeatDays == "WEEKDAYS" -> !setOf("SAT", "SUN").contains(dayOfWeek)
                task.repeatDays == "WEEKENDS" -> setOf("SAT", "SUN").contains(dayOfWeek)
                task.repeatDays.startsWith("[") -> task.repeatDays.contains(dayOfWeek)
                else -> false
            }
        }
        
        if (filteredTasks.isEmpty()) {
            _uiState.update { it.copy(isRestDay = true, allTasks = emptyList(), currentTask = null) }
            notificationHelper.cancelAllNotifications()
            return
        }

        val sortedTasks = filteredTasks.sortedBy { it.startTime }
        
        // 1. Check if any task is explicitly "RESUMED" right now
        // A resumed task stays active until its remaining duration is up.
        var activeTask: TaskEntity? = null
        var activeLog: com.habitik.data.entity.TaskLogEntity? = null
        
        for (task in sortedTasks) {
            val log = logRepository.getLatestLogForTask(task.id, date)
            if (log?.status == "RESUMED" && log.startedAt != null) {
                val instant = java.time.Instant.ofEpochMilli(log.startedAt)
                val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                
                val remainingSeconds = log.remainingDurationSec ?: (log.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                val endAt = startedAt.plusSeconds(remainingSeconds)
                
                if (now.isAfter(startedAt) && now.isBefore(endAt)) {
                    activeTask = task
                    activeLog = log
                    break
                }
            }
        }

        // 2. If no resumed task, check for scheduled tasks
        if (activeTask == null) {
            activeTask = sortedTasks.find { task ->
                val start = LocalTime.parse(task.startTime)
                val end = start.plusMinutes(task.durationMin.toLong())
                val isScheduledNow = now.isAfter(start) && now.isBefore(end)
                
                if (isScheduledNow) {
                    // If it's scheduled now, only make it 'active' if it's NOT already DONE or SKIPPED
                    val log = logRepository.getLatestLogForTask(task.id, date)
                    log?.status != "DONE" && log?.status != "SKIPPED"
                } else false
            }
            
            // If still no active task, check if there is a DONE or SKIPPED task in the current window 
            // that we should show as 'completed' in the focus card
            if (activeTask == null) {
                activeTask = sortedTasks.find { task ->
                    val start = LocalTime.parse(task.startTime)
                    val end = start.plusMinutes(task.durationMin.toLong())
                    now.isAfter(start) && now.isBefore(end)
                }
            }

            if (activeTask != null) {
                activeLog = logRepository.getLatestLogForTask(activeTask.id, date)
            }
        }

        // 3. If no active or resumed task, check for any PAUSED tasks for today
        if (activeTask == null) {
            var pausedTask: TaskEntity? = null
            var pausedLog: com.habitik.data.entity.TaskLogEntity? = null
            
            for (task in sortedTasks) {
                val log = logRepository.getLatestLogForTask(task.id, date)
                if (log?.status == "PAUSED") {
                    pausedTask = task
                    pausedLog = log
                    break
                }
            }

            if (pausedTask != null) {
                activeTask = pausedTask
                activeLog = pausedLog
            }
        }

        val next = sortedTasks.filter { task ->
            LocalTime.parse(task.startTime).isAfter(now)
        }.take(2)

        var minutes = 0L
        var seconds = 0L
        var totalSeconds = 0L
        var remainingSeconds = 0L

        if (activeTask != null) {
            val status = activeLog?.status ?: "PENDING"
            
            if (status == "RESUMED" && activeLog?.startedAt != null) {
                val instant = java.time.Instant.ofEpochMilli(activeLog.startedAt)
                val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                
                totalSeconds = activeLog.remainingDurationSec ?: (activeLog.remainingDurationMin?.toLong()?.times(60L) ?: (activeTask.durationMin * 60L))
                val endAt = startedAt.plusSeconds(totalSeconds)
                
                remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
            } else if (status == "PAUSED") {
                totalSeconds = activeLog?.remainingDurationSec ?: (activeLog?.remainingDurationMin?.toLong()?.times(60L) ?: (activeTask.durationMin * 60L))
                remainingSeconds = totalSeconds
            } else if (status == "SKIPPED" || status == "DONE") {
                totalSeconds = activeTask.durationMin * 60L
                remainingSeconds = 0
            } else if (status == "PENDING") {
                totalSeconds = activeTask.durationMin * 60L
                remainingSeconds = totalSeconds
            } else {
                val start = LocalTime.parse(activeTask.startTime)
                val end = start.plusMinutes(activeTask.durationMin.toLong())
                totalSeconds = activeTask.durationMin * 60L
                remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, end))
            }
            
            minutes = remainingSeconds / 60
            seconds = remainingSeconds % 60
        }

        val tasksWithStatus = sortedTasks.map { task ->
            val logs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
            val latestLog = logs.maxByOrNull { it.id }
            
            var status = latestLog?.status ?: "PENDING"
            var progressText = ""
            var progressVal = 0f

            when (task.measurementType) {
                "TIME" -> {
                    val start = LocalTime.parse(task.startTime)
                    val end = start.plusMinutes(task.durationMin.toLong())
                    
                    if (status == "DONE") {
                        progressText = "${task.durationMin}/${task.durationMin} mins done (100%)"
                        progressVal = 1f
                    } else if (status == "SKIPPED") {
                        progressText = "Skipped"
                        progressVal = 0f
                    } else {
                        // Calculate actual progress
                        var remainingSecs = 0L
                        if (status == "RESUMED" && latestLog?.startedAt != null) {
                            val instant = java.time.Instant.ofEpochMilli(latestLog.startedAt!!)
                            val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                            val totalSecs = latestLog.remainingDurationSec ?: (latestLog.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                            val endAt = startedAt.plusSeconds(totalSecs)
                            remainingSecs = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
                        } else if (status == "PAUSED") {
                            remainingSecs = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                        } else if (status == "PENDING") {
                            remainingSecs = task.durationMin * 60L
                        } else if (now.isAfter(end)) {
                            remainingSecs = 0
                        } else {
                            remainingSecs = task.durationMin * 60L
                        }

                        val completedSecs = (task.durationMin * 60L) - remainingSecs
                        val completedMins = completedSecs / 60
                        val progressPercent = if (task.durationMin > 0) (completedSecs.toFloat() / (task.durationMin * 60f) * 100).toInt() else 0
                        progressText = "$completedMins/${task.durationMin} mins done ($progressPercent%)"
                        progressVal = if (task.durationMin > 0) completedSecs.toFloat() / (task.durationMin * 60f) else 0f
                    }
                }
                "COUNT" -> {
                    val doneLogs = logs.filter { it.status == "DONE" }.size
                    progressVal = if (task.repeatCount > 0) doneLogs.toFloat() / task.repeatCount else 0f
                    val percent = (progressVal * 100).toInt()
                    progressText = "$doneLogs/${task.repeatCount} times done ($percent%)"
                    if (doneLogs >= task.repeatCount) status = "DONE"
                    else if (doneLogs > 0) status = "RESUMED"
                }
                "QUANTITY" -> {
                    val totalDone = logs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                    progressVal = if (task.goalValue > 0) totalDone / task.goalValue else 0f
                    val percent = (progressVal * 100).toInt()
                    
                    val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                    val goalStr = if (task.goalValue % 1 == 0f) task.goalValue.toInt().toString() else task.goalValue.toString()
                    
                    progressText = "$doneStr${task.goalUnit} / $goalStr${task.goalUnit} done ($percent%)"
                    if (totalDone >= task.goalValue) status = "DONE"
                    else if (totalDone > 0) status = "RESUMED"
                }
            }

            TaskStatusInfo(task, status, progressText, progressVal.coerceIn(0f, 1f))
        }

        val isDoneMode = activeTask?.measurementType != "TIME"
        val progressLabel = when (activeTask?.measurementType) {
            "COUNT" -> "DONE"
            "QUANTITY" -> "DONE"
            else -> "REMAINING"
        }
        
        var displayTime = "00:00"
        var displayProgress = 1f
        
        if (activeTask != null) {
            val status = activeLog?.status ?: "PENDING"
            if (status == "SKIPPED") {
                displayTime = "SKIPPED"
                displayProgress = 0f
            } else if (status == "DONE" && activeTask.measurementType == "TIME") {
                displayTime = "DONE"
                displayProgress = 1f
            } else if (activeTask.measurementType == "TIME") {
                displayTime = String.format("%02d:%02d", minutes, seconds)
                displayProgress = if (totalSeconds > 0) remainingSeconds.toFloat() / (activeTask.durationMin * 60f) else 1f
            } else if (activeTask.measurementType == "COUNT") {
                val doneLogs = logRepository.getLogsForTask(activeTask.id).first().filter { it.logDate == date && it.status == "DONE" }.size
                displayTime = "$doneLogs/${activeTask.repeatCount}"
                displayProgress = if (activeTask.repeatCount > 0) doneLogs.toFloat() / activeTask.repeatCount else 0f
            } else if (activeTask.measurementType == "QUANTITY") {
                val totalDone = logRepository.getLogsForTask(activeTask.id).first().filter { it.logDate == date && it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                displayTime = "$doneStr${activeTask.goalUnit}"
                displayProgress = if (activeTask.goalValue > 0) totalDone / activeTask.goalValue else 0f
            }
        }

        val focusableTasks = tasksWithStatus.filter { item ->
            val isScheduledNow = run {
                val start = LocalTime.parse(item.task.startTime)
                val end = start.plusMinutes(item.task.durationMin.toLong())
                now.isAfter(start) && now.isBefore(end)
            }
            val hasStarted = item.status == "RESUMED" || item.status == "PAUSED" || item.status == "DONE" || item.status == "SKIPPED"
            
            isScheduledNow || hasStarted
        }.sortedByDescending { 
            // Put RESUMED tasks first, then PAUSED, then scheduled, then finished
            when (it.status) {
                "RESUMED" -> 3
                "PAUSED" -> 2
                "PENDING" -> 1
                else -> 0
            }
        }

        _uiState.update { 
            it.copy(
                currentTask = activeTask,
                remainingTime = displayTime,
                progressLabel = progressLabel,
                isDoneMode = isDoneMode,
                progress = displayProgress,
                nextTasks = next,
                tasksWithStatus = tasksWithStatus,
                focusableTasks = focusableTasks,
                allTasks = sortedTasks,
                isRestDay = next.isEmpty() && tasks.isNotEmpty() && activeTask == null && focusableTasks.isEmpty(),
                isPaused = activeLog?.status == "PAUSED"
            )
        }
    }

    fun onSkipTask(targetTask: TaskEntity? = null) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            logRepository.insertLog(
                com.habitik.data.entity.TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "SKIPPED"
                )
            )
        }
    }

    fun onDoneTask(targetTask: TaskEntity? = null) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            val logs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
            val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1
            
            logRepository.insertLog(
                com.habitik.data.entity.TaskLogEntity(
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

    fun onIncrementTask(targetTask: TaskEntity? = null, value: Float = 1f) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            val logs = logRepository.getLogsForTask(task.id).first().filter { it.logDate == date }
            val nextOccurrence = (logs.maxByOrNull { it.occurrence }?.occurrence ?: 0) + 1
            
            logRepository.insertLog(
                com.habitik.data.entity.TaskLogEntity(
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

    fun onPauseTask(targetTask: TaskEntity? = null) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        
        viewModelScope.launch {
            val now = LocalTime.now()
            val date = java.time.LocalDate.now().toString()
            val log = logRepository.getLatestLogForTask(task.id, date)
            
            if (log?.status == "PAUSED") return@launch
            
            val remainingSeconds: Long
            if (log?.status == "RESUMED" && log.startedAt != null) {
                val instant = java.time.Instant.ofEpochMilli(log.startedAt)
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
                com.habitik.data.entity.TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "PAUSED",
                    remainingDurationMin = (remainingSeconds / 60).toInt(),
                    remainingDurationSec = remainingSeconds
                )
            )
        }
    }

    fun onResumeTask(targetTask: TaskEntity? = null) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        
        viewModelScope.launch {
            val date = java.time.LocalDate.now().toString()
            
            // Auto-pause any other active TIME tasks
            val otherTasks = repository.getAllActiveTasks().first()
            for (other in otherTasks) {
                if (other.id != task.id && other.measurementType == "TIME") {
                    val otherLog = logRepository.getLatestLogForTask(other.id, date)
                    if (otherLog?.status == "RESUMED") {
                        onPauseTask(other)
                    }
                }
            }

            val log = logRepository.getLatestLogForTask(task.id, date)
            if (log?.status == "RESUMED") return@launch

            val remainingMins = log?.remainingDurationMin ?: task.durationMin
            val remainingSecs = log?.remainingDurationSec ?: (remainingMins * 60L)

            logRepository.insertLog(
                com.habitik.data.entity.TaskLogEntity(
                    taskId = task.id,
                    logDate = date,
                    status = "RESUMED",
                    startedAt = System.currentTimeMillis(),
                    remainingDurationMin = remainingMins,
                    remainingDurationSec = remainingSecs
                )
            )
        }
    }

    fun onSnoozeTask() {
        // Implementation for +10 min
    }
}
