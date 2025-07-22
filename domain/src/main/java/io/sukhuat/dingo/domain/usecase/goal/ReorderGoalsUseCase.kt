package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for reordering goals within the 12-position grid
 */
class ReorderGoalsUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * Legacy method for backward compatibility
     */
    suspend operator fun invoke(goalIds: List<String>): Result<Boolean> {
        return try {
            val result = goalRepository.reorderGoals(goalIds)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to reorder goals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move a goal from one position to another position in the grid
     * If target position is occupied, swap the goals
     * If target position is empty, just move the goal
     */
    suspend fun moveGoalToPosition(
        goalId: String,
        newPosition: Int
    ): Result<Unit> {
        return try {
            require(newPosition in 0..11) { "Grid position must be between 0 and 11" }

            goalRepository.moveGoalToPosition(goalId, newPosition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Swap positions of two goals
     */
    suspend fun swapGoalPositions(
        goalId1: String,
        goalId2: String
    ): Result<Unit> {
        return try {
            goalRepository.swapGoalPositions(goalId1, goalId2)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
