package com.mysnapgoals.app.domain.util

import com.mysnapgoals.app.domain.model.GoalPeriodicity
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalPeriodMathTest {

    @Test
    fun periodRangeForToday_weekly_usesMondayToSunday() {
        val day = LocalDate.of(2026, 2, 15).toEpochDay() // Sunday

        val (start, end) = GoalPeriodMath.periodRangeForToday(GoalPeriodicity.WEEKLY, day)

        assertEquals(LocalDate.of(2026, 2, 9).toEpochDay(), start)
        assertEquals(LocalDate.of(2026, 2, 15).toEpochDay(), end)
    }

    @Test
    fun periodTarget_monthly_returnsMonthLength() {
        val day = LocalDate.of(2026, 2, 10).toEpochDay()

        val target = GoalPeriodMath.periodTarget(GoalPeriodicity.MONTHLY, day)

        assertEquals(28, target)
    }

    @Test
    fun expectedOccurrences_handlesAllPeriodicities() {
        val start = LocalDate.of(2026, 1, 1).toEpochDay()
        val end = LocalDate.of(2026, 3, 15).toEpochDay()

        assertEquals(74, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.DAILY, start, end))
        assertEquals(11, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.WEEKLY, start, end))
        assertEquals(3, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.MONTHLY, start, end))
        assertEquals(1, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.SEMESTRAL, start, end))
        assertEquals(1, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.ANNUAL, start, end))
    }

    @Test
    fun expectedOccurrences_returnsZeroForInvalidRange() {
        val start = LocalDate.of(2026, 2, 2).toEpochDay()
        val end = LocalDate.of(2026, 2, 1).toEpochDay()

        assertEquals(0, GoalPeriodMath.expectedOccurrences(GoalPeriodicity.DAILY, start, end))
    }
}
