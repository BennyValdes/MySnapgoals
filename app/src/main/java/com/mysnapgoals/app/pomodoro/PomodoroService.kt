package com.mysnapgoals.app.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import com.mysnapgoals.app.MainActivity
import com.mysnapgoals.app.R
import com.mysnapgoals.app.settings.PomodoroSettings
import com.mysnapgoals.app.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PomodoroService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var settingsRepository: SettingsRepository

    private var settings: PomodoroSettings = PomodoroSettings()
    private var state: PomodoroState = PomodoroState()
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        createNotificationChannel()
        serviceScope.launch {
            settingsRepository.settingsFlow.collect { latest ->
                settings = latest
                maybeStopService()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        serviceScope.launch {
            when (action) {
                ACTION_START -> {
                    state = state.copy(isRunning = true)
                }
                ACTION_PAUSE -> {
                    state = state.copy(isRunning = false)
                }
                ACTION_TOGGLE -> {
                    state = state.copy(isRunning = !state.isRunning)
                }
                ACTION_RESET -> {
                    state = state.copy(
                        isRunning = false,
                        phase = PomodoroPhase.WORK,
                        completedPomodoros = 0,
                        remainingSeconds = state.totalSeconds
                    )
                }
                ACTION_SET_WORK_DURATION -> {
                    val totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, state.totalSeconds)
                    val safeTotal = totalSeconds.coerceAtLeast(60)
                    val newRemaining =
                        if (state.phase == PomodoroPhase.WORK && !state.isRunning) safeTotal
                        else state.remainingSeconds
                    state = state.copy(totalSeconds = safeTotal, remainingSeconds = newRemaining)
                }
            }

            startTickerIfNeeded()
            syncState()
            maybeStopService()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        tickerJob = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTickerIfNeeded() {
        if (tickerJob?.isActive == true) return
        tickerJob = serviceScope.launch {
            while (isActive) {
                if (state.isRunning) {
                    val nextRemaining = state.remainingSeconds - 1
                    if (nextRemaining <= 0) {
                        onPhaseComplete()
                    } else {
                        state = state.copy(remainingSeconds = nextRemaining)
                    }
                }
                syncState()
                maybeStopService()
                delay(1_000)
            }
        }
    }

    private fun onPhaseComplete() {
        if (settings.alarmEnabled) {
            playAlarm(settings.alarmVolumePercent)
        }
        if (settings.vibrationEnabled) {
            vibrate()
        }

        when (state.phase) {
            PomodoroPhase.WORK -> {
                val completed = state.completedPomodoros + 1
                val nextPhase =
                    if (completed % settings.longBreakEvery == 0) {
                        PomodoroPhase.LONG_BREAK
                    } else {
                        PomodoroPhase.SHORT_BREAK
                    }
                val nextRemaining =
                    if (nextPhase == PomodoroPhase.LONG_BREAK) {
                        settings.longBreakSeconds
                    } else {
                        settings.shortBreakSeconds
                    }
                state = state.copy(
                    phase = nextPhase,
                    completedPomodoros = completed,
                    remainingSeconds = nextRemaining,
                    isRunning = settings.autoStartBreaks
                )
            }
            PomodoroPhase.SHORT_BREAK,
            PomodoroPhase.LONG_BREAK -> {
                state = state.copy(
                    phase = PomodoroPhase.WORK,
                    remainingSeconds = state.totalSeconds,
                    isRunning = settings.autoStartWork
                )
            }
        }
    }

    private fun syncState() {
        PomodoroController.updateFromService(state)
        updateNotification()
    }

    private fun maybeStopService() {
        if (!settings.keepNotification && !state.isRunning) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun updateNotification() {
        if (!settings.keepNotification) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }
        val notification = buildNotification()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    private fun buildNotification(): Notification {
        val phaseText =
            when (state.phase) {
                PomodoroPhase.WORK -> getString(R.string.pomodoro_phase_work)
                PomodoroPhase.SHORT_BREAK -> getString(R.string.pomodoro_phase_short_break)
                PomodoroPhase.LONG_BREAK -> getString(R.string.pomodoro_phase_long_break)
            }
        val content = getString(
            R.string.pomodoro_notification_content,
            phaseText,
            formatSeconds(state.remainingSeconds)
        )

        val toggleAction = if (state.isRunning) {
            getString(R.string.pomodoro_action_pause)
        } else {
            getString(R.string.pomodoro_action_resume)
        }
        val toggleIntent = pendingServiceIntent(ACTION_TOGGLE)

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_add)
            .setContentTitle(getString(R.string.pomodoro_title))
            .setContentText(content)
            .setContentIntent(openIntent)
            .setOngoing(state.isRunning)
            .addAction(0, toggleAction, toggleIntent)
            .build()
    }

    private fun pendingServiceIntent(action: String): PendingIntent {
        val intent = Intent(this, PomodoroService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.pomodoro_title),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService<NotificationManager>()
        manager?.createNotificationChannel(channel)
    }

    private fun playAlarm(volumePercent: Int) {
        val volume = volumePercent.coerceIn(0, 100)
        val tone = ToneGenerator(AudioManager.STREAM_ALARM, volume)
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
        tone.release()
    }

    private fun vibrate() {
        val vibrator = getSystemService<Vibrator>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(400)
        }
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        val minutes = safeSeconds / 60
        val seconds = safeSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    companion object {
        const val ACTION_START = "com.mysnapgoals.app.pomodoro.START"
        const val ACTION_PAUSE = "com.mysnapgoals.app.pomodoro.PAUSE"
        const val ACTION_TOGGLE = "com.mysnapgoals.app.pomodoro.TOGGLE"
        const val ACTION_RESET = "com.mysnapgoals.app.pomodoro.RESET"
        const val ACTION_SET_WORK_DURATION = "com.mysnapgoals.app.pomodoro.SET_WORK_DURATION"

        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"

        private const val CHANNEL_ID = "pomodoro_channel"
        private const val NOTIFICATION_ID = 42
    }
}
