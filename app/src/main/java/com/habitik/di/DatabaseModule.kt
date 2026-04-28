package com.habitik.di

import android.content.Context
import com.habitik.data.HabitikDatabase
import com.habitik.data.dao.StreakDao
import com.habitik.data.dao.TaskDao
import com.habitik.data.dao.TaskLogDao
import com.habitik.data.dao.RoutineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitikDatabase {
        return HabitikDatabase.getDatabase(context)
    }

    @Provides
    fun provideTaskDao(database: HabitikDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideTaskLogDao(database: HabitikDatabase): TaskLogDao = database.taskLogDao()

    @Provides
    fun provideStreakDao(database: HabitikDatabase): StreakDao = database.streakDao()

    @Provides
    fun provideRoutineDao(database: HabitikDatabase): RoutineDao = database.routineDao()
}
