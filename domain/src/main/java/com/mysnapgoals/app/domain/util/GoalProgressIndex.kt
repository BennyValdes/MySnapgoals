package com.mysnapgoals.app.domain.util

import com.mysnapgoals.app.domain.model.GoalProgressEvent

class GoalProgressIndex private constructor(
    private val cumulativeByGoal: Map<String, List<DayCumulative>>
) {
    fun sum(goalId: String, startDay: Long, endDay: Long): Int {
        if (startDay > endDay) return 0
        val series = cumulativeByGoal[goalId] ?: return 0
        if (series.isEmpty()) return 0

        val endCum = cumulativeAtOrBefore(series, endDay)
        val beforeStartCum = cumulativeAtOrBefore(series, startDay - 1)
        return endCum - beforeStartCum
    }

    private fun cumulativeAtOrBefore(series: List<DayCumulative>, day: Long): Int {
        var low = 0
        var high = series.lastIndex
        var best = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (series[mid].day <= day) {
                best = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return if (best >= 0) series[best].cumulative else 0
    }

    private data class DayCumulative(
        val day: Long,
        val cumulative: Int
    )

    companion object {
        fun from(events: List<GoalProgressEvent>): GoalProgressIndex {
            val byGoal = events.groupBy { it.goalId }

            val cumulativeByGoal = byGoal.mapValues { (_, goalEvents) ->
                val dayTotals = goalEvents
                    .groupBy { it.epochDay }
                    .mapValues { (_, dayEvents) -> dayEvents.sumOf { it.delta } }
                    .toSortedMap()

                var running = 0
                dayTotals.map { (day, delta) ->
                    running += delta
                    DayCumulative(day = day, cumulative = running)
                }
            }

            return GoalProgressIndex(cumulativeByGoal)
        }
    }
}
