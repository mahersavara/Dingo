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
        customImage: String? = null,
        position: Int = -1
    ): Result<String> {
        return try {
            val goal = Goal.create(
                text = text,
                imageResId = imageResId,
                customImage = customImage,
                position = position
            )

            val goalId = goalRepository.createGoal(goal)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
