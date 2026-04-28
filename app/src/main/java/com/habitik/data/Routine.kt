package com.habitik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTime: String,
    val durationMinutes: Int,
    val daysOfWeek: String,
    val isCompletedToday: Boolean = false
)
