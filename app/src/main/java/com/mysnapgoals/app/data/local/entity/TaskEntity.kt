package com.mysnapgoals.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val type: Int, // 0 TODO, 1 GOAL
    val title: String,
    val isDone: Boolean,
    val scheduledDay: Long?, // LocalDate.toEpochDay(), null = sin fecha (MVP: tratamos null como "hoy")
    val createdAt: Long,
    val updatedAt: Long, // Solo GOAL (nullable para TODO)
    val current: Int?,
    val target: Int?
) {
    companion object {
        const val TYPE_TODO = 0
        const val TYPE_GOAL = 1
    }
}
