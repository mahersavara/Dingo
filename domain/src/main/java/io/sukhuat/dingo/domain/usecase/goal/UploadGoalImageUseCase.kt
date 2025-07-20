package io.sukhuat.dingo.domain.usecase.goal

import android.net.Uri
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.repository.StorageRepository
import javax.inject.Inject

/**
 * Use case for uploading goal images
 */
class UploadGoalImageUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val storageRepository: StorageRepository
) {
    /**
     * Upload an image for a goal
     * @param goalId The ID of the goal
     * @param imageUri The URI of the image to upload
     * @return Result containing the URL of the uploaded image
     */
    suspend operator fun invoke(goalId: String, imageUri: Uri): Result<String> {
        return try {
            // Upload the image to storage
            val imageUrl = storageRepository.uploadImage(imageUri, goalId)

            // Update the goal with the image URL
            val updateResult = goalRepository.updateGoalImageUrl(goalId, imageUrl)

            if (updateResult) {
                Result.success(imageUrl)
            } else {
                // Clean up the uploaded image if goal update fails
                storageRepository.deleteImage(imageUrl)
                Result.failure(Exception("Failed to update goal with image URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
