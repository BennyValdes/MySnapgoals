package com.mysnapgoals.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.usecase.ObserveGoalProgressEventsUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.temporal.ChronoUnit
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
            ) { tasks, events, _ -> tasks to events }
                .collect { (tasks, events) ->
                    val today = todayEpochDay()

                    val (weekStart, weekEndExclusive) = weekRange(today)
                    val (monthStart, monthEndExclusive) = monthRange(today)
                    val (yearStart, yearEndExclusive) = yearRange(today)

                    val dayBreakdown = calculateBreakdown(tasks, events, today to today, today)
                    val weekBreakdown = calculateBreakdown(tasks, events, weekStart to (weekEndExclusive - 1), today)
                    val monthBreakdown = calculateBreakdown(tasks, events, monthStart to (monthEndExclusive - 1), today)
                    val yearBreakdown = calculateBreakdown(tasks, events, yearStart to (yearEndExclusive - 1), today)

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

    private fun calculateBreakdown(
        tasks: List<Task>,
        events: List<GoalProgressEvent>,
        range: Pair<Long, Long>,
        todayEpochDay: Long
    ): Breakdown {
        val startDay = range.first
        val endDay = range.second

        var completed = 0
        var pending = 0
        var overdue = 0

        val goalDeltaByGoalAndDay = events
            .groupBy { it.goalId }
            .mapValues { (_, list) ->
                list.groupBy { it.epochDay }
                    .mapValues { (_, dayEvents) -> dayEvents.sumOf { it.delta } }
            }

        val todos =
            tasks.filter { it.type == TaskType.TODO }
                .filter { todo ->
                    val dueDay = todo.scheduledDay ?: todayEpochDay
                    rangesOverlap(startDay, endDay, dueDay, dueDay)
                }

        val goals = tasks.filter { it.type == TaskType.GOAL }

        for (todo in todos) {
            val isCompleted = todo.isDone
            if (isCompleted) {
                completed++
            } else {
                val scheduled = todo.scheduledDay ?: todayEpochDay
                val isOverdue = scheduled < todayEpochDay
                if (isOverdue) overdue++ else pending++
            }
        }

        for (goal in goals) {
            val periodicity = goal.periodicity ?: GoalPeriodicity.MONTHLY
            val dueDay = goal.dueDay ?: Long.MAX_VALUE
            val createdDay = epochDayFromMillis(goal.createdAt)
            val effectiveStart = maxOf(startDay, createdDay)
            val effectiveEnd = minOf(endDay, dueDay)

            if (effectiveStart > effectiveEnd) continue

            val overdueEnd = minOf(effectiveEnd, todayEpochDay - 1)
            val pendingStart = maxOf(effectiveStart, todayEpochDay)

            val overdueExpected = if (effectiveStart <= overdueEnd) {
                expectedOccurrences(periodicity, effectiveStart, overdueEnd)
            } else {
                0
            }
            val pendingExpected = if (pendingStart <= effectiveEnd) {
                expectedOccurrences(periodicity, pendingStart, effectiveEnd)
            } else {
                0
            }

            val overdueProgress = if (effectiveStart <= overdueEnd) {
                sumGoalDelta(goalDeltaByGoalAndDay, goal.id, effectiveStart, overdueEnd)
            } else {
                0
            }.coerceAtLeast(0)

            val pendingProgress = if (pendingStart <= effectiveEnd) {
                sumGoalDelta(goalDeltaByGoalAndDay, goal.id, pendingStart, effectiveEnd)
            } else {
                0
            }.coerceAtLeast(0)

            val completedOverdue = overdueProgress.coerceAtMost(overdueExpected)
            val completedPending = pendingProgress.coerceAtMost(pendingExpected)

            completed += (completedOverdue + completedPending)
            overdue += (overdueExpected - completedOverdue).coerceAtLeast(0)
            pending += (pendingExpected - completedPending).coerceAtLeast(0)
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

    private fun rangesOverlap(startA: Long, endA: Long, startB: Long, endB: Long): Boolean {
        return startA <= endB && endA >= startB
    }

    private fun sumGoalDelta(
        goalDeltaByGoalAndDay: Map<String, Map<Long, Int>>,
        goalId: String,
        startDay: Long,
        endDay: Long
    ): Int {
        if (startDay > endDay) return 0
        val dayMap = goalDeltaByGoalAndDay[goalId] ?: return 0
        var sum = 0
        for (day in startDay..endDay) {
            sum += dayMap[day] ?: 0
        }
        return sum
    }

    private fun expectedOccurrences(periodicity: GoalPeriodicity, startDay: Long, endDay: Long): Int {
        if (startDay > endDay) return 0
        val start = LocalDate.ofEpochDay(startDay)
        val end = LocalDate.ofEpochDay(endDay)

        return when (periodicity) {
            GoalPeriodicity.DAILY -> (ChronoUnit.DAYS.between(start, end) + 1L).toInt()
            GoalPeriodicity.WEEKLY -> {
                val daysInclusive = (ChronoUnit.DAYS.between(start, end) + 1L).coerceAtLeast(0)
                if (daysInclusive == 0L) {
                    0
                } else {
                    kotlin.math.ceil(daysInclusive / 7.0).toInt()
                }
            }
            GoalPeriodicity.MONTHLY -> {
                val startMonth = YearMonth.from(start)
                val endMonth = YearMonth.from(end)
                (ChronoUnit.MONTHS.between(startMonth, endMonth) + 1L).toInt()
            }
            GoalPeriodicity.SEMESTRAL -> {
                val startIndex = start.year * 2 + if (start.monthValue <= 6) 0 else 1
                val endIndex = end.year * 2 + if (end.monthValue <= 6) 0 else 1
                (endIndex - startIndex + 1).coerceAtLeast(1)
            }
            GoalPeriodicity.ANNUAL -> (end.year - start.year + 1).coerceAtLeast(1)
        }
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
