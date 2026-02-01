package com.mysnapgoals.app.settings

data class PomodoroSettings(
    val autoStartBreaks: Boolean = true,
    val autoStartWork: Boolean = true,
    val pauseOnExit: Boolean = true,
    val alarmEnabled: Boolean = true,
    val alarmVolumePercent: Int = 80,
    val vibrationEnabled: Boolean = true,
    val keepNotification: Boolean = true,
    val shortBreakSeconds: Int = 5 * 60,
    val longBreakSeconds: Int = 15 * 60,
    val longBreakEvery: Int = 4,
    val profileAvatar: ProfileAvatar = ProfileAvatar.MALE,
    val appTheme: AppTheme = AppTheme.LIGHT
)
