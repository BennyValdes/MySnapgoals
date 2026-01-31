package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.repository.TasksRepository
import java.util.UUID

class AddGoalUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(title: String, target: Int, scheduledDay: Long, now: Long = System.currentTimeMillis()) {
        repository.upsert(
            Task(
                id = UUID.randomUUID().toString(),
                type = TaskType.GOAL,
                title = title,
                isDone = false,
                scheduledDay = scheduledDay,
                createdAt = now,
                updatedAt = now,
                current = 0,
                target = target,
                doneAt = null
            )
        )
    }
}
