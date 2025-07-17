package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.repository.StorageRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case for downloading a goal image
 */
class DownloadGoalImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    /**
     * Download an image for a goal
     * @param imageUrl The URL of the image to download
     * @param destinationFile The file to save the image to
     * @return A Result containing a boolean indicating success or failure
     */
    suspend operator fun invoke(imageUrl: String, destinationFile: File): Result<Boolean> {
        return try {
            val success = storageRepository.downloadImage(imageUrl, destinationFile)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to download image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}