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
    val status: String,              // DONE | SKIPPED | PENDING | SNOOZED
    val doneAt: Long?,               // Unix timestamp when marked done
    val occurrence: Int = 1          // Which repetition (for repeatCount tasks)
)
