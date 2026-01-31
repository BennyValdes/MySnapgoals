package com.mysnapgoals.app.di

import com.mysnapgoals.app.domain.repository.TasksRepository
import com.mysnapgoals.app.domain.usecase.AddGoalUseCase
import com.mysnapgoals.app.domain.usecase.AddTodoUseCase
import com.mysnapgoals.app.domain.usecase.InsertGoalProgressEventUseCase
import com.mysnapgoals.app.domain.usecase.ObserveTasksUseCase
import com.mysnapgoals.app.domain.usecase.SetCurrentUseCase
import com.mysnapgoals.app.domain.usecase.SetDoneUseCase
import com.mysnapgoals.app.domain.usecase.SumGoalProgressUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideObserveTasksUseCase(repo: TasksRepository) = ObserveTasksUseCase(repo)

    @Provides
    @Singleton
    fun provideAddTodoUseCase(repo: TasksRepository) = AddTodoUseCase(repo)

    @Provides
    @Singleton
    fun provideAddGoalUseCase(repo: TasksRepository) = AddGoalUseCase(repo)

    @Provides
    @Singleton
    fun provideSetDoneUseCase(repo: TasksRepository) = SetDoneUseCase(repo)

    @Provides
    @Singleton
    fun provideSetCurrentUseCase(repo: TasksRepository) = SetCurrentUseCase(repo)

    @Provides
    @Singleton
    fun provideInsertGoalProgressEventUseCase(repo: TasksRepository) = InsertGoalProgressEventUseCase(repo)

    @Provides
    @Singleton
    fun provideSumGoalProgressUseCase(repo: TasksRepository) = SumGoalProgressUseCase(repo)
}
