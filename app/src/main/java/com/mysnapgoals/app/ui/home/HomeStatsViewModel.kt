package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.usecase.ObserveGoalProgressEventsUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SumGoalProgressForGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeStatsState(
    val dayCompleted: Int = 0,
    val dayPending: Int = 0,
    val dayOverdue: Int = 0,
    val dayCompletedCount: Int = 0,
    val dayPendingCount: Int = 0,
    val dayOverdueCount: Int = 0,
    val weekCompleted: Int = 0,
    val weekPending: Int = 0,
    val weekOverdue: Int = 0,
    val weekCompletedCount: Int = 0,
    val weekPendingCount: Int = 0,
    val weekOverdueCount: Int = 0,
    val monthCompleted: Int = 0,
    val monthPending: Int = 0,
    val monthOverdue: Int = 0,
    val monthCompletedCount: Int = 0,
    val monthPendingCount: Int = 0,
    val monthOverdueCount: Int = 0,
    val yearCompleted: Int = 0,
    val yearPending: Int = 0,
    val yearOverdue: Int = 0,
    val yearCompletedCount: Int = 0,
    val yearPendingCount: Int = 0,
    val yearOverdueCount: Int = 0
)

@HiltViewModel
class HomeStatsViewModel @Inject constructor(
    private val observeTasks: ObserveTasksUseCase,
    private val sumGoalProgressForGoal: SumGoalProgressForGoalUseCase,
    private val observeGoalProgressEvents: ObserveGoalProgressEventsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeStatsState())
    val state: StateFlow<HomeStatsState> = _state
    private val dayTick = MutableStateFlow(todayEpochDay())

    init {
        viewModelScope.launch {
            combine(
                observeTasks(),
                observeGoalProgressEvents(),
                dayTick
            ) { tasks, _, _ -> tasks }
                .collect { entities ->
                    val today = todayEpochDay()

                    val (weekStart, weekEndExclusive) = weekRange(today)
                    val (monthStart, monthEndExclusive) = monthRange(today)
                    val (yearStart, yearEndExclusive) = yearRange(today)

                    val dayBreakdown = calculateBreakdown(entities, today to today, today)
                    val weekBreakdown = calculateBreakdown(entities, weekStart to (weekEndExclusive - 1), today)
                    val monthBreakdown = calculateBreakdown(entities, monthStart to (monthEndExclusive - 1), today)
                    val yearBreakdown = calculateBreakdown(entities, yearStart to (yearEndExclusive - 1), today)

                    _state.update {
                        it.copy(
                            dayCompleted = dayBreakdown.completedPct,
                            dayPending = dayBreakdown.pendingPct,
                            dayOverdue = dayBreakdown.overduePct,
                            dayCompletedCount = dayBreakdown.completed,
                            dayPendingCount = dayBreakdown.pending,
                            dayOverdueCount = dayBreakdown.overdue,
                            weekCompleted = weekBreakdown.completedPct,
                            weekPending = weekBreakdown.pendingPct,
                            weekOverdue = weekBreakdown.overduePct,
                            weekCompletedCount = weekBreakdown.completed,
                            weekPendingCount = weekBreakdown.pending,
                            weekOverdueCount = weekBreakdown.overdue,
                            monthCompleted = monthBreakdown.completedPct,
                            monthPending = monthBreakdown.pendingPct,
                            monthOverdue = monthBreakdown.overduePct,
                            monthCompletedCount = monthBreakdown.completed,
                            monthPendingCount = monthBreakdown.pending,
                            monthOverdueCount = monthBreakdown.overdue,
                            yearCompleted = yearBreakdown.completedPct,
                            yearPending = yearBreakdown.pendingPct,
                            yearOverdue = yearBreakdown.overduePct,
                            yearCompletedCount = yearBreakdown.completed,
                            yearPendingCount = yearBreakdown.pending,
                            yearOverdueCount = yearBreakdown.overdue
                        )
                    }
                }
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                val now = java.time.ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
                val delayMs = java.time.Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
                delay(delayMs)
                dayTick.value = todayEpochDay()
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

    private data class Breakdown(
        val completed: Int,
        val pending: Int,
        val overdue: Int,
        val total: Int,
        val completedPct: Int,
        val pendingPct: Int,
        val overduePct: Int
    )

    private suspend fun calculateBreakdown(
        tasks: List<Task>,
        range: Pair<Long, Long>,
        todayEpochDay: Long
    ): Breakdown {
        val startDay = range.first
        val endDay = range.second

        var completed = 0
        var pending = 0
        var overdue = 0

        val todos =
            tasks.filter { it.type == TaskType.TODO }
                .filter { (it.scheduledDay ?: todayEpochDay) in startDay..endDay }

        val goals =
            tasks.filter { it.type == TaskType.GOAL }
                .filter { (it.dueDay ?: todayEpochDay) in startDay..endDay }

        for (todo in todos) {
            val doneDay = todo.doneAt?.let { epochDayFromMillis(it) }
            val isCompleted = todo.isDone && doneDay != null && doneDay in startDay..endDay
            if (isCompleted) {
                completed++
            } else {
                val scheduled = todo.scheduledDay ?: todayEpochDay
                if (scheduled < todayEpochDay) overdue++ else pending++
            }
        }

        for (goal in goals) {
            val dueDay = goal.dueDay ?: todayEpochDay
            val progressInRange = sumGoalProgressForGoal(goal.id, startDay, endDay)
            val isCompleted = progressInRange > 0
            if (isCompleted) {
                completed++
            } else if (dueDay in startDay..endDay) {
                if (dueDay < todayEpochDay) overdue++ else pending++
            }
        }

        val total = completed + pending + overdue
        if (total == 0) {
            return Breakdown(0, 0, 0, 0, 0, 0, 0)
        }

        val (completedPct, pendingPct, overduePct) = computePercents(completed, pending, overdue)

        return Breakdown(
            completed = completed,
            pending = pending,
            overdue = overdue,
            total = total,
            completedPct = completedPct,
            pendingPct = pendingPct,
            overduePct = overduePct
        )
    }

    private fun computePercents(
        completed: Int,
        pending: Int,
        overdue: Int
    ): Triple<Int, Int, Int> {
        val total = completed + pending + overdue
        if (total == 0) {
            return Triple(0, 0, 0)
        }

        data class Bucket(
            val key: String,
            val count: Int,
            val raw: Double,
            var pct: Int,
            val frac: Double
        )

        val buckets = listOf(
            Bucket("completed", completed, completed * 100.0 / total, 0, 0.0),
            Bucket("pending", pending, pending * 100.0 / total, 0, 0.0),
            Bucket("overdue", overdue, overdue * 100.0 / total, 0, 0.0)
        ).map { bucket ->
            if (bucket.count == 0) {
                bucket.copy(pct = 0, frac = 0.0)
            } else {
                val base = kotlin.math.floor(bucket.raw).toInt().coerceIn(0, 100)
                bucket.copy(pct = base, frac = bucket.raw - base)
            }
        }.toMutableList()

        var remainder = 100 - buckets.sumOf { it.pct }
        if (remainder > 0) {
            val sorted = buckets.filter { it.count > 0 }
                .sortedByDescending { it.frac }
            var i = 0
            while (remainder > 0 && sorted.isNotEmpty()) {
                val idx = buckets.indexOf(sorted[i % sorted.size])
                buckets[idx] = buckets[idx].copy(pct = buckets[idx].pct + 1)
                remainder--
                i++
            }
        } else if (remainder < 0) {
            val sorted = buckets.filter { it.count > 0 }
                .sortedBy { it.frac }
            var i = 0
            while (remainder < 0 && sorted.isNotEmpty()) {
                val idx = buckets.indexOf(sorted[i % sorted.size])
                val current = buckets[idx].pct
                if (current > 0) {
                    buckets[idx] = buckets[idx].copy(pct = current - 1)
                    remainder++
                }
                i++
            }
        }

        val completedPct = buckets.first { it.key == "completed" }.pct
        val pendingPct = buckets.first { it.key == "pending" }.pct
        val overduePct = buckets.first { it.key == "overdue" }.pct
        return Triple(completedPct, pendingPct, overduePct)
    }

}
