package io.sukhuat.dingo.domain.model

import java.util.UUID

/**
 * Represents the status of a goal
 */
enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    ARCHIVED
}

/**
 * Domain model representing a user's goal
 */
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val imageResId: Int? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val customImage: String? = null,
    val imageUrl: String? = null, // URL of the image stored in Firebase Storage
    val position: Int = -1 // For ordering goals in the grid
)
