package com.mysnapgoals.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_DATASTORE_NAME = "snapgoals_settings"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME
)

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.dataStore

    val settingsFlow: Flow<PomodoroSettings> =
        dataStore.data.map { prefs ->
            PomodoroSettings(
                autoStartBreaks = prefs[Keys.AUTO_START_BREAKS] ?: true,
                autoStartWork = prefs[Keys.AUTO_START_WORK] ?: true,
                pauseOnExit = prefs[Keys.PAUSE_ON_EXIT] ?: true,
                alarmEnabled = prefs[Keys.ALARM_ENABLED] ?: true,
                alarmVolumePercent = prefs[Keys.ALARM_VOLUME] ?: 80,
                vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
                keepNotification = prefs[Keys.KEEP_NOTIFICATION] ?: true,
                shortBreakSeconds = prefs[Keys.SHORT_BREAK_SECONDS] ?: 5 * 60,
                longBreakSeconds = prefs[Keys.LONG_BREAK_SECONDS] ?: 15 * 60,
                longBreakEvery = prefs[Keys.LONG_BREAK_EVERY] ?: 4,
                profileAvatar = parseProfileAvatar(prefs[Keys.PROFILE_AVATAR]),
                appTheme = parseAppTheme(prefs[Keys.APP_THEME])
            )
        }

    suspend fun setAutoStartBreaks(value: Boolean) {
        dataStore.edit { it[Keys.AUTO_START_BREAKS] = value }
    }

    suspend fun setAutoStartWork(value: Boolean) {
        dataStore.edit { it[Keys.AUTO_START_WORK] = value }
    }

    suspend fun setPauseOnExit(value: Boolean) {
        dataStore.edit { it[Keys.PAUSE_ON_EXIT] = value }
    }

    suspend fun setAlarmEnabled(value: Boolean) {
        dataStore.edit { it[Keys.ALARM_ENABLED] = value }
    }

    suspend fun setAlarmVolumePercent(value: Int) {
        dataStore.edit { it[Keys.ALARM_VOLUME] = value.coerceIn(0, 100) }
    }

    suspend fun setVibrationEnabled(value: Boolean) {
        dataStore.edit { it[Keys.VIBRATION_ENABLED] = value }
    }

    suspend fun setKeepNotification(value: Boolean) {
        dataStore.edit { it[Keys.KEEP_NOTIFICATION] = value }
    }

    suspend fun setShortBreakSeconds(value: Int) {
        dataStore.edit { it[Keys.SHORT_BREAK_SECONDS] = value.coerceIn(60, 30 * 60) }
    }

    suspend fun setLongBreakSeconds(value: Int) {
        dataStore.edit { it[Keys.LONG_BREAK_SECONDS] = value.coerceIn(60, 45 * 60) }
    }

    suspend fun setLongBreakEvery(value: Int) {
        dataStore.edit { it[Keys.LONG_BREAK_EVERY] = value.coerceIn(2, 8) }
    }

    suspend fun setProfileAvatar(value: ProfileAvatar) {
        dataStore.edit { it[Keys.PROFILE_AVATAR] = value.name }
    }

    suspend fun setAppTheme(value: AppTheme) {
        dataStore.edit { it[Keys.APP_THEME] = value.name }
    }

    private object Keys {
        val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
        val AUTO_START_WORK = booleanPreferencesKey("auto_start_work")
        val PAUSE_ON_EXIT = booleanPreferencesKey("pause_on_exit")
        val ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
        val ALARM_VOLUME = intPreferencesKey("alarm_volume_percent")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEEP_NOTIFICATION = booleanPreferencesKey("keep_notification")
        val SHORT_BREAK_SECONDS = intPreferencesKey("short_break_seconds")
        val LONG_BREAK_SECONDS = intPreferencesKey("long_break_seconds")
        val LONG_BREAK_EVERY = intPreferencesKey("long_break_every")
        val PROFILE_AVATAR = stringPreferencesKey("profile_avatar")
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    private fun parseProfileAvatar(value: String?): ProfileAvatar {
        val raw = value ?: return ProfileAvatar.MALE
        return runCatching { ProfileAvatar.valueOf(raw) }.getOrDefault(ProfileAvatar.MALE)
    }

    private fun parseAppTheme(value: String?): AppTheme {
        val raw = value ?: return AppTheme.LIGHT
        if (raw == "DEFAULT") return AppTheme.LIGHT
        return runCatching { AppTheme.valueOf(raw) }.getOrDefault(AppTheme.LIGHT)
    }
}
