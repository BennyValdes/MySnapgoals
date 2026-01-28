package com.mysnapgoals.app

import android.content.Context
import androidx.room.Room
import com.mysnapgoals.app.data.local.MIGRATION_1_2
import com.mysnapgoals.app.data.local.SnapGoalsDatabase
import com.mysnapgoals.app.data.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object SnapGoalsGraph {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var db: SnapGoalsDatabase
        private set

    lateinit var tasksRepository: TasksRepository
        private set

    fun init(context: Context) {
        db = Room.databaseBuilder(
            context,
            SnapGoalsDatabase::class.java,
            "snapgoals.db"
        ).addMigrations(MIGRATION_1_2).build()

        tasksRepository = TasksRepository(
            dao = db.taskDao(),
            goalEventDao = db.goalProgressEventDao(),
            appScope = appScope
        )
    }
}