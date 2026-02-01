package com.mysnapgoals.app.pomodoro

enum class PomodoroPhase {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

data class PomodoroState(
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val phase: PomodoroPhase = PomodoroPhase.WORK,
    val completedPomodoros: Int = 0
)
