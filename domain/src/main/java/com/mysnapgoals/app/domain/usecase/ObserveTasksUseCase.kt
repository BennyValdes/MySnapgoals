package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.repository.TasksRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(
    private val repository: TasksRepository
) {
    operator fun invoke(): Flow<List<Task>> = repository.observeAll()
}
