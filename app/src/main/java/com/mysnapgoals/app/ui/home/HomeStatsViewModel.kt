package com.mysnapgoals.app.ui.home

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.mysnapgoals.app.SnapGoalsGraph
import com.mysnapgoals.app.data.local.entity.TaskEntity
import kotlinx.coroutines.launch

data class HomeStatsState(
    val dayPercent: Int = 0,
    val weekPercent: Int = 0,
    val monthPercent: Int = 0,
    val yearPercent: Int = 0
) : MavericksState

class HomeStatsViewModel(
    initialState: HomeStatsState
) : MavericksViewModel<HomeStatsState>(initialState) {

    companion object : MavericksViewModelFactory<HomeStatsViewModel, HomeStatsState> {
        override fun create(viewModelContext: ViewModelContext, state: HomeStatsState) =
            HomeStatsViewModel(state)
    }

    private val repo = SnapGoalsGraph.tasksRepository

    init {
        viewModelScope.launch {
            repo.tasksState.collect { entities ->
                val today = todayEpochDay()

                val (weekStart, weekEndExclusive) = weekRange(today)
                val (monthStart, monthEndExclusive) = monthRange(today)
                val (yearStart, yearEndExclusive) = yearRange(today)

                val todayItems = entities.filter { it.scheduledDay == today }
                val weekItems = entities.filter { it.scheduledDay in weekStart until weekEndExclusive }
                val monthItems = entities.filter { it.scheduledDay in monthStart until monthEndExclusive }
                val yearItems = entities.filter { it.scheduledDay in yearStart until yearEndExclusive }

                val dayPercent = computeDayPercent(todayItems, todayEpochDay = today)
                val weekPercent = computeRangePercent(weekItems)
                val monthPercent = computeRangePercent(monthItems)
                val yearPercent = computeRangePercent(yearItems)

                setState {
                    copy(
                        dayPercent = dayPercent,
                        weekPercent = weekPercent,
                        monthPercent = monthPercent,
                        yearPercent = yearPercent
                    )
                }
            }
        }
    }


    private fun computeDayPercent(items: List<TaskEntity>, todayEpochDay: Long): Int {
        if (items.isEmpty()) return 0

        val avg =
            items
                .asSequence()
                .map { e ->
                    when (e.type) {
                        TaskEntity.TYPE_TODO -> if (e.isDone) 1.0 else 0.0

                        TaskEntity.TYPE_GOAL -> {
                            // Para "Hoy": cuenta 100% si avanzó hoy (updatedAt del día de hoy)
                            val updatedDay = epochDayFromMillis(e.updatedAt)
                            if (updatedDay == todayEpochDay) 1.0 else 0.0
                        }

                        else -> 0.0
                    }
                }
                .average()

        return (avg * 100).toInt().coerceIn(0, 100)
    }

    private fun computeRangePercent(items: List<TaskEntity>): Int {
        if (items.isEmpty()) return 0

        val avg =
            items
                .asSequence()
                .map { e ->
                    when (e.type) {
                        TaskEntity.TYPE_TODO -> if (e.isDone) 1.0 else 0.0

                        TaskEntity.TYPE_GOAL -> {
                            val target = (e.target ?: 0).coerceAtLeast(0)
                            val current = (e.current ?: 0).coerceAtLeast(0)

                            if (target <= 0) {
                                if (e.isDone) 1.0 else 0.0
                            } else {
                                (current.toDouble() / target.toDouble()).coerceIn(0.0, 1.0)
                            }
                        }

                        else -> 0.0
                    }
                }
                .average()

        return (avg * 100).toInt().coerceIn(0, 100)
    }

    private fun weekRange(todayEpochDay: Long): Pair<Long, Long> {
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.ofEpochDay(todayEpochDay)

        // ISO week: Lunes como inicio
        val start = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val endExclusive = start.plusDays(7)

        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun monthRange(todayEpochDay: Long): Pair<Long, Long> {
        val today = java.time.LocalDate.ofEpochDay(todayEpochDay)
        val start = today.withDayOfMonth(1)
        val endExclusive = start.plusMonths(1)
        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun yearRange(todayEpochDay: Long): Pair<Long, Long> {
        val today = java.time.LocalDate.ofEpochDay(todayEpochDay)
        val start = today.withDayOfYear(1)
        val endExclusive = start.plusYears(1)
        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun todayEpochDay(): Long = java.time.LocalDate.now().toEpochDay()

    private fun epochDayFromMillis(millis: Long): Long {
        return java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    }

    private fun rangeForWeek(today: Long): LongRange {
        // hoy es epochDay; semana = últimos 6 días + hoy (7 días)
        val start = today - 6
        return start..today
    }

    private fun rangeForMonth(today: Long): LongRange {
        val date = java.time.LocalDate.now()
        val start = date.withDayOfMonth(1).toEpochDay()
        return start..today
    }

    private fun rangeForYear(today: Long): LongRange {
        val date = java.time.LocalDate.now()
        val start = date.withDayOfYear(1).toEpochDay()
        return start..today
    }

    private suspend fun calculatePercentForRange(
        tasks: List<TaskEntity>,
        startDay: Long,
        endDay: Long
    ): Int {
        val inRange = tasks.filter { it.scheduledDay != null && it.scheduledDay in startDay..endDay }

        val todos = inRange.filter { it.type == TaskEntity.TYPE_TODO }
        val goals = inRange.filter { it.type == TaskEntity.TYPE_GOAL }

        val totalTodoUnits = todos.size
        val doneTodoUnits = todos.count { it.isDone && it.doneAt != null } // o doneAt in range si quieres ultra preciso

        val totalGoalUnits = goals.sumOf { it.target ?: 0 }

        // progreso en rango se basa en events (no en current snapshot)
        val goalProgressUnits = repo.sumGoalsDeltaBetweenDays(startDay, endDay) // tienes que exponerlo

        val denom = totalTodoUnits + totalGoalUnits
        if (denom <= 0) return 0

        val numer = doneTodoUnits + goalProgressUnits
        return ((numer * 100) / denom).coerceIn(0, 100)
    }
}