package com.habitik.data.repository

import com.habitik.data.dao.TaskLogDao
import com.habitik.data.entity.TaskLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskLogRepository @Inject constructor(
    private val taskLogDao: TaskLogDao
) {
    fun getLogsForDate(date: String): Flow<List<TaskLogEntity>> = taskLogDao.getLogsForDate(date)
    
    fun getLogsForTask(taskId: Int): Flow<List<TaskLogEntity>> = taskLogDao.getLogsForTask(taskId)
    
    fun getLogsInRange(startDate: String, endDate: String): Flow<List<TaskLogEntity>> = 
        taskLogDao.getLogsInRange(startDate, endDate)
        
    suspend fun insertLog(log: TaskLogEntity): Long = taskLogDao.insertLog(log)
    
    suspend fun updateLog(log: TaskLogEntity) = taskLogDao.updateLog(log)
}
