package io.sukhuat.dingo.domain.repository

import android.net.Uri
import java.io.File

/**
 * Repository interface for storage operations
 */
interface StorageRepository {
    /**
     * Upload an image to storage
     * @param imageUri The URI of the image to upload
     * @param goalId The ID of the goal associated with the image
     * @return The URL of the uploaded image
     */
    suspend fun uploadImage(imageUri: Uri, goalId: String? = null): String

    /**
     * Download an image from storage
     * @param imageUrl The URL of the image to download
     * @param file The file to save the image to
     * @return True if the download was successful, false otherwise
     */
    suspend fun downloadImage(imageUrl: String, file: File): Boolean

    /**
     * Delete an image from storage
     * @param imageUrl The URL of the image to delete
     * @return True if the deletion was successful, false otherwise
     */
    suspend fun deleteImage(imageUrl: String): Boolean
}
