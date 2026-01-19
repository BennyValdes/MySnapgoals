package com.mysnapgoals.app

import android.content.Context
import androidx.room.Room
import com.mysnapgoals.app.data.local.SnapGoalsDatabase
import com.mysnapgoals.app.data.repository.TasksRepository

object SnapGoalsGraph {
    lateinit var db: SnapGoalsDatabase
        private set

    lateinit var tasksRepository: TasksRepository
        private set

    fun init(context: Context) {
        db = Room.databaseBuilder(
            context,
            SnapGoalsDatabase::class.java,
            "snapgoals.db"
        ).build()

        tasksRepository = TasksRepository(db.taskDao())
    }
}