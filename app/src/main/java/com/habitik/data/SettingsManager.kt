package com.habitik.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("habitik_settings", Context.MODE_PRIVATE)
    
    val notificationsEnabled = mutableStateOf(prefs.getBoolean("notifications_enabled", true))
    val snoozeDurationMin = mutableStateOf(prefs.getInt("snooze_duration_min", 10))
    val isVibrationEnabled = mutableStateOf(prefs.getBoolean("vibration_enabled", true))
    val isConcentrationModeActive = mutableStateOf(false)
    
    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }
    
    fun setSnoozeDurationMin(min: Int) {
        snoozeDurationMin.value = min
        prefs.edit().putInt("snooze_duration_min", min).apply()
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        isVibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    // ── Theme persistence ──────────────────────────────────────────────────────
    fun saveTheme(themeName: String) {
        prefs.edit().putString("app_theme", themeName).apply()
    }

    fun loadThemeName(): String {
        return prefs.getString("app_theme", "Purple") ?: "Purple"
    }
}
