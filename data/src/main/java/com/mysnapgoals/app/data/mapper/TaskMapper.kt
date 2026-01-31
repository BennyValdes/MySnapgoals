package com.mysnapgoals.app.data.mapper

import com.mysnapgoals.app.data.local.entity.TaskEntity
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.TaskType

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        type = if (type == TaskEntity.TYPE_TODO) TaskType.TODO else TaskType.GOAL,
        title = title,
        isDone = isDone,
        scheduledDay = scheduledDay,
        createdAt = createdAt,
        updatedAt = updatedAt,
        current = current,
        target = target,
        doneAt = doneAt
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        type = if (type == TaskType.TODO) TaskEntity.TYPE_TODO else TaskEntity.TYPE_GOAL,
        title = title,
        isDone = isDone,
        scheduledDay = scheduledDay,
        createdAt = createdAt,
        updatedAt = updatedAt,
        current = current,
        target = target,
        doneAt = doneAt
    )
