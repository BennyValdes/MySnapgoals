package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.repository.TasksRepository
import java.util.UUID

class AddTodoUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(title: String, scheduledDay: Long, now: Long = System.currentTimeMillis()) {
        repository.upsert(
            Task(
                id = UUID.randomUUID().toString(),
                type = TaskType.TODO,
                title = title,
                isDone = false,
                scheduledDay = scheduledDay,
                createdAt = now,
                updatedAt = now,
                current = null,
                target = null,
                doneAt = null
            )
        )
    }
}
