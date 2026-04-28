package com.habitik.data.dao

import androidx.room.*
import com.habitik.data.Routine
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: Routine): Long

    @Update
    suspend fun update(routine: Routine)

    @Delete
    suspend fun delete(routine: Routine)

    @Query("UPDATE routines SET isCompletedToday = :completed WHERE id = :id")
    suspend fun markAsDone(id: Int, completed: Boolean)
}
