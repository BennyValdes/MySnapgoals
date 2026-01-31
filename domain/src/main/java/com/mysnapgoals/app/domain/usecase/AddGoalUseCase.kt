package com.mysnapgoals.app.domain.usecase

import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType
import com.mysnapgoals.app.domain.repository.TasksRepository
import java.util.UUID

class AddGoalUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(
        title: String,
        periodicity: GoalPeriodicity,
        dueDay: Long,
        scheduledDay: Long,
        now: Long = System.currentTimeMillis()
    ) {
        repository.upsert(
            Task(
                id = UUID.randomUUID().toString(),
                type = TaskType.GOAL,
                title = title,
                isDone = false,
                scheduledDay = scheduledDay,
                createdAt = now,
                updatedAt = now,
                current = null,
                target = null,
                doneAt = null,
                periodicity = periodicity,
                dueDay = dueDay
            )
        )
    }
}
