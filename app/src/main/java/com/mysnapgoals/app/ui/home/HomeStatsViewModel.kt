package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SumGoalProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeStatsState(
    val dayPercent: Int = 0,
    val weekPercent: Int = 0,
    val monthPercent: Int = 0,
    val yearPercent: Int = 0
)

@HiltViewModel
class HomeStatsViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val sumGoalProgress: SumGoalProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeStatsState())
    val state: StateFlow<HomeStatsState> = _state

    init {
        viewModelScope.launch {
            observeTasks().collect { entities ->
                val today = todayEpochDay()

                val (weekStart, weekEndExclusive) = weekRange(today)
                val (monthStart, monthEndExclusive) = monthRange(today)
                val (yearStart, yearEndExclusive) = yearRange(today)

                val todayItems = entities.filter { (it.scheduledDay ?: today) == today }
                val weekItems = entities.filter { (it.scheduledDay ?: today) in weekStart until weekEndExclusive }
                val monthItems = entities.filter { (it.scheduledDay ?: today) in monthStart until monthEndExclusive }
                val yearItems = entities.filter { (it.scheduledDay ?: today) in yearStart until yearEndExclusive }

                val dayPercent = calculatePercentForRange(todayItems, today, today)
                val weekPercent = calculatePercentForRange(weekItems, weekStart, weekEndExclusive - 1)
                val monthPercent = calculatePercentForRange(monthItems, monthStart, monthEndExclusive - 1)
                val yearPercent = calculatePercentForRange(yearItems, yearStart, yearEndExclusive - 1)

                _state.update {
                    it.copy(
                        dayPercent = dayPercent,
                        weekPercent = weekPercent,
                        monthPercent = monthPercent,
                        yearPercent = yearPercent
                    )
                }
            }
        }
    }

    private fun weekRange(todayEpochDay: Long): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val start = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val endExclusive = start.plusDays(7)
        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun monthRange(todayEpochDay: Long): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val start = today.withDayOfMonth(1)
        val endExclusive = start.plusMonths(1)
        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun yearRange(todayEpochDay: Long): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        val start = today.withDayOfYear(1)
        val endExclusive = start.plusYears(1)
        return start.toEpochDay() to endExclusive.toEpochDay()
    }

    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    private fun epochDayFromMillis(millis: Long): Long {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    }

    private suspend fun calculatePercentForRange(
        tasks: List<Task>,
        startDay: Long,
        endDay: Long
    ): Int {
        if (tasks.isEmpty()) return 0

        val today = todayEpochDay()
        val inRange =
            tasks.filter { task ->
                val day = task.scheduledDay ?: today
                day in startDay..endDay
            }

        val todos = inRange.filter { it.type == TaskType.TODO }
        val goals = inRange.filter { it.type == TaskType.GOAL }

        val totalTodoUnits = todos.size
        val doneTodoUnits =
            todos.count { todo ->
                val doneDay = todo.doneAt?.let { epochDayFromMillis(it) }
                todo.isDone && doneDay != null && doneDay in startDay..endDay
            }

        val totalGoalUnits = goals.sumOf { it.target ?: 0 }

        val goalProgressUnits =
            sumGoalProgress(
                goalIds = goals.map { it.id },
                startDay = startDay,
                endDay = endDay
            )

        val denom = totalTodoUnits + totalGoalUnits
        if (denom <= 0) return 0

        val numer = doneTodoUnits + goalProgressUnits
        return ((numer * 100) / denom).coerceIn(0, 100)
    }
}
