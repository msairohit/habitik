package com.habitik.data.dao

import androidx.room.*
import com.habitik.data.entity.TaskLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TaskLogEntity): Long

    @Update
    suspend fun updateLog(log: TaskLogEntity)

    @Query("SELECT * FROM task_logs WHERE logDate = :date")
    fun getLogsForDate(date: String): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId")
    fun getLogsForTask(taskId: Int): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs WHERE logDate = :date AND taskId = :taskId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestLogForTask(taskId: Int, date: String): TaskLogEntity?

    @Query("SELECT * FROM task_logs WHERE logDate BETWEEN :startDate AND :endDate")
    fun getLogsInRange(startDate: String, endDate: String): Flow<List<TaskLogEntity>>

    @Query("SELECT * FROM task_logs")
    fun getAllLogs(): Flow<List<TaskLogEntity>>
}
