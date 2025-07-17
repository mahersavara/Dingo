package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for updating a goal's properties
 */
class UpdateGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal): Result<Boolean> {
        return try {
            val result = goalRepository.updateGoal(goal)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateText(goalId: String, text: String): Result<Boolean> {
        return try {
            val result = goalRepository.updateGoalText(goalId, text)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update goal text"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateImage(goalId: String, customImage: String?): Result<Boolean> {
        return try {
            val result = goalRepository.updateGoalImage(goalId, customImage)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update goal image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}