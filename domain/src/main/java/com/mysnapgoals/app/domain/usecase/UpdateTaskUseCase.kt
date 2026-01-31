package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.repository.TasksRepository

class UpdateTaskUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task, now: Long = System.currentTimeMillis()) {
        repository.upsert(task.copy(updatedAt = now))
    }
}
