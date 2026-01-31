package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow

class ObserveGoalProgressEventsUseCase(
    private val repository: TasksRepository
) {
    operator fun invoke(): Flow<List<GoalProgressEvent>> =
        repository.observeGoalProgressEvents()
}
