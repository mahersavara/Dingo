package io.sukhuat.dingo.data.model

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * Data Transfer Object for Firebase to store goals
 * Previously was a Room entity, now adapted for Firebase
 */
data class GoalEntity(
    val id: String,
    val text: String,
    val imageResId: Int?,
    val status: String,
    val createdAt: Long,
    val customImage: String?,
    val imageUrl: String?,
    val position: Int
) {
    /**
     * Convert the entity to a domain model
     */
    fun toDomainModel(): Goal {
        return Goal(
            id = id,
            text = text,
            imageResId = imageResId,
            status = GoalStatus.valueOf(status),
            createdAt = createdAt,
            customImage = customImage,
            imageUrl = imageUrl,
            position = position
        )
    }

    companion object {
        /**
         * Convert a domain model to an entity
         */
        fun fromDomainModel(goal: Goal): GoalEntity {
            return GoalEntity(
                id = goal.id,
                text = goal.text,
                imageResId = goal.imageResId,
                status = goal.status.name,
                createdAt = goal.createdAt,
                customImage = goal.customImage,
                imageUrl = goal.imageUrl,
                position = goal.position
            )
        }
    }
}