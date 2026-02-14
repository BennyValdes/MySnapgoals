package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import com.mysnapgoals.app.pomodoro.PomodoroController
import com.mysnapgoals.app.pomodoro.PomodoroPhase
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.SettingsRepository
import com.mysnapgoals.app.R
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val POMODORO_MIN_SECONDS = 5 * 60
private const val POMODORO_MAX_SECONDS = 90 * 60
private const val POMODORO_MIN_BREAK_SECONDS = 1 * 60
private const val POMODORO_MAX_SHORT_BREAK_SECONDS = 30 * 60
private const val POMODORO_MAX_LONG_BREAK_SECONDS = 45 * 60
private const val POMODORO_MIN_LONG_BREAK_EVERY = 2
private const val POMODORO_MAX_LONG_BREAK_EVERY = 8
private val POMODORO_WORK_OPTIONS_MINUTES = (5..90).toList()
private val POMODORO_SHORT_BREAK_OPTIONS_MINUTES = (1..30).toList()
private val POMODORO_LONG_BREAK_OPTIONS_MINUTES = (1..45).toList()
private val POMODORO_LONG_BREAK_EVERY_OPTIONS = (POMODORO_MIN_LONG_BREAK_EVERY..POMODORO_MAX_LONG_BREAK_EVERY).toList()

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
    val handleBack = {
        if (settings.pauseOnExit && pomodoroState.isRunning) {
            PomodoroController.pause(context)
        }
        onClose()
    }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val insets = WindowInsets.systemBars.asPaddingValues()
    val topPadding = insets.calculateTopPadding().coerceAtLeast(12.dp)
    val bottomPadding = insets.calculateBottomPadding().coerceAtLeast(16.dp)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = topPadding, bottom = bottomPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.pomodoro_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = handleBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back)
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (pomodoroState.phase) {
                            PomodoroPhase.WORK -> stringResource(R.string.pomodoro_phase_work)
                            PomodoroPhase.SHORT_BREAK -> stringResource(R.string.pomodoro_phase_short_break)
                            PomodoroPhase.LONG_BREAK -> stringResource(R.string.pomodoro_phase_long_break)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatSeconds(pomodoroState.remainingSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 2.dp
            )
            Spacer(Modifier.padding(vertical = 10.dp))
            Text(
                text = stringResource(R.string.pomodoro_breaks_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 50.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircularIndicatorSelector(
                        title = stringResource(R.string.pomodoro_phase_work),
                        value = stringResource(R.string.pomodoro_minutes, pomodoroState.totalSeconds / 60),
                        options = withCurrentValue(POMODORO_WORK_OPTIONS_MINUTES, pomodoroState.totalSeconds / 60),
                        selected = pomodoroState.totalSeconds / 60,
                        enabled = !pomodoroState.isRunning,
                        onSelect = { minutes ->
                            val seconds = (minutes * 60).coerceIn(POMODORO_MIN_SECONDS, POMODORO_MAX_SECONDS)
                            PomodoroController.setWorkDuration(context, seconds)
                        },
                        modifier = Modifier
                    )
                    CircularIndicatorSelector(
                        title = stringResource(R.string.pomodoro_phase_short_break),
                        value = stringResource(R.string.pomodoro_minutes, settings.shortBreakSeconds / 60),
                        options = withCurrentValue(
                            POMODORO_SHORT_BREAK_OPTIONS_MINUTES,
                            settings.shortBreakSeconds / 60
                        ),
                        selected = settings.shortBreakSeconds / 60,
                        enabled = !pomodoroState.isRunning,
                        onSelect = { minutes ->
                            val seconds = (minutes * 60).coerceIn(
                                POMODORO_MIN_BREAK_SECONDS,
                                POMODORO_MAX_SHORT_BREAK_SECONDS
                            )
                            scope.launch { settingsRepository.setShortBreakSeconds(seconds) }
                        },
                        modifier = Modifier
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircularIndicatorSelector(
                        title = stringResource(R.string.pomodoro_phase_long_break),
                        value = stringResource(R.string.pomodoro_minutes, settings.longBreakSeconds / 60),
                        options = withCurrentValue(
                            POMODORO_LONG_BREAK_OPTIONS_MINUTES,
                            settings.longBreakSeconds / 60
                        ),
                        selected = settings.longBreakSeconds / 60,
                        enabled = !pomodoroState.isRunning,
                        onSelect = { minutes ->
                            val seconds = (minutes * 60).coerceIn(
                                POMODORO_MIN_BREAK_SECONDS,
                                POMODORO_MAX_LONG_BREAK_SECONDS
                            )
                            scope.launch { settingsRepository.setLongBreakSeconds(seconds) }
                        },
                        modifier = Modifier
                    )
                    CircularIndicatorSelector(
                        title = stringResource(R.string.pomodoro_long_break_every, settings.longBreakEvery),
                        value = settings.longBreakEvery.toString(),
                        options = POMODORO_LONG_BREAK_EVERY_OPTIONS,
                        selected = settings.longBreakEvery,
                        enabled = !pomodoroState.isRunning,
                        onSelect = { value ->
                            scope.launch { settingsRepository.setLongBreakEvery(value) }
                        },
                        modifier = Modifier
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button3D(
                        text = if (pomodoroState.isRunning) {
                            stringResource(R.string.pomodoro_stop)
                        } else {
                            stringResource(R.string.pomodoro_start)
                        },
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
                        text = stringResource(R.string.pomodoro_reset),
                        onClick = {
                            PomodoroController.reset(context)
                        },
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        depth = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun CircularIndicatorSelector(
    title: String,
    value: String,
    options: List<Int>,
    selected: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeOptions = options.ifEmpty { listOf(selected) }
    val selectedIndex = safeOptions.indexOf(selected).let { if (it >= 0) it else 0 }
    val progress = if (safeOptions.size <= 1) 1f else selectedIndex.toFloat() / (safeOptions.size - 1).toFloat()
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val activeColor = MaterialTheme.colorScheme.primary
    val titleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.85f else 0.45f)
    val valueColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.45f)
    var selectorSize by remember { mutableIntStateOf(0) }
    var lastDispatchedValue by remember { mutableIntStateOf(selected) }

    LaunchedEffect(selected) {
        lastDispatchedValue = selected
    }

    fun updateFromTouch(offset: Offset) {
        if (!enabled || safeOptions.size <= 1 || selectorSize <= 0) return
        val center = selectorSize / 2f
        val dx = offset.x - center
        val dy = offset.y - center
        val rawAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat().let {
            if (it < 0f) it + 360f else it
        }
        val startAngle = 140f
        val totalSweep = 260f
        val unwrapped = if (rawAngle < startAngle) rawAngle + 360f else rawAngle
        val clamped = unwrapped.coerceIn(startAngle, startAngle + totalSweep)
        val p = (clamped - startAngle) / totalSweep
        val idx = (p * (safeOptions.size - 1)).roundToInt().coerceIn(0, safeOptions.lastIndex)
        val newValue = safeOptions[idx]
        if (newValue != lastDispatchedValue) {
            lastDispatchedValue = newValue
            onSelect(newValue)
        }
    }

    Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .onSizeChanged { selectorSize = it.width }
                .pointerInput(enabled, safeOptions) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        updateFromTouch(down.position)
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (change.pressed) {
                                updateFromTouch(change.position)
                                change.consume()
                            }
                        } while (change.pressed)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val radius = (size.minDimension / 2f) - strokeWidth
                val startAngle = 140f
                val totalSweep = 260f
                val angle = startAngle + totalSweep * progress

                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = activeColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val rad = Math.toRadians(angle.toDouble())
                val cx = center.x + radius * cos(rad).toFloat()
                val cy = center.y + radius * sin(rad).toFloat()
                drawCircle(
                    color = activeColor,
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun withCurrentValue(base: List<Int>, current: Int): List<Int> {
    return (base + current).distinct().sorted()
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
