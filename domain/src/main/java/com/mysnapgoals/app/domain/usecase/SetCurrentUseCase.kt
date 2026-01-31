package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.repository.TasksRepository

class SetCurrentUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(id: String, current: Int, now: Long = System.currentTimeMillis()) {
        repository.setCurrent(id, current, now)
    }
}
