package com.habitik.data.repository

import com.habitik.data.dao.StreakDao
import com.habitik.data.entity.StreakEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val streakDao: StreakDao
) {
    suspend fun getStreakForTask(taskId: Int): StreakEntity? = streakDao.getStreakForTask(taskId)
    
    fun getAllStreaks(): Flow<List<StreakEntity>> = streakDao.getAllStreaks()
    
    suspend fun insertOrUpdateStreak(streak: StreakEntity) = streakDao.insertOrUpdateStreak(streak)
}
