package com.mysnapgoals.app.data.mapper

import com.mysnapgoals.app.data.local.entity.TaskEntity
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.model.GoalPeriodicity
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
        doneAt = doneAt,
        periodicity = periodicity?.let { fromPeriodicityDbValue(it) },
        dueDay = dueDay
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
        doneAt = doneAt,
        periodicity = periodicity?.days,
        dueDay = dueDay
    )

private fun fromPeriodicityDbValue(value: Int): GoalPeriodicity =
    when (value) {
        GoalPeriodicity.DAILY.days -> GoalPeriodicity.DAILY
        GoalPeriodicity.WEEKLY.days -> GoalPeriodicity.WEEKLY
        GoalPeriodicity.MONTHLY.days -> GoalPeriodicity.MONTHLY
        GoalPeriodicity.SEMESTRAL.days -> GoalPeriodicity.SEMESTRAL
        GoalPeriodicity.ANNUAL.days -> GoalPeriodicity.ANNUAL
        else -> GoalPeriodicity.MONTHLY
    }
