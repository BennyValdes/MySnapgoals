package com.mysnapgoals.app.ui.home.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CalendarBannerState(
    val timeText: String = "--:--",
    val dayOfWeekText: String = "",
    val dateText: String = ""
)

@HiltViewModel
class CalendarBannerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CalendarBannerState())
    val state: StateFlow<CalendarBannerState> = _state

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val locale: Locale = Locale.Builder()
        .setLanguage("es")
        .setRegion("US")
        .build()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", locale)

    init {
        viewModelScope.launch {
            tick()
            while (isActive) {
                val now = ZonedDateTime.now(zoneId)
                delay(scheduleNextMinuteDelayMs(now))
                tick()
            }
        }
    }

    private fun tick() {
        val now = LocalDateTime.now(zoneId)

        val time = now.format(timeFormatter)
        val day = now.format(dayFormatter).replaceFirstChar { it.uppercase(locale) }
        val date = now.format(dateFormatter)

        _state.update {
            it.copy(
                timeText = time,
                dayOfWeekText = day,
                dateText = date
            )
        }
    }

    private fun scheduleNextMinuteDelayMs(now: ZonedDateTime): Long {
        val nextMinute = now.plusMinutes(1).withSecond(0).withNano(0)
        return Duration.between(now, nextMinute).toMillis().coerceAtLeast(250L)
    }
}
