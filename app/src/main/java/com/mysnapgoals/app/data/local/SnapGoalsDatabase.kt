package com.mysnapgoals.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SnapGoalsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}