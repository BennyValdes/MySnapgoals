package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.repository.TasksRepository

class SumGoalProgressForGoalUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(goalId: String, startDay: Long, endDay: Long): Int {
        return repository.sumDeltaForGoalBetweenDays(goalId, startDay, endDay)
    }
}
