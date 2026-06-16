package com.habitik.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitik.data.dao.TaskDao
import com.habitik.data.dao.TaskLogDao
import com.habitik.data.dao.StreakDao
import com.habitik.data.dao.RoutineDao
import com.habitik.data.entity.TaskEntity
import com.habitik.data.entity.TaskLogEntity
import com.habitik.data.entity.StreakEntity

@Database(
    entities = [TaskEntity::class, TaskLogEntity::class, StreakEntity::class, Routine::class],
    version = 10, // Incremented for schema changes
    exportSchema = false
)
abstract class HabitikDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskLogDao(): TaskLogDao
    abstract fun streakDao(): StreakDao
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: HabitikDatabase? = null

        fun getDatabase(context: Context): HabitikDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitikDatabase::class.java,
                    "habitik_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
