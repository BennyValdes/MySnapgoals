package com.mysnapgoals.app.domain.model

enum class TaskType {
    TODO,
    GOAL
}

enum class GoalPeriodicity(val days: Int) {
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30),
    SEMESTRAL(180),
    ANNUAL(365)
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
    val doneAt: Long?,
    val periodicity: GoalPeriodicity?,
    val dueDay: Long?
)
