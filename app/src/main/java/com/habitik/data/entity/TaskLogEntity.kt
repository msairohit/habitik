package com.habitik.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_logs",
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
data class TaskLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val logDate: String,             // "YYYY-MM-DD"
    val status: String,              // DONE | SKIPPED | PENDING | SNOOZED | PAUSED
    val doneAt: Long? = null,        // Unix timestamp when marked done
    val startedAt: Long? = null,     // Unix timestamp when last started/resumed
    val remainingDurationMin: Int? = null, // Remaining minutes if paused
    val remainingDurationSec: Long? = null, // Remaining seconds if paused (more precise)
    val completedValue: Float? = null,     // For COUNT and QUANTITY progress
    val occurrence: Int = 1          // Which repetition (for repeatCount tasks)
)
