package com.mysnapgoals.app.domain.util

import com.mysnapgoals.app.domain.model.GoalProgressEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalProgressIndexTest {

    @Test
    fun sum_aggregatesMultipleEventsPerDayAndRange() {
        val events = listOf(
            event(goalId = "g1", day = 100, delta = 1),
            event(goalId = "g1", day = 100, delta = 2),
            event(goalId = "g1", day = 101, delta = -1),
            event(goalId = "g1", day = 105, delta = 4),
            event(goalId = "g2", day = 100, delta = 7)
        )
        val index = GoalProgressIndex.from(events)

        assertEquals(2, index.sum("g1", 100, 101))
        assertEquals(6, index.sum("g1", 100, 105))
        assertEquals(7, index.sum("g2", 100, 100))
    }

    @Test
    fun sum_handlesUnknownGoalAndInvalidRange() {
        val index = GoalProgressIndex.from(emptyList())

        assertEquals(0, index.sum("missing", 10, 20))
        assertEquals(0, index.sum("missing", 20, 10))
    }

    private fun event(goalId: String, day: Long, delta: Int): GoalProgressEvent {
        return GoalProgressEvent(
            id = "$goalId-$day-$delta",
            goalId = goalId,
            delta = delta,
            timestamp = 0L,
            epochDay = day
        )
    }
}
