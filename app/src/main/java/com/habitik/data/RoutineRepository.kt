package com.habitik.data

import com.habitik.data.dao.RoutineDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(private val routineDao: RoutineDao) {
    
    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()

    suspend fun insert(routine: Routine): Long {
        return routineDao.insert(routine)
    }

    suspend fun update(routine: Routine) {
        routineDao.update(routine)
    }

    suspend fun delete(routine: Routine) {
        routineDao.delete(routine)
    }

    suspend fun markAsDone(id: Int, completed: Boolean) {
        routineDao.markAsDone(id, completed)
    }
}
