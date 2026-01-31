package com.mysnapgoals.app.domain.model

data class GoalProgressEvent(
    val id: String,
    val goalId: String,
    val delta: Int,
    val timestamp: Long,
    val epochDay: Long
)
