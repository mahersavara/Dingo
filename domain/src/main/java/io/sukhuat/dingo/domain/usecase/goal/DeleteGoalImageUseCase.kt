package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.repository.StorageRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Use case for deleting a goal image
 */
class DeleteGoalImageUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val storageRepository: StorageRepository
) {
    /**
     * Delete an image for a goal and update the goal entity
     * @param goalId The ID of the goal
     * @param imageUrl The URL of the image to delete
     * @return A Result containing a boolean indicating success or failure
     */
    suspend operator fun invoke(goalId: String, imageUrl: String): Result<Boolean> {
        return try {
            // First delete the image from storage
            val deleteSuccess = storageRepository.deleteImage(imageUrl)
            
            if (deleteSuccess) {
                // If deletion was successful, update the goal to remove the image URL
                val goal = goalRepository.getGoalById(goalId).firstOrNull()
                
                if (goal != null) {
                    // Update the goal with a null imageUrl
                    val updatedGoal = goal.copy(imageUrl = null)
                    val updateSuccess = goalRepository.updateGoal(updatedGoal)
                    
                    if (updateSuccess) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Failed to update goal after image deletion"))
                    }
                } else {
                    Result.failure(Exception("Goal not found"))
                }
            } else {
                Result.failure(Exception("Failed to delete image from storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 