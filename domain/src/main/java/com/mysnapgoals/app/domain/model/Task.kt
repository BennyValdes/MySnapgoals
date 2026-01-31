package com.mysnapgoals.app.domain.model

enum class TaskType {
    TODO,
    GOAL
}

data class Task(
    val id: String,
    val type: TaskType,
    val title: String,
    val isDone: Boolean,
    val scheduledDay: Long?,
    val createdAt: Long,
    val updatedAt: Long?,
    val current: Int?,
    val target: Int?,
    val doneAt: Long?
)
