package com.mysnapgoals.app.data.mapper

import com.mysnapgoals.app.data.local.entity.GoalProgressEventEntity
import com.mysnapgoals.app.domain.model.GoalProgressEvent

fun GoalProgressEvent.toEntity(): GoalProgressEventEntity =
    GoalProgressEventEntity(
        id = id,
        goalId = goalId,
        delta = delta,
        timestamp = timestamp,
        epochDay = epochDay
    )

fun GoalProgressEventEntity.toDomain(): GoalProgressEvent =
    GoalProgressEvent(
        id = id,
        goalId = goalId,
        delta = delta,
        timestamp = timestamp,
        epochDay = epochDay
    )
