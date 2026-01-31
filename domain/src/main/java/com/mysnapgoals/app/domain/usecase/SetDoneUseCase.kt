package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.repository.TasksRepository

class SetDoneUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(id: String, isDone: Boolean, now: Long = System.currentTimeMillis()) {
        repository.setDone(id, isDone, now)
    }
}
