package io.sukhuat.dingo.common.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Uploads an image to Firebase Storage and returns the download URL.
 * Compresses the image before uploading if necessary.
 * * @param context The application context
 * @param imageUri The URI of the image to upload
 * @param shouldCompress Whether to compress the image before uploading
 * @return The download URL of the uploaded image, or null if upload failed
 */
suspend fun uploadImageToFirebase(
    context: Context,
    imageUri: Uri,
    shouldCompress: Boolean = true
): Uri? = withContext(Dispatchers.IO) {
    try {
        Log.d("ImageUploadUtils", "Starting image upload to Firebase: $imageUri")

        // Compress image if needed
        val finalUri = if (shouldCompress) {
            compressAndSaveImage(context, imageUri) ?: imageUri
        } else {
            imageUri
        }

        // Get current user ID for proper storage structure
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("ImageUploadUtils", "User not authenticated, cannot upload image")
            return@withContext null
        }
        
        val userId = currentUser.uid

        // Get reference to Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        // Create a unique filename and path following security rules
        val filename = "image_${UUID.randomUUID()}.jpg"
        val imagePath = "users/$userId/goals/images/$filename"
        val imageRef = storageRef.child(imagePath)

        // Upload file
        val uploadTask = imageRef.putFile(finalUri)

        // Wait for upload to complete
        uploadTask.await()

        // Get download URL
        val downloadUrl = imageRef.downloadUrl.await()

        Log.d("ImageUploadUtils", "Image successfully uploaded to Firebase path: $imagePath - URL: $downloadUrl")
        return@withContext downloadUrl
    } catch (e: Exception) {
        Log.e("ImageUploadUtils", "Error uploading image to Firebase", e)

        // Save a local copy as fallback
        try {
            val localUri = compressAndSaveImage(context, imageUri)
            Log.d("ImageUploadUtils", "Saved local fallback copy: $localUri")
            return@withContext localUri
        } catch (e2: Exception) {
            Log.e("ImageUploadUtils", "Error saving local fallback copy", e2)
            return@withContext null
        }
    }
}
