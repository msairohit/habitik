package com.habitik.data.dao

import androidx.room.*
import com.habitik.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteTask(id: Int)

    @Query("SELECT * FROM tasks WHERE isActive = 1")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isActive = 1 AND repeatDays LIKE '%' || :dayOfWeek || '%'")
    fun getTasksByDay(dayOfWeek: String): Flow<List<TaskEntity>>
}
