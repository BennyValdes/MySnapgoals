package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.mysnapgoals.app.pomodoro.PomodoroController
import com.mysnapgoals.app.pomodoro.PomodoroPhase
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.SettingsRepository
import kotlinx.coroutines.launch

private const val POMODORO_MIN_SECONDS = 5 * 60
private const val POMODORO_MAX_SECONDS = 90 * 60
private const val POMODORO_MIN_BREAK_SECONDS = 1 * 60
private const val POMODORO_MAX_SHORT_BREAK_SECONDS = 30 * 60
private const val POMODORO_MAX_LONG_BREAK_SECONDS = 45 * 60
private const val POMODORO_MIN_LONG_BREAK_EVERY = 2
private const val POMODORO_MAX_LONG_BREAK_EVERY = 8
private val POMODORO_PRESET_MINUTES = listOf(15, 25, 50)

@Composable
fun PomodoroScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val settings by settingsRepository.settingsFlow.collectAsState(initial = PomodoroSettings())
    val pomodoroState by PomodoroController.state.collectAsState()
    val scope = rememberCoroutineScope()
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pomodoro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (pomodoroState.phase) {
                        PomodoroPhase.WORK -> "Trabajo"
                        PomodoroPhase.SHORT_BREAK -> "Descanso corto"
                        PomodoroPhase.LONG_BREAK -> "Descanso largo"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = formatSeconds(pomodoroState.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Tiempo de trabajo ${pomodoroState.totalSeconds / 60} min",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button3D(
                        text = "-5",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds = (pomodoroState.totalSeconds - 5 * 60)
                                .coerceAtLeast(POMODORO_MIN_SECONDS)
                            PomodoroController.setWorkDuration(context, nextSeconds)
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Button3D(
                        text = "-1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds = (pomodoroState.totalSeconds - 60)
                                .coerceAtLeast(POMODORO_MIN_SECONDS)
                            PomodoroController.setWorkDuration(context, nextSeconds)
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Text(
                        text = "${pomodoroState.totalSeconds / 60} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Button3D(
                        text = "+1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds = (pomodoroState.totalSeconds + 60)
                                .coerceAtMost(POMODORO_MAX_SECONDS)
                            PomodoroController.setWorkDuration(context, nextSeconds)
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Button3D(
                        text = "+5",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds = (pomodoroState.totalSeconds + 5 * 60)
                                .coerceAtMost(POMODORO_MAX_SECONDS)
                            PomodoroController.setWorkDuration(context, nextSeconds)
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    POMODORO_PRESET_MINUTES.forEach { minutes ->
                        Button3D(
                            text = "${minutes}m",
                            onClick = {
                                if (pomodoroState.isRunning) return@Button3D
                                val presetSeconds = (minutes * 60).coerceIn(POMODORO_MIN_SECONDS, POMODORO_MAX_SECONDS)
                                PomodoroController.setWorkDuration(context, presetSeconds)
                            },
                            modifier = Modifier.weight(1f),
                            height = 40.dp,
                            depth = 3.dp
                        )
                    }
                }
                Divider(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
                Text(
                    text = "Descansos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Tiempo descanso corto ${settings.shortBreakSeconds / 60} min",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button3D(
                        text = "-1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds =
                                (settings.shortBreakSeconds - 60).coerceAtLeast(POMODORO_MIN_BREAK_SECONDS)
                            scope.launch { settingsRepository.setShortBreakSeconds(nextSeconds) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Button3D(
                        text = "+1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds =
                                (settings.shortBreakSeconds + 60).coerceAtMost(POMODORO_MAX_SHORT_BREAK_SECONDS)
                            scope.launch { settingsRepository.setShortBreakSeconds(nextSeconds) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                }
                Text(
                    text = "Tiempo descanso largo ${settings.longBreakSeconds / 60} min",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button3D(
                        text = "-1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds =
                                (settings.longBreakSeconds - 60).coerceAtLeast(POMODORO_MIN_BREAK_SECONDS)
                            scope.launch { settingsRepository.setLongBreakSeconds(nextSeconds) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Button3D(
                        text = "+1",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextSeconds =
                                (settings.longBreakSeconds + 60).coerceAtMost(POMODORO_MAX_LONG_BREAK_SECONDS)
                            scope.launch { settingsRepository.setLongBreakSeconds(nextSeconds) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                }
                Text(
                    text = "Descanso cada ${settings.longBreakEvery} ciclos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button3D(
                        text = "-",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextValue =
                                (settings.longBreakEvery - 1).coerceAtLeast(POMODORO_MIN_LONG_BREAK_EVERY)
                            scope.launch { settingsRepository.setLongBreakEvery(nextValue) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                    Button3D(
                        text = "+",
                        onClick = {
                            if (pomodoroState.isRunning) return@Button3D
                            val nextValue =
                                (settings.longBreakEvery + 1).coerceAtMost(POMODORO_MAX_LONG_BREAK_EVERY)
                            scope.launch { settingsRepository.setLongBreakEvery(nextValue) }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        depth = 3.dp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button3D(
                        text = if (pomodoroState.isRunning) "Detener" else "Iniciar",
                        onClick = {
                            if (settings.keepNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@Button3D
                                }
                            }
                            PomodoroController.toggle(context, settings.keepNotification)
                        },
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        depth = 4.dp
                    )
                    Button3D(
                        text = "Reiniciar",
                        onClick = {
                            PomodoroController.reset(context)
                        },
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        depth = 4.dp
                    )
                }

                Button3D(
                    text = "Volver",
                    onClick = {
                        if (settings.pauseOnExit && pomodoroState.isRunning) {
                            PomodoroController.pause(context)
                        }
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp,
                    depth = 4.dp
                )
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun PomodoroScreenPreview() {
    SnapGoalsTheme {
        PomodoroScreen(onClose = {})
    }
}
