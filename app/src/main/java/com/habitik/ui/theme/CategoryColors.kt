package com.habitik.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Modern Vibrant Category Colors
val HealthColorStart = Color(0xFFF43F5E) // Rose 500
val HealthColorEnd = Color(0xFFFB923C)   // Amber 400

val WorkColorStart = Color(0xFF06B6D4)   // Cyan 500
val WorkColorEnd = Color(0xFF3B82F6)     // Blue 500

val PersonalColorStart = Color(0xFF8B5CF6) // Violet 500
val PersonalColorEnd = Color(0xFFD946EF)   // Fuchsia 500

val FamilyColorStart = Color(0xFFF59E0B)   // Amber 500
val FamilyColorEnd = Color(0xFFEF4444)     // Red 500

val SpiritualColorStart = Color(0xFF6366F1) // Indigo 500
val SpiritualColorEnd = Color(0xFFA855F7)   // Purple 500

val OtherColorStart = Color(0xFF10B981)    // Emerald 500
val OtherColorEnd = Color(0xFF3B82F6)      // Blue 500

enum class TaskCategory {
    HEALTH, WORK, PERSONAL, FAMILY, SPIRITUAL, OTHER
}

fun getCategoryGradient(category: String): Brush {
    val (start, end) = when (category.uppercase()) {
        "HEALTH" -> HealthColorStart to HealthColorEnd
        "WORK" -> WorkColorStart to WorkColorEnd
        "PERSONAL" -> PersonalColorStart to PersonalColorEnd
        "FAMILY" -> FamilyColorStart to FamilyColorEnd
        "SPIRITUAL" -> SpiritualColorStart to SpiritualColorEnd
        else -> OtherColorStart to OtherColorEnd
    }
    return Brush.linearGradient(listOf(start, end))
}

fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "HEALTH" -> HealthColorStart
        "WORK" -> WorkColorStart
        "PERSONAL" -> PersonalColorStart
        "FAMILY" -> FamilyColorStart
        "SPIRITUAL" -> SpiritualColorStart
        else -> OtherColorStart
    }
}
