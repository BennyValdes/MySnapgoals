package com.mysnapgoals.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mysnapgoals.app.data.local.dao.GoalProgressEventDao
import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.local.entity.GoalProgressEventEntity
import com.mysnapgoals.app.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        GoalProgressEventEntity::class
    ],
    version = 2
)
abstract class SnapGoalsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun goalProgressEventDao(): GoalProgressEventDao
}