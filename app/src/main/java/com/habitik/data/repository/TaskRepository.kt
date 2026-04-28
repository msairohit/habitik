package com.habitik.data.repository

import com.habitik.data.dao.TaskDao
import com.habitik.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllActiveTasks(): Flow<List<TaskEntity>> = taskDao.getAllActiveTasks()
    
    fun getTasksByDay(dayOfWeek: String): Flow<List<TaskEntity>> = taskDao.getTasksByDay(dayOfWeek)
    
    suspend fun getTaskById(id: Int): TaskEntity? = taskDao.getTaskById(id)
    
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    
    suspend fun softDeleteTask(id: Int) = taskDao.softDeleteTask(id)
}
