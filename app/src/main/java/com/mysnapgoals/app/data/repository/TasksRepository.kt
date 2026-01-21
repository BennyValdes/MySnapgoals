package com.mysnapgoals.app.data.repository

import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TasksRepository(
    private val dao: TaskDao,
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
        dao.setDone(id, isDone, updatedAt = now)

    suspend fun setCurrent(id: String, current: Int, now: Long) =
        dao.setCurrent(id, current, updatedAt = now)

    suspend fun seedIfEmpty(tasks: List<TaskEntity>) {
        if (dao.count() == 0) dao.upsertAll(tasks)
    }
}