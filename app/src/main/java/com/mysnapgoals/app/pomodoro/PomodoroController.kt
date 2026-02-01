package com.mysnapgoals.app.pomodoro

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object PomodoroController {
    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    fun start(context: Context) {
        sendAction(context, PomodoroService.ACTION_START, useForeground = true)
    }

    fun pause(context: Context) {
        sendAction(context, PomodoroService.ACTION_PAUSE, useForeground = false)
    }

    fun toggle(context: Context, useForeground: Boolean) {
        sendAction(context, PomodoroService.ACTION_TOGGLE, useForeground = useForeground)
    }

    fun reset(context: Context) {
        sendAction(context, PomodoroService.ACTION_RESET, useForeground = false)
    }

    fun setWorkDuration(context: Context, totalSeconds: Int) {
        _state.update { current ->
            val newTotal = totalSeconds.coerceAtLeast(60)
            val newRemaining =
                if (current.phase == PomodoroPhase.WORK && !current.isRunning) newTotal
                else current.remainingSeconds
            current.copy(totalSeconds = newTotal, remainingSeconds = newRemaining)
        }
        sendAction(
            context,
            PomodoroService.ACTION_SET_WORK_DURATION,
            useForeground = false,
            PomodoroService.EXTRA_TOTAL_SECONDS to totalSeconds
        )
    }

    internal fun updateFromService(state: PomodoroState) {
        _state.value = state
    }

    private fun sendAction(
        context: Context,
        action: String,
        useForeground: Boolean,
        vararg extras: Pair<String, Int>
    ) {
        val intent = Intent(context, PomodoroService::class.java).apply {
            this.action = action
            extras.forEach { (key, value) -> putExtra(key, value) }
        }
        if (useForeground) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
