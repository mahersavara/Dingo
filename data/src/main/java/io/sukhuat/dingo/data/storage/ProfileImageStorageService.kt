package io.sukhuat.dingo.data.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import io.sukhuat.dingo.data.image.ImageProcessor
import io.sukhuat.dingo.data.image.ImageSize
import io.sukhuat.dingo.data.image.ImageValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage service for profile image management with multi-size support
 * Handles upload, download, and deletion of profile images in multiple resolutions
 */
@Singleton
class ProfileImageStorageService @Inject constructor(
    private val storage: FirebaseStorage,
    private val imageProcessor: ImageProcessor
) {

    companion object {
        private const val PROFILE_IMAGES_PATH = "users"
        private const val PROFILE_FOLDER = "profile"

        // File naming convention
        private const val ORIGINAL_FILE = "avatar_original.jpg"
        private const val MEDIUM_FILE = "avatar_medium.jpg"
        private const val SMALL_FILE = "avatar_small.jpg"
    }

    /**
     * Upload profile image with automatic processing and multi-size generation
     * @param userId User ID for folder organization
     * @param imageUri Original image URI
     * @return ProfileImageUploadResult with URLs for all sizes
     */
    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri
    ): ProfileImageUploadResult {
        println("ProfileImageStorageService: uploadProfileImage called - userId=$userId, imageUri=$imageUri")
        return withContext(Dispatchers.IO) {
            try {
                // Validate image first
                println("ProfileImageStorageService: Validating image URI")
                val validation = imageProcessor.validateImageUri(imageUri)
                if (validation is ImageValidationResult.Invalid) {
                    println("ProfileImageStorageService: Image validation failed: ${validation.reason}")
                    return@withContext ProfileImageUploadResult.Error(
                        "Invalid image: ${validation.reason}"
                    )
                }
                println("ProfileImageStorageService: Image validation passed")

                // Process image into multiple sizes
                println("ProfileImageStorageService: Processing image into multiple sizes")
                val processedImage = imageProcessor.processProfileImage(imageUri)
                println("ProfileImageStorageService: Image processing completed")

                // Upload all sizes in parallel
                println("ProfileImageStorageService: Starting parallel uploads for all sizes")
                val uploadResults = coroutineScope {
                    listOf(
                        async {
                            println("ProfileImageStorageService: Uploading original size")
                            uploadImageVariant(
                                userId,
                                ORIGINAL_FILE,
                                processedImage.originalSize,
                                "image/jpeg"
                            )
                        },
                        async {
                            println("ProfileImageStorageService: Uploading medium size")
                            uploadImageVariant(
                                userId,
                                MEDIUM_FILE,
                                processedImage.mediumSize,
                                "image/jpeg"
                            )
                        },
                        async {
                            println("ProfileImageStorageService: Uploading small size")
                            uploadImageVariant(
                                userId,
                                SMALL_FILE,
                                processedImage.smallSize,
                                "image/jpeg"
                            )
                        }
                    ).awaitAll()
                }
                println("ProfileImageStorageService: All upload tasks completed")

                // Check if all uploads succeeded
                val failedUploads = uploadResults.filterIsInstance<UploadResult.Error>()
                if (failedUploads.isNotEmpty()) {
                    println("ProfileImageStorageService: Some uploads failed: ${failedUploads.map { "${it.fileName}: ${it.error}" }}")
                    // Clean up any successful uploads
                    val successfulUploads = uploadResults.filterIsInstance<UploadResult.Success>()
                    println("ProfileImageStorageService: Cleaning up ${successfulUploads.size} successful uploads due to failures")
                    cleanupPartialUploads(userId, successfulUploads.map { it.fileName })

                    return@withContext ProfileImageUploadResult.Error(
                        "Upload failed: ${failedUploads.first().error}"
                    )
                }

                // Extract URLs from successful uploads
                val successfulUploads = uploadResults.filterIsInstance<UploadResult.Success>()
                println("ProfileImageStorageService: All uploads successful, extracting URLs")

                val originalUrl = successfulUploads.find { it.fileName == ORIGINAL_FILE }?.downloadUrl
                val mediumUrl = successfulUploads.find { it.fileName == MEDIUM_FILE }?.downloadUrl
                val smallUrl = successfulUploads.find { it.fileName == SMALL_FILE }?.downloadUrl

                println("ProfileImageStorageService: URLs extracted - original=$originalUrl, medium=$mediumUrl, small=$smallUrl")

                if (originalUrl != null && mediumUrl != null && smallUrl != null) {
                    val result = ProfileImageUploadResult.Success(
                        originalImageUrl = originalUrl,
                        mediumImageUrl = mediumUrl,
                        smallImageUrl = smallUrl,
                        uploadTimestamp = System.currentTimeMillis()
                    )
                    println("ProfileImageStorageService: Upload completed successfully")
                    result
                } else {
                    println("ProfileImageStorageService: ERROR - Failed to retrieve some download URLs")
                    ProfileImageUploadResult.Error("Failed to retrieve download URLs")
                }
            } catch (e: Exception) {
                println("ProfileImageStorageService: ERROR during upload: ${e.message}")
                e.printStackTrace()
                ProfileImageUploadResult.Error("Upload failed: ${e.message}")
            }
        }
    }

    /**
     * Delete all profile image variants for a user
     * @param userId User ID
     */
    suspend fun deleteProfileImages(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val deleteResults = coroutineScope {
                    listOf(
                        async { deleteImageVariant(userId, ORIGINAL_FILE) },
                        async { deleteImageVariant(userId, MEDIUM_FILE) },
                        async { deleteImageVariant(userId, SMALL_FILE) }
                    ).awaitAll()
                }

                // Return true if all deletions succeeded or files didn't exist
                deleteResults.all { it }
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Get download URLs for existing profile images
     * @param userId User ID
     * @return ProfileImageUrls with all available sizes
     */
    suspend fun getProfileImageUrls(userId: String): ProfileImageUrls? {
        return withContext(Dispatchers.IO) {
            try {
                val urlResults = coroutineScope {
                    listOf(
                        async { getImageUrl(userId, ORIGINAL_FILE) },
                        async { getImageUrl(userId, MEDIUM_FILE) },
                        async { getImageUrl(userId, SMALL_FILE) }
                    ).awaitAll()
                }

                val originalUrl = urlResults[0]
                val mediumUrl = urlResults[1]
                val smallUrl = urlResults[2]

                if (originalUrl != null || mediumUrl != null || smallUrl != null) {
                    ProfileImageUrls(
                        originalImageUrl = originalUrl,
                        mediumImageUrl = mediumUrl,
                        smallImageUrl = smallUrl
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Upload a single image variant
     */
    private suspend fun uploadImageVariant(
        userId: String,
        fileName: String,
        imageData: ByteArray,
        contentType: String
    ): UploadResult {
        println("ProfileImageStorageService: uploadImageVariant - userId=$userId, fileName=$fileName, dataSize=${imageData.size} bytes, contentType=$contentType")
        return try {
            val storageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(userId)
                .child(PROFILE_FOLDER)
                .child(fileName)

            println("ProfileImageStorageService: Storage reference created: ${storageRef.path}")

            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata("userId", userId)
                .setCustomMetadata("uploadTimestamp", System.currentTimeMillis().toString())
                .build()

            println("ProfileImageStorageService: Starting Firebase Storage upload")
            val uploadTask = storageRef.putBytes(imageData, metadata).await()
            println("ProfileImageStorageService: Upload task completed, getting download URL")

            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            println("ProfileImageStorageService: Download URL obtained: $downloadUrl")

            UploadResult.Success(fileName, downloadUrl)
        } catch (e: Exception) {
            println("ProfileImageStorageService: ERROR uploading $fileName: ${e.message}")
            e.printStackTrace()
            UploadResult.Error(fileName, e.message ?: "Upload failed")
        }
    }

    /**
     * Delete a single image variant
     */
    private suspend fun deleteImageVariant(userId: String, fileName: String): Boolean {
        return try {
            val storageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(userId)
                .child(PROFILE_FOLDER)
                .child(fileName)

            storageRef.delete().await()
            true
        } catch (e: Exception) {
            // File might not exist, which is okay for deletion
            true
        }
    }

    /**
     * Get download URL for a single image variant
     */
    private suspend fun getImageUrl(userId: String, fileName: String): String? {
        return try {
            val storageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(userId)
                .child(PROFILE_FOLDER)
                .child(fileName)

            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clean up partially uploaded files in case of failure
     */
    private suspend fun cleanupPartialUploads(userId: String, uploadedFiles: List<String>) {
        uploadedFiles.forEach { fileName ->
            try {
                deleteImageVariant(userId, fileName)
            } catch (e: Exception) {
                // Ignore cleanup failures
            }
        }
    }

    /**
     * Get the appropriate image URL for a given size preference
     * Falls back to available sizes if preferred size is not available
     */
    fun getImageUrlForSize(urls: ProfileImageUrls, preferredSize: ImageSize): String? {
        return when (preferredSize) {
            ImageSize.ORIGINAL -> urls.originalImageUrl ?: urls.mediumImageUrl ?: urls.smallImageUrl
            ImageSize.MEDIUM -> urls.mediumImageUrl ?: urls.originalImageUrl ?: urls.smallImageUrl
            ImageSize.SMALL -> urls.smallImageUrl ?: urls.mediumImageUrl ?: urls.originalImageUrl
        }
    }
}

/**
 * Result of profile image upload operation
 */
sealed class ProfileImageUploadResult {
    data class Success(
        val originalImageUrl: String,
        val mediumImageUrl: String,
        val smallImageUrl: String,
        val uploadTimestamp: Long
    ) : ProfileImageUploadResult()

    data class Error(
        val message: String
    ) : ProfileImageUploadResult()
}

/**
 * Container for profile image URLs in different sizes
 */
data class ProfileImageUrls(
    val originalImageUrl: String? = null, // 512x512
    val mediumImageUrl: String? = null, // 256x256
    val smallImageUrl: String? = null // 64x64
)

/**
 * Result of individual image upload
 */
private sealed class UploadResult {
    data class Success(val fileName: String, val downloadUrl: String) : UploadResult()
    data class Error(val fileName: String, val error: String) : UploadResult()
}
