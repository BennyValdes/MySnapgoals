package com.mysnapgoals.app.domain.repository

import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TasksRepository {
    val tasksState: StateFlow<List<Task>>
    fun observeAll(): Flow<List<Task>>
    suspend fun upsert(task: Task)
    suspend fun setDone(id: String, isDone: Boolean, now: Long)
    suspend fun setCurrent(id: String, current: Int, now: Long)
    suspend fun seedIfEmpty(tasks: List<Task>)
    suspend fun insertGoalProgressEvent(event: GoalProgressEvent)
    fun observeGoalProgressEvents(): Flow<List<GoalProgressEvent>>
    suspend fun sumGoalsDeltaBetweenDays(startDay: Long, endDay: Long): Int
    suspend fun sumDeltaForGoalBetweenDays(goalId: String, startDay: Long, endDay: Long): Int
    suspend fun sumDeltaForGoalsBetweenDays(goalIds: List<String>, startDay: Long, endDay: Long): Int
}
