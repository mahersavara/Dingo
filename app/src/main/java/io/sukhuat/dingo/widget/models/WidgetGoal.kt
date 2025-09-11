package io.sukhuat.dingo.widget.models

import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * Simplified goal model for widget display
 */
data class WidgetGoal(
    val id: String,
    val text: String,
    val imageResId: Int?,
    val customImage: String?,
    val status: GoalStatus,
    val weekOfYear: Int,
    val yearCreated: Int,
    val position: Int
)
