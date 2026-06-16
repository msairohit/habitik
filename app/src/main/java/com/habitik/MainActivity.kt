package com.habitik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.habitik.data.SettingsManager
import com.habitik.ui.navigation.NavGraph
import com.habitik.ui.theme.AppTheme
import com.habitik.ui.theme.HabitikTheme
import com.habitik.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // Restore persisted theme before UI is drawn
        val savedThemeName = settingsManager.loadThemeName()
        val restoredTheme = AppTheme.values().firstOrNull { it.name == savedThemeName } ?: AppTheme.Purple
        ThemeManager.currentTheme.value = restoredTheme

        setContent {
            HabitikTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
