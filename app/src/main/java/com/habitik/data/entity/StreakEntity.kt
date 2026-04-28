package com.habitik.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streaks",
    indices = [Index(value = ["taskId"])],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StreakEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastDoneDate: String?,       // "YYYY-MM-DD"
    val graceUsed: Int = 0           // Grace days used this week
)
