package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.repository.TasksRepository

class SumGoalProgressUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(goalIds: List<String>, startDay: Long, endDay: Long): Int {
        return repository.sumDeltaForGoalsBetweenDays(goalIds, startDay, endDay)
    }
}
