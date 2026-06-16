package com.habitik.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.entity.TaskEntity
import com.habitik.data.repository.TaskRepository
import com.habitik.service.AlarmScheduler
import com.habitik.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TaskStatusInfo(
    val task: TaskEntity,
    val status: String,          // PENDING | DONE | SKIPPED | PAUSED | RESUMED
    val progressText: String,    // "20/30 mins", "2/8 times", etc.
    val progress: Float,         // 0.0 to 1.0
    val remainingTime: String
)

data class DayScheduleInfo(
    val date: LocalDate,
    val dayName: String,         // "MON", "TUE", etc.
    val tasks: List<TaskStatusInfo>
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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val logRepository: com.habitik.data.repository.TaskLogRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: com.habitik.service.NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Expose streaks for active tasks
    val taskStreaks: StateFlow<Map<Int, Int>> = repository.getAllActiveTasks()
        .flatMapLatest { tasks ->
            logRepository.getAllLogs().map { logs ->
                tasks.associate { task ->
                    val taskLogs = logs.filter { it.taskId == task.id }
                    task.id to calculateStreak(task, taskLogs)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Expose completion rate per day for the last 7 days (date to percentage float)
    val weeklyCompletionRate: StateFlow<Map<String, Float>> = repository.getAllActiveTasks()
        .flatMapLatest { tasks ->
            val startRange = java.time.LocalDate.now().minusDays(7)
            logRepository.getLogsInRange(startRange.toString(), java.time.LocalDate.now().toString()).map { logs ->
                val logsMap = logs.groupBy { it.logDate }
                (0..6).associate { i ->
                    val date = java.time.LocalDate.now().minusDays(i.toLong())
                    val dateStr = date.toString()
                    val dayLogs = logsMap[dateStr] ?: emptyList()
                    val activeTasksForDay = tasks.filter { isTaskScheduledForDayName(it, date) }
                    val doneTasksCount = activeTasksForDay.count { isTaskFullyCompletedForDay(it, dayLogs) }
                    val percent = if (activeTasksForDay.isNotEmpty()) doneTasksCount.toFloat() / activeTasksForDay.size else 0f
                    dateStr to percent.coerceIn(0f, 1f)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Completed task count by category
    val categoryCompletion: StateFlow<Map<String, Int>> = repository.getAllActiveTasks()
        .flatMapLatest { tasks ->
            logRepository.getLogsForDate(java.time.LocalDate.now().toString())
                .map { logs ->
                    tasks.filter { isTaskFullyCompletedForDay(it, logs) }
                        .groupBy { it.category }
                        .mapValues { it.value.size }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Weekly schedule tracking for the Timeline view
    val weeklySchedule: StateFlow<List<DayScheduleInfo>> = repository.getAllActiveTasks()
        .flatMapLatest { tasks ->
            val startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            val endOfWeek = LocalDate.now().with(java.time.DayOfWeek.SUNDAY)
            logRepository.getLogsInRange(startOfWeek.toString(), endOfWeek.toString()).map { logs ->
                val logsMap = logs.groupBy { it.logDate }
                (0..6).map { i ->
                    val date = startOfWeek.plusDays(i.toLong())
                    val dayLogs = logsMap[date.toString()] ?: emptyList()
                    compileDaySchedule(tasks, date, dayLogs)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected date tracking for the History Calendar view
    val selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    val selectedDateSchedule: StateFlow<DayScheduleInfo?> = selectedDate
        .flatMapLatest { date ->
            repository.getAllActiveTasks().flatMapLatest { tasks ->
                logRepository.getLogsForDate(date.toString()).map { logs ->
                    compileDaySchedule(tasks, date, logs)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's schedule tracking for the Dashboard / Focus view
    val todaySchedule: StateFlow<DayScheduleInfo?> = repository.getAllActiveTasks()
        .flatMapLatest { tasks ->
            logRepository.getLogsForDate(LocalDate.now().toString()).map { logs ->
                compileDaySchedule(tasks, LocalDate.now(), logs)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun compileDaySchedule(
        tasks: List<TaskEntity>,
        date: LocalDate,
        dayLogs: List<com.habitik.data.entity.TaskLogEntity>
    ): DayScheduleInfo {
        val dayOfWeekName = date.dayOfWeek.name.take(3)
        val dayTasks = tasks.filter { task ->
            val taskCreationDate = java.time.Instant.ofEpochMilli(task.createdAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            if (date.isBefore(taskCreationDate)) {
                return@filter false
            }
            when {
                task.repeatDays == "ONCE" -> {
                    taskCreationDate == date
                }
                task.repeatDays == "DAILY" -> true
                task.repeatDays == "WEEKDAYS" -> !setOf("SAT", "SUN").contains(dayOfWeekName)
                task.repeatDays == "WEEKENDS" -> setOf("SAT", "SUN").contains(dayOfWeekName)
                task.repeatDays.startsWith("[") -> task.repeatDays.contains(dayOfWeekName)
                else -> false
            }
        }.sortedBy { it.startTime }.map { task ->
            val taskLogs = dayLogs.filter { it.taskId == task.id }
            val latestLog = taskLogs.maxByOrNull { it.id }
            
            var status = latestLog?.status ?: "PENDING"
            var progressText = ""
            var progressVal = 0f

            when (task.measurementType) {
                "TIME" -> {
                    if (status == "DONE") {
                        progressText = "${task.durationMin}/${task.durationMin} mins (100%)"
                        progressVal = 1f
                    } else if (status == "SKIPPED") {
                        progressText = "Skipped"
                        progressVal = 0f
                    } else if (status == "RESUMED" || status == "PAUSED") {
                        val remainingSecs = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                        val completedSecs = Math.max(0L, (task.durationMin * 60L) - remainingSecs)
                        val completedMins = completedSecs / 60
                        progressText = "$completedMins/${task.durationMin} mins"
                        progressVal = completedSecs.toFloat() / (task.durationMin * 60f)
                    } else {
                        progressText = "0/${task.durationMin} mins"
                        progressVal = 0f
                    }
                }
                "COUNT" -> {
                    val doneLogs = taskLogs.filter { it.status == "DONE" }.size
                    progressVal = if (task.repeatCount > 0) doneLogs.toFloat() / task.repeatCount else 0f
                    progressText = "$doneLogs/${task.repeatCount} times"
                    if (doneLogs >= task.repeatCount) status = "DONE"
                    else if (doneLogs > 0) status = "IN PROGRESS"
                }
                "QUANTITY" -> {
                    val totalDone = taskLogs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                    progressVal = if (task.goalValue > 0) totalDone / task.goalValue else 0f
                    val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                    val goalStr = if (task.goalValue % 1 == 0f) task.goalValue.toInt().toString() else task.goalValue.toString()
                    progressText = "$doneStr/$goalStr ${task.goalUnit}"
                    if (totalDone >= task.goalValue) status = "DONE"
                    else if (totalDone > 0) status = "IN PROGRESS"
                }
            }

            val remainingTime = when (task.measurementType) {
                "TIME" -> {
                    if (status == "DONE") "DONE"
                    else if (status == "SKIPPED") "SKIPPED"
                    else {
                        val remainingSecs = latestLog?.remainingDurationSec ?: (latestLog?.remainingDurationMin?.toLong()?.times(60L) ?: (task.durationMin * 60L))
                        val mins = remainingSecs / 60
                        val secs = remainingSecs % 60
                        String.format("%02d:%02d", mins, secs)
                    }
                }
                else -> progressText.split(" ").first()
            }

            TaskStatusInfo(task, status, progressText, progressVal.coerceIn(0f, 1f), remainingTime)
        }
        
        return DayScheduleInfo(date, dayOfWeekName, dayTasks)
    }

    init {
        // Cleanup notifications on start in case of stale ghosts
        notificationHelper.cancelAllNotifications()
        scheduleAllAlarms()
        startTicker()
        
        // Auto-refresh Home Screen Glance Widget when database logs update
        viewModelScope.launch {
            logRepository.getLogsForDate(LocalDate.now().toString()).collect {
                try {
                    com.habitik.widget.HabitWidgetHelper.updateAll(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Keep UI state synchronized with today's compiled schedule changes reactively
        viewModelScope.launch {
            todaySchedule.collect { schedule ->
                if (schedule != null) {
                    updateStateWithSchedule(schedule)
                }
            }
        }
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
                tickActiveTimer()
                delay(1000)
            }
        }
    }

    private fun updateStateWithSchedule(schedule: DayScheduleInfo) {
        val tasksWithStatus = schedule.tasks
        val sortedTasks = schedule.tasks.map { it.task }
        
        val now = LocalTime.now()
        val date = LocalDate.now().toString()
        
        // Find active task (resumed first, then paused, then scheduled now)
        var activeItem = tasksWithStatus.find { it.status == "RESUMED" }
        if (activeItem == null) {
            activeItem = tasksWithStatus.find { it.status == "PAUSED" }
        }
        if (activeItem == null) {
            activeItem = tasksWithStatus.find { item ->
                if (item.task.startTime == "ANYTIME") false
                else {
                    val start = LocalTime.parse(item.task.startTime)
                    val end = start.plusMinutes(item.task.durationMin.toLong())
                    now.isAfter(start) && now.isBefore(end) && item.status != "DONE" && item.status != "SKIPPED"
                }
            }
        }
        
        val activeTask = activeItem?.task
        val isDoneMode = activeTask?.measurementType != "TIME"
        val progressLabel = when (activeTask?.measurementType) {
            "COUNT" -> "DONE"
            "QUANTITY" -> "DONE"
            else -> "REMAINING"
        }
        
        val focusableTasks = tasksWithStatus.filter { item ->
            val isScheduledNow = run {
                if (item.task.startTime == "ANYTIME") false
                else {
                    val start = LocalTime.parse(item.task.startTime)
                    val end = start.plusMinutes(item.task.durationMin.toLong())
                    now.isAfter(start) && now.isBefore(end)
                }
            }
            val hasStarted = item.status == "RESUMED" || item.status == "PAUSED"
            val isCompletedOrSkipped = item.status == "DONE" || item.status == "SKIPPED"
            
            (isScheduledNow || hasStarted) && !isCompletedOrSkipped
        }.sortedByDescending { 
            when (it.status) {
                "RESUMED" -> 3
                "PAUSED" -> 2
                "PENDING" -> 1
                else -> 0
            }
        }

        val next = sortedTasks.filter { task ->
            if (task.startTime == "ANYTIME") false
            else LocalTime.parse(task.startTime).isAfter(now)
        }.take(2)

        _uiState.update { 
            it.copy(
                currentTask = activeTask,
                progressLabel = progressLabel,
                isDoneMode = isDoneMode,
                nextTasks = next,
                tasksWithStatus = tasksWithStatus,
                focusableTasks = focusableTasks,
                allTasks = sortedTasks,
                isRestDay = next.isEmpty() && sortedTasks.isNotEmpty() && activeTask == null && focusableTasks.isEmpty(),
                isPaused = activeItem?.status == "PAUSED"
            )
        }
        tickActiveTimer()
    }

    private fun tickActiveTimer() {
        viewModelScope.launch {
            val uiStateVal = _uiState.value
            val activeTask = uiStateVal.currentTask
            val activeItem = uiStateVal.tasksWithStatus.find { it.task.id == activeTask?.id }
            
            if (activeTask == null || activeItem == null) {
                _uiState.update { it.copy(remainingTime = "00:00", progress = 1f) }
                return@launch
            }
            
            val now = LocalTime.now()
            val date = LocalDate.now().toString()
            var displayTime = "00:00"
            var displayProgress = 1f
            
            when (activeTask.measurementType) {
                "TIME" -> {
                    val status = activeItem.status
                    if (status == "SKIPPED") {
                        displayTime = "SKIPPED"
                        displayProgress = 0f
                    } else if (status == "DONE") {
                        displayTime = "DONE"
                        displayProgress = 1f
                    } else {
                        val log = logRepository.getLatestLogForTask(activeTask.id, date)
                        val totalSeconds = log?.remainingDurationSec ?: (log?.remainingDurationMin?.toLong()?.times(60L) ?: (activeTask.durationMin * 60L))
                        val remainingSeconds: Long
                        
                        if (status == "RESUMED" && log?.startedAt != null) {
                            val instant = java.time.Instant.ofEpochMilli(log.startedAt)
                            val startedAt = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime()
                            val endAt = startedAt.plusSeconds(totalSeconds)
                            remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, endAt))
                        } else {
                            remainingSeconds = totalSeconds
                        }
                        
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        displayTime = String.format("%02d:%02d", mins, secs)
                        displayProgress = if (activeTask.durationMin > 0) remainingSeconds.toFloat() / (activeTask.durationMin * 60f) else 1f
                    }
                }
                "COUNT" -> {
                    val doneLogs = activeItem.progressText.split("/").firstOrNull()?.trim()?.toIntOrNull() ?: 0
                    displayTime = "$doneLogs/${activeTask.repeatCount}"
                    displayProgress = if (activeTask.repeatCount > 0) doneLogs.toFloat() / activeTask.repeatCount else 0f
                }
                "QUANTITY" -> {
                    val totalDone = activeItem.progressText.split("/").firstOrNull()?.trim()?.toFloatOrNull() ?: 0f
                    val doneStr = if (totalDone % 1 == 0f) totalDone.toInt().toString() else totalDone.toString()
                    displayTime = "$doneStr${activeTask.goalUnit}"
                    displayProgress = if (activeTask.goalValue > 0) totalDone / activeTask.goalValue else 0f
                }
            }
            
            _uiState.update { 
                it.copy(
                    remainingTime = displayTime,
                    progress = displayProgress
                )
            }
        }
    }

    fun onSkipTask(targetTask: TaskEntity? = null) {
        val task = targetTask ?: uiState.value.currentTask ?: return
        if (task.measurementType == "TIME") {
            stopTimerService()
        }
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
        val currentStatus = uiState.value.tasksWithStatus.find { it.task.id == task.id }?.status
        if (currentStatus == "DONE") return
        if (task.measurementType == "TIME") {
            stopTimerService()
        }
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
        val currentStatus = uiState.value.tasksWithStatus.find { it.task.id == task.id }?.status
        if (currentStatus == "DONE") return
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
        if (task.measurementType == "TIME") {
            stopTimerService()
        }
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
                if (task.startTime == "ANYTIME") {
                    remainingSeconds = task.durationMin * 60L
                } else {
                    val start = LocalTime.parse(task.startTime)
                    val end = start.plusMinutes(task.durationMin.toLong())
                    remainingSeconds = Math.max(0, ChronoUnit.SECONDS.between(now, end))
                }
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

            if (task.measurementType == "TIME") {
                startTimerService(task.id, task.name, remainingSecs)
            }

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

    fun onSnoozeTask() {
        // Implementation for +10 min
    }

    private fun calculateStreak(task: TaskEntity, logs: List<com.habitik.data.entity.TaskLogEntity>): Int {
        var streak = 0
        var checkDate = java.time.LocalDate.now()
        val todayLogs = logs.filter { it.logDate == checkDate.toString() }
        val hasDoneToday = isTaskFullyCompletedForDay(task, todayLogs)
        if (!hasDoneToday) {
            checkDate = checkDate.minusDays(1)
        }
        while (true) {
            val dateLogs = logs.filter { it.logDate == checkDate.toString() }
            val hasDoneOnDate = isTaskFullyCompletedForDay(task, dateLogs)
            if (hasDoneOnDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun isTaskFullyCompletedForDay(task: TaskEntity, dayLogs: List<com.habitik.data.entity.TaskLogEntity>): Boolean {
        val taskLogs = dayLogs.filter { it.taskId == task.id }
        if (taskLogs.isEmpty()) return false
        
        return when (task.measurementType) {
            "COUNT" -> {
                val doneCount = taskLogs.count { it.status == "DONE" }
                doneCount >= task.repeatCount
            }
            "QUANTITY" -> {
                val totalDone = taskLogs.filter { it.status == "DONE" }.sumOf { (it.completedValue ?: 0f).toDouble() }.toFloat()
                totalDone >= task.goalValue
            }
            else -> { // TIME
                taskLogs.any { it.status == "DONE" }
            }
        }
    }

    private fun isTaskScheduledForDayName(task: TaskEntity, date: java.time.LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek.name.take(3)
        return when {
            task.repeatDays == "ONCE" -> date.toString() == java.time.LocalDate.now().toString()
            task.repeatDays == "DAILY" -> true
            task.repeatDays == "WEEKDAYS" -> !setOf("SAT", "SUN").contains(dayOfWeek)
            task.repeatDays == "WEEKENDS" -> setOf("SAT", "SUN").contains(dayOfWeek)
            task.repeatDays.startsWith("[") -> task.repeatDays.contains(dayOfWeek)
            else -> false
        }
    }
}
