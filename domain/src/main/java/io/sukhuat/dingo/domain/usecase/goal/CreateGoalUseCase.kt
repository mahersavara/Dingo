package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for creating a new goal
 */
class CreateGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        text: String,
        imageResId: Int? = null,
        customImage: String? = null
    ): Result<String> {
        return try {
            val goal = Goal(
                text = text,
                imageResId = imageResId,
                customImage = customImage
            )
            
            val goalId = goalRepository.createGoal(goal)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 