package com.mysnapgoals.app.domain.util

import com.mysnapgoals.app.domain.model.GoalPeriodicity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

object GoalPeriodMath {
    fun periodRangeForToday(periodicity: GoalPeriodicity, todayEpochDay: Long): Pair<Long, Long> {
        val today = LocalDate.ofEpochDay(todayEpochDay)
        return when (periodicity) {
            GoalPeriodicity.DAILY -> todayEpochDay to todayEpochDay
            GoalPeriodicity.WEEKLY -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.MONTHLY -> {
                val start = today.withDayOfMonth(1)
                val end = start.plusMonths(1).minusDays(1)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.SEMESTRAL -> {
                val start = if (today.monthValue <= 6) {
                    LocalDate.of(today.year, 1, 1)
                } else {
                    LocalDate.of(today.year, 7, 1)
                }
                val end = start.plusMonths(6).minusDays(1)
                start.toEpochDay() to end.toEpochDay()
            }
            GoalPeriodicity.ANNUAL -> {
                val start = LocalDate.of(today.year, 1, 1)
                val end = LocalDate.of(today.year, 12, 31)
                start.toEpochDay() to end.toEpochDay()
            }
        }
    }

    fun periodTarget(periodicity: GoalPeriodicity, todayEpochDay: Long): Int {
        val (start, end) = periodRangeForToday(periodicity, todayEpochDay)
        return (end - start + 1).toInt().coerceAtLeast(1)
    }

    fun expectedOccurrences(periodicity: GoalPeriodicity, startDay: Long, endDay: Long): Int {
        if (startDay > endDay) return 0
        val start = LocalDate.ofEpochDay(startDay)
        val end = LocalDate.ofEpochDay(endDay)

        return when (periodicity) {
            GoalPeriodicity.DAILY -> (ChronoUnit.DAYS.between(start, end) + 1L).toInt()
            GoalPeriodicity.WEEKLY -> {
                val daysInclusive = (ChronoUnit.DAYS.between(start, end) + 1L).coerceAtLeast(0)
                if (daysInclusive == 0L) 0 else ceil(daysInclusive / 7.0).toInt()
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
}
