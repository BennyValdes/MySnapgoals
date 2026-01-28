package com.mysnapgoals.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS goal_progress_events (
                id TEXT NOT NULL PRIMARY KEY,
                goalId TEXT NOT NULL,
                delta INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                epochDay INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_progress_events_goalId ON goal_progress_events(goalId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_progress_events_timestamp ON goal_progress_events(timestamp)")

        // Si agregas doneAt:
        db.execSQL("ALTER TABLE tasks ADD COLUMN doneAt INTEGER")
    }
}