package com.mysnapgoals.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mysnapgoals.app.ui.home.HomeScreen
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsRepository = remember { SettingsRepository(context) }
            val settings by settingsRepository.settingsFlow.collectAsState(initial = PomodoroSettings())

            SnapGoalsTheme(themeOption = settings.appTheme) {
                HomeScreen()
            }
        }
    }
}
