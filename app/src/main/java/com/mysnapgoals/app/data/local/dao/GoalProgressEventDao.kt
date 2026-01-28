package com.mysnapgoals.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mysnapgoals.app.data.local.entity.GoalProgressEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalProgressEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: GoalProgressEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<GoalProgressEventEntity>)

    @Query("SELECT * FROM goal_progress_events WHERE epochDay BETWEEN :startDay AND :endDay")
    fun observeBetweenDays(startDay: Long, endDay: Long): Flow<List<GoalProgressEventEntity>>

    @Query("SELECT COALESCE(SUM(delta), 0) FROM goal_progress_events WHERE goalId = :goalId AND epochDay BETWEEN :startDay AND :endDay")
    suspend fun sumDeltaForGoalBetweenDays(goalId: String, startDay: Long, endDay: Long): Int

    @Query("SELECT COALESCE(SUM(delta), 0) FROM goal_progress_events WHERE epochDay BETWEEN :startDay AND :endDay")
    suspend fun sumDeltaBetweenDays(startDay: Long, endDay: Long): Int
}