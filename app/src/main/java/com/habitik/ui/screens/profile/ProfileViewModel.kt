package com.habitik.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitik.data.SettingsManager
import com.habitik.data.repository.StreakRepository
import com.habitik.data.repository.TaskLogRepository
import com.habitik.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val taskRepository: TaskRepository,
    private val taskLogRepository: TaskLogRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    val savedThemeName: String get() = settingsManager.loadThemeName()

    fun saveTheme(themeName: String) {
        settingsManager.saveTheme(themeName)
    }

    val totalActiveTasks: StateFlow<Int> = taskRepository.getAllActiveTasks()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCompletedTasks: StateFlow<Int> = taskLogRepository.getAllLogs()
        .map { logs -> logs.filter { it.status == "DONE" }.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestStreak: StateFlow<Int> = streakRepository.getAllStreaks()
        .map { streaks -> streaks.maxOfOrNull { it.currentStreak } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
