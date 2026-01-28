package com.mysnapgoals.app.data.repository

import com.mysnapgoals.app.data.local.dao.GoalProgressEventDao
import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.local.entity.GoalProgressEventEntity
import com.mysnapgoals.app.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TasksRepository(
    private val dao: TaskDao,
    private val goalEventDao: GoalProgressEventDao,
    appScope: CoroutineScope
) {
    val tasksState: StateFlow<List<TaskEntity>> =
        dao.observeAll().stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = emptyList()
        )
    fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun upsert(task: TaskEntity) = dao.upsert(task)

    suspend fun setDone(id: String, isDone: Boolean, now: Long) =
        dao.setDone(
            id = id,
            isDone = isDone,
            updatedAt = now,
            doneAt = if (isDone) now else null
        )

    suspend fun setCurrent(id: String, current: Int, now: Long) =
        dao.setCurrent(id, current, updatedAt = now)

    suspend fun seedIfEmpty(tasks: List<TaskEntity>) {
        if (dao.count() == 0) dao.upsertAll(tasks)
    }

    suspend fun insertGoalProgressEvent(event: GoalProgressEventEntity) =
        goalEventDao.insert(event)

    // Global (todas las metas)
    suspend fun sumGoalsDeltaBetweenDays(startDay: Long, endDay: Long): Int =
        goalEventDao.sumDeltaBetweenDays(startDay, endDay)

    // Por goalId (si lo necesitas después)
    suspend fun sumDeltaForGoalBetweenDays(goalId: String, startDay: Long, endDay: Long): Int =
        goalEventDao.sumDeltaForGoalBetweenDays(goalId, startDay, endDay)

}