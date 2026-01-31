package com.mysnapgoals.app.data.repository

import com.mysnapgoals.app.data.local.dao.GoalProgressEventDao
import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.mapper.toDomain
import com.mysnapgoals.app.data.mapper.toEntity
import com.mysnapgoals.app.domain.model.GoalProgressEvent
import com.mysnapgoals.app.domain.model.Task
import com.mysnapgoals.app.domain.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TasksRepositoryImpl(
    private val dao: TaskDao,
    private val goalEventDao: GoalProgressEventDao,
    appScope: CoroutineScope
) : TasksRepository {
    override val tasksState: StateFlow<List<Task>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .stateIn(
                scope = appScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = emptyList()
            )

    override fun observeAll(): Flow<List<Task>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(task: Task) = dao.upsert(task.toEntity())

    override suspend fun setDone(id: String, isDone: Boolean, now: Long) =
        dao.setDone(
            id = id,
            isDone = isDone,
            updatedAt = now,
            doneAt = if (isDone) now else null
        )

    override suspend fun setCurrent(id: String, current: Int, now: Long) =
        dao.setCurrent(id, current, updatedAt = now)

    override suspend fun seedIfEmpty(tasks: List<Task>) {
        if (dao.count() == 0) dao.upsertAll(tasks.map { it.toEntity() })
    }

    override suspend fun insertGoalProgressEvent(event: GoalProgressEvent) =
        goalEventDao.insert(event.toEntity())

    override fun observeGoalProgressEvents(): Flow<List<GoalProgressEvent>> =
        goalEventDao.observeAll().map { list -> list.map { it.toDomain() } }

    // Global (todas las metas)
    override suspend fun sumGoalsDeltaBetweenDays(startDay: Long, endDay: Long): Int =
        goalEventDao.sumDeltaBetweenDays(startDay, endDay)

    // Por goalId (si lo necesitas despues)
    override suspend fun sumDeltaForGoalBetweenDays(goalId: String, startDay: Long, endDay: Long): Int =
        goalEventDao.sumDeltaForGoalBetweenDays(goalId, startDay, endDay)

    // Por varios goalId (para evitar N queries)
    override suspend fun sumDeltaForGoalsBetweenDays(goalIds: List<String>, startDay: Long, endDay: Long): Int =
        if (goalIds.isEmpty()) 0 else goalEventDao.sumDeltaForGoalsBetweenDays(goalIds, startDay, endDay)

}
