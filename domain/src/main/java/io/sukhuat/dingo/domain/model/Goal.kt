package io.sukhuat.dingo.domain.model

import java.util.Calendar
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
    val position: Int = -1, // For ordering goals in the grid
    val weekOfYear: Int = -1, // Will be calculated in factory function
    val yearCreated: Int = -1 // Will be calculated in factory function
) {
    companion object {
        /**
         * Create a new Goal with proper week/year calculation
         */
        fun create(
            text: String,
            imageResId: Int? = null,
            status: GoalStatus = GoalStatus.ACTIVE,
            customImage: String? = null,
            imageUrl: String? = null,
            position: Int = -1,
            createdAt: Long = System.currentTimeMillis()
        ): Goal {
            val calendar = Calendar.getInstance().apply { timeInMillis = createdAt }
            return Goal(
                text = text,
                imageResId = imageResId,
                status = status,
                createdAt = createdAt,
                customImage = customImage,
                imageUrl = imageUrl,
                position = position,
                weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
                yearCreated = calendar.get(Calendar.YEAR)
            )
        }
    }
}
