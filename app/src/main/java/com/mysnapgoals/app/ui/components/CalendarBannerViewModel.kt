package com.mysnapgoals.app.ui.components

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalendarBannerState(
    val timeText: String = "--:--",
    val dayOfWeekText: String = "",
    val dateText: String = ""
) : MavericksState

class CalendarBannerViewModel(
    initialState: CalendarBannerState
) : MavericksViewModel<CalendarBannerState>(initialState) {

    companion object : MavericksViewModelFactory<CalendarBannerViewModel, CalendarBannerState> {
        override fun create(viewModelContext: ViewModelContext, state: CalendarBannerState) =
            CalendarBannerViewModel(state)
    }

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val locale: Locale = Locale.Builder()
        .setLanguage("es")
        .setRegion("US")
        .build()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)// lunes, martes...
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", locale)

    init {
        viewModelScope.launch {
            // Primer render inmediato
            tick()

            // Tick alineado al cambio de minuto (sin drift)
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

        setState {
            copy(
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