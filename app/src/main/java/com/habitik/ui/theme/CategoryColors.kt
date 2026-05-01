package com.habitik.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

enum class TaskCategory {
    HEALTH, WORK, PERSONAL, FAMILY, SPIRITUAL, OTHER
}

/**
 * Returns a SolidColor Brush for the given category string.
 */
fun getCategoryBrush(category: String): Brush {
    val color = getCategoryColor(category)
    return SolidColor(color)
}

/**
 * Returns the primary color associated with a category string.
 */
fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "HEALTH"    -> HealthColor
        "WORK"      -> WorkColor
        "PERSONAL"  -> PersonalColor
        "FAMILY"    -> FamilyColor
        "SPIRITUAL" -> SpiritualColor
        else        -> OtherColor
    }
}

/**
 * Returns the primary color associated with a TaskCategory enum.
 */
fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.HEALTH    -> HealthColor
        TaskCategory.WORK      -> WorkColor
        TaskCategory.PERSONAL  -> PersonalColor
        TaskCategory.FAMILY    -> FamilyColor
        TaskCategory.SPIRITUAL -> SpiritualColor
        TaskCategory.OTHER     -> OtherColor
    }
}

/**
 * Returns a representative emoji for the category.
 */
fun getCategoryEmoji(category: String): String {
    return when (category.uppercase()) {
        "HEALTH"    -> "🥗"
        "WORK"      -> "💻"
        "PERSONAL"  -> "✨"
        "FAMILY"    -> "🏠"
        "SPIRITUAL" -> "🧘"
        else        -> "⏳"
    }
}
