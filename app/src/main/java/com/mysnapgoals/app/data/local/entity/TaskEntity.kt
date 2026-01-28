package com.mysnapgoals.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val type: Int, // 0 TODO, 1 GOAL
    val title: String,
    val isDone: Boolean,
    val scheduledDay: Long?, // LocalDate.toEpochDay(), null = sin fecha (MVP: tratamos null como "hoy")
    val createdAt: Long,
    val updatedAt: Long?, // Solo GOAL (nullable para TODO)
    val current: Int?,
    val target: Int?,
    val doneAt: Long?
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
    val delta: Int,          // normalmente +1
    val timestamp: Long,     // System.currentTimeMillis()
    val epochDay: Long       // LocalDate.now().toEpochDay() (para queries rápidas)
)