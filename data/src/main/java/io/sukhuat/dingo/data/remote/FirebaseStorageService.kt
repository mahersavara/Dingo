package io.sukhuat.dingo.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import io.sukhuat.dingo.data.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseStorageService"

/**
 * Service class for interacting with Firebase Storage for image uploads
 */
@Singleton
class FirebaseStorageService @Inject constructor(
    private val context: Context,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    private val imagesPath = "goal_images"

    /**
     * Get the current user ID or throw an exception if not logged in
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User is not authenticated")
    }

    /**
     * Upload an image to Firebase Storage with progress tracking
     * @param imageUri The URI of the image to upload
     * @param goalId The ID of the goal associated with the image
     * @return The download URL of the uploaded image
     */
    suspend fun uploadImage(imageUri: Uri, goalId: String? = null): String = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()

            // Compress the image before uploading
            val compressedImageFile = ImageUtils.compressImage(context, imageUri)
                ?: throw IOException("Failed to compress image")

            // Get file extension and generate unique filename
            val extension = ImageUtils.getFileExtension(context, imageUri)
            val fileName = ImageUtils.generateUniqueFileName(goalId, extension)
            val imagePath = "$userId/$imagesPath/$fileName"

            // Upload the compressed image with progress monitoring
            val storageRef = storage.reference.child(imagePath)
            val uploadTask = storageRef.putFile(Uri.fromFile(compressedImageFile))

            // Add progress listeners for debugging
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(TAG, "Upload progress: $progress%")
            }

            val taskSnapshot = uploadTask.await()

            // Clean up the temporary file
            compressedImageFile.delete()

            Log.d(TAG, "Image uploaded successfully to path: $imagePath")
            return@withContext taskSnapshot.storage.downloadUrl.await().toString()
        } catch (e: StorageException) {
            // Handle specific Firebase Storage errors
            val errorMessage = when (e.errorCode) {
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "Upload failed: Retry limit exceeded"
                StorageException.ERROR_NOT_AUTHENTICATED -> "Upload failed: User not authenticated"
                StorageException.ERROR_QUOTA_EXCEEDED -> "Upload failed: Storage quota exceeded"
                StorageException.ERROR_BUCKET_NOT_FOUND -> "Upload failed: Storage bucket not found"
                else -> "Upload failed: ${e.message}"
            }
            Log.e(TAG, errorMessage, e)
            throw IOException(errorMessage, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            throw IOException("Failed to upload image: ${e.message}", e)
        }
    }

    /**
     * Download an image from Firebase Storage
     * @param imageUrl The URL of the image to download
     * @param file The local file to save the image to
     * @return True if the download was successful, false otherwise
     */
    suspend fun downloadImage(imageUrl: String, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val reference = storage.getReferenceFromUrl(imageUrl)
            val downloadTask = reference.getFile(file)

            // Add progress listener for debugging
            downloadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(TAG, "Download progress: $progress%")
            }

            downloadTask.await()
            Log.d(TAG, "Image downloaded successfully to: ${file.absolutePath}")
            return@withContext true
        } catch (e: StorageException) {
            // Handle specific Firebase Storage errors
            val errorMessage = when (e.errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> "Download failed: Image not found"
                StorageException.ERROR_NOT_AUTHENTICATED -> "Download failed: User not authenticated"
                else -> "Download failed: ${e.message}"
            }
            Log.e(TAG, errorMessage, e)
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Delete an image from Firebase Storage
     * @param imageUrl The URL of the image to delete
     * @return True if the deletion was successful, false otherwise
     */
    suspend fun deleteImage(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // First check if the URL is a valid Firebase Storage URL
            if (!imageUrl.contains("firebasestorage.googleapis.com")) {
                Log.w(TAG, "Attempted to delete non-Firebase Storage URL: $imageUrl")
                return@withContext false
            }

            val reference = storage.getReferenceFromUrl(imageUrl)
            reference.delete().await()
            Log.d(TAG, "Image deleted successfully: $imageUrl")
            return@withContext true
        } catch (e: StorageException) {
            // Handle specific Firebase Storage errors
            val errorMessage = when (e.errorCode) {
                StorageException.ERROR_OBJECT_NOT_FOUND -> "Delete failed: Image not found"
                StorageException.ERROR_NOT_AUTHENTICATED -> "Delete failed: User not authenticated"
                StorageException.ERROR_NOT_AUTHORIZED -> "Delete failed: Not authorized to delete this image"
                else -> "Delete failed: ${e.message}"
            }
            Log.e(TAG, errorMessage, e)
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: ${e.message}", e)
            return@withContext false
        }
    }
}