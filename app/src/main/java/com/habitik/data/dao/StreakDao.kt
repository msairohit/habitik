package com.habitik.data.dao

import androidx.room.*
import com.habitik.data.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Upsert
    suspend fun insertOrUpdateStreak(streak: StreakEntity)

    @Query("SELECT * FROM streaks WHERE taskId = :taskId")
    suspend fun getStreakForTask(taskId: Int): StreakEntity?

    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<StreakEntity>>
}
