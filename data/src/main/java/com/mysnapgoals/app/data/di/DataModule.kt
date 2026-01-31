package com.mysnapgoals.app.data.di

import android.content.Context
import androidx.room.Room
import com.mysnapgoals.app.data.local.MIGRATION_1_2
import com.mysnapgoals.app.data.local.MIGRATION_2_3
import com.mysnapgoals.app.data.local.SnapGoalsDatabase
import com.mysnapgoals.app.data.local.dao.GoalProgressEventDao
import com.mysnapgoals.app.data.local.dao.TaskDao
import com.mysnapgoals.app.data.repository.TasksRepositoryImpl
import com.mysnapgoals.app.domain.repository.TasksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SnapGoalsDatabase =
        Room.databaseBuilder(
            context,
            SnapGoalsDatabase::class.java,
            "snapgoals.db"
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()

    @Provides
    fun provideTaskDao(db: SnapGoalsDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideGoalProgressEventDao(db: SnapGoalsDatabase): GoalProgressEventDao = db.goalProgressEventDao()

    @Provides
    @Singleton
    fun provideTasksRepository(
        dao: TaskDao,
        goalEventDao: GoalProgressEventDao,
        appScope: CoroutineScope
    ): TasksRepository = TasksRepositoryImpl(dao, goalEventDao, appScope)
}
