package io.sukhuat.dingo.domain.repository

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing goals
 */
interface GoalRepository {
    /**
     * Get all goals as a Flow
     */
    fun getAllGoals(): Flow<List<Goal>>

    /**
     * Get goals filtered by status
     */
    fun getGoalsByStatus(status: GoalStatus): Flow<List<Goal>>

    /**
     * Get a specific goal by ID
     */
    fun getGoalById(id: String): Flow<Goal?>

    /**
     * Create a new goal
     */
    suspend fun createGoal(goal: Goal): String

    /**
     * Update an existing goal
     */
    suspend fun updateGoal(goal: Goal): Boolean

    /**
     * Update the status of a goal
     */
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus): Boolean

    /**
     * Update the text of a goal
     */
    suspend fun updateGoalText(goalId: String, text: String): Boolean

    /**
     * Update the custom image of a goal
     */
    suspend fun updateGoalImage(goalId: String, customImage: String?): Boolean

    /**
     * Update the image URL of a goal
     */
    suspend fun updateGoalImageUrl(goalId: String, imageUrl: String?): Boolean

    /**
     * Delete a goal
     */
    suspend fun deleteGoal(goalId: String): Boolean

    /**
     * Reorder goals
     */
    suspend fun reorderGoals(goalIds: List<String>): Boolean

    /**
     * Clear all goals from the local database
     * This should be called when a user logs out or switches accounts
     */
    suspend fun clearAllGoals()
}
