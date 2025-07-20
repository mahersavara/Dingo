package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utility class for Firebase Storage operations
 */
object FirebaseStorageUtil {
    private const val TAG = "FirebaseStorageUtil"
    private const val IMAGES_FOLDER = "goal_images"

    /**
     * Uploads an image to Firebase Storage with compression
     * * @param context The application context
     * @param uri The URI of the original image
     * @return The download URL of the uploaded image
     */
    suspend fun uploadImage(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting image upload for URI: $uri")

                // First compress the image
                val compressedImageBytes = compressImage(context, uri)

                // Generate a unique filename
                val filename = "img_${UUID.randomUUID()}.jpg"

                // Get reference to Firebase Storage
                val storageRef = Firebase.storage.reference
                    .child(IMAGES_FOLDER)
                    .child(filename)

                Log.d(TAG, "Uploading to Firebase Storage path: $IMAGES_FOLDER/$filename")

                try {
                    // Upload the compressed image
                    val uploadTask = storageRef.putBytes(compressedImageBytes).await()

                    // Get download URL
                    val downloadUrl = storageRef.downloadUrl.await().toString()
                    Log.d(TAG, "Upload successful, download URL: $downloadUrl")

                    // Also save a local copy for offline access
                    saveLocalCopy(context, compressedImageBytes, filename)

                    // Return the download URL
                    downloadUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase upload failed, falling back to local storage", e)
                    // If Firebase upload fails, fall back to local storage and return a local URI
                    val localUri = saveLocalCopyAndGetUri(context, compressedImageBytes, filename)
                    Log.d(TAG, "Falling back to local storage: $localUri")
                    localUri.toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image to Firebase Storage", e)

                // If all else fails, return the original URI
                uri.toString()
            }
        }
    }

    /**
     * Compresses an image from a URI
     * * @param context The application context
     * @param uri The URI of the original image
     * @return ByteArray of the compressed image
     */
    private suspend fun compressImage(context: Context, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                // Open input stream from the original URI
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("Could not open input stream for URI: $uri")

                // Decode the image
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) {
                    throw IllegalArgumentException("Could not decode bitmap from URI: $uri")
                }

                // Calculate new dimensions to maintain aspect ratio
                val maxDimension = 1024f
                val originalWidth = originalBitmap.width
                val originalHeight = originalBitmap.height

                val scale = if (originalWidth > originalHeight) {
                    maxDimension / originalWidth
                } else {
                    maxDimension / originalHeight
                }

                val newWidth = (originalWidth * scale).toInt()
                val newHeight = (originalHeight * scale).toInt()

                Log.d(TAG, "Resizing image from ${originalWidth}x$originalHeight to ${newWidth}x$newHeight")

                // Resize the bitmap
                val resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    newWidth,
                    newHeight,
                    true
                )

                // Compress to JPEG
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

                // Clean up memory
                if (originalBitmap != resizedBitmap) {
                    originalBitmap.recycle()
                }
                resizedBitmap.recycle()

                // Return the compressed bytes
                outputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, "Error compressing image", e)
                throw e
            }
        }
    }

    /**
     * Saves a local copy of the image for offline access
     * * @param context The application context
     * @param imageBytes The compressed image bytes
     * @param filename The filename to save as
     */
    private fun saveLocalCopy(context: Context, imageBytes: ByteArray, filename: String) {
        try {
            // Create the directory if it doesn't exist
            val localDir = File(context.filesDir, IMAGES_FOLDER).apply {
                if (!exists()) mkdirs()
            }

            // Save the file
            val localFile = File(localDir, filename)
            FileOutputStream(localFile).use { outputStream ->
                outputStream.write(imageBytes)
                outputStream.flush()
            }

            Log.d(TAG, "Saved local copy to: ${localFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving local copy", e)
        }
    }

    /**
     * Saves a local copy of the image and returns the URI
     * Used as a fallback when Firebase upload fails
     * * @param context The application context
     * @param imageBytes The compressed image bytes
     * @param filename The filename to save as
     * @return The URI of the local copy
     */
    private fun saveLocalCopyAndGetUri(context: Context, imageBytes: ByteArray, filename: String): Uri {
        try {
            // Create the directory if it doesn't exist
            val localDir = File(context.filesDir, IMAGES_FOLDER).apply {
                if (!exists()) mkdirs()
            }

            // Save the file
            val localFile = File(localDir, filename)
            FileOutputStream(localFile).use { outputStream ->
                outputStream.write(imageBytes)
                outputStream.flush()
            }

            Log.d(TAG, "Saved local copy to: ${localFile.absolutePath}")
            return Uri.fromFile(localFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving local copy", e)
            throw e
        }
    }

    /**
     * Saves a local copy of the image and returns the URI
     * Used as a fallback when Firebase upload fails
     * * @param context The application context
     * @param uri The original image URI
     * @return The URI of the local copy
     */
    private suspend fun saveLocalCopyAndGetUri(context: Context, uri: Uri): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = compressImage(context, uri)
                val filename = "img_${UUID.randomUUID()}.jpg"

                // Create the directory if it doesn't exist
                val localDir = File(context.filesDir, IMAGES_FOLDER).apply {
                    if (!exists()) mkdirs()
                }

                // Save the file
                val localFile = File(localDir, filename)
                FileOutputStream(localFile).use { outputStream ->
                    outputStream.write(imageBytes)
                    outputStream.flush()
                }

                Log.d(TAG, "Saved local copy to: ${localFile.absolutePath}")
                Uri.fromFile(localFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving local copy", e)
                uri // Return original URI as fallback
            }
        }
    }
}
