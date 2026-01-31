package com.mysnapgoals.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val type: Int,
    val title: String,
    val isDone: Boolean,
    val scheduledDay: Long?,
    val createdAt: Long,
    val updatedAt: Long?,
    val current: Int?,
    val target: Int?,
    val doneAt: Long?,
    val periodicity: Int?,
    val dueDay: Long?
) {
    companion object {
        const val TYPE_TODO = 0
        const val TYPE_GOAL = 1
    }
}

@Entity(
    tableName = "goal_progress_events",
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["timestamp"])
    ]
)
data class GoalProgressEventEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val delta: Int,
    val timestamp: Long,
    val epochDay: Long
)

