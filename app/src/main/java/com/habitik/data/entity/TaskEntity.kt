package com.habitik.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,          // HEALTH, WORK, PERSONAL, FAMILY, SPIRITUAL, OTHER
    val colorHex: String,          // Category gradient start color
    val startTime: String,         // "HH:mm" 24hr format
    val durationMin: Int,          // Duration in minutes
    val isFlexible: Boolean,       // false=fixed, true=flexible window
    val flexWindowEnd: String?,    // "HH:mm" latest start time if flexible
    val repeatDays: String,        // JSON array: ["MON","TUE",...] or "DAILY","WEEKDAYS","WEEKENDS"
    val repeatCount: Int = 1,      // How many times per day (e.g., water intake = 8)
    val isImportant: Boolean = false, // true = tracked in streak
    val reminderMin: Int = 5,      // Pre-alert minutes before task
    val isActive: Boolean = true,  // Soft delete
    val createdAt: Long = System.currentTimeMillis()
)
