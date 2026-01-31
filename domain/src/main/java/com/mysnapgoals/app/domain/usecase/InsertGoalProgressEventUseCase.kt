package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.repository.TasksRepository

class InsertGoalProgressEventUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(event: GoalProgressEvent) {
        repository.insertGoalProgressEvent(event)
    }
}
