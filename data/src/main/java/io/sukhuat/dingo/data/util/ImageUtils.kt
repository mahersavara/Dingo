package io.sukhuat.dingo.data.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Utility class for image-related operations
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    private const val COMPRESSION_QUALITY = 85
    
    /**
     * Compress an image from a URI to a temporary file
     * @param context Application context
     * @param imageUri URI of the image to compress
     * @return File containing the compressed image, or null if compression failed
     */
    fun compressImage(context: Context, imageUri: Uri): File? {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var tempFile: File? = null
        
        try {
            val contentResolver: ContentResolver = context.contentResolver
            inputStream = contentResolver.openInputStream(imageUri)
            
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for image URI: $imageUri")
                return null
            }
            
            // Decode the image
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false
            
            // Reopen the input stream
            inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to reopen input stream for image URI: $imageUri")
                return null
            }
            
            // Decode with inSampleSize set
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $imageUri")
                return null
            }
            
            // Create a temporary file to store the compressed image
            tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            outputStream = FileOutputStream(tempFile)
            
            // Compress the image
            val baos = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos)
            
            // If the image is still too large, reduce quality until it's under MAX_IMAGE_SIZE
            var quality = COMPRESSION_QUALITY
            while (baos.toByteArray().size > MAX_IMAGE_SIZE && quality > 10) {
                baos.reset()
                quality -= 10
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }
            
            // Write the compressed image to the file
            outputStream.write(baos.toByteArray())
            outputStream.flush()
            
            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            tempFile?.delete() // Clean up the temp file if there was an error
            return null
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing streams: ${e.message}", e)
            }
        }
    }
    
    /**
     * Calculate the optimal inSampleSize value based on required dimensions
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Get a file extension from a URI
     * @param context Application context
     * @param uri URI to get extension from
     * @return File extension (without the dot) or "jpg" as default
     */
    fun getFileExtension(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        
        return when {
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
            mimeType?.contains("png") == true -> "png"
            else -> {
                val path = uri.path
                val extension = path?.substringAfterLast('.', "")
                if (extension.isNullOrEmpty()) "jpg" else extension
            }
        }
    }
    
    /**
     * Generate a unique file name for an image
     * @param goalId Optional goal ID to include in the file name
     * @param extension File extension
     * @return Unique file name
     */
    fun generateUniqueFileName(goalId: String? = null, extension: String): String {
        val timestamp = System.currentTimeMillis()
        return if (goalId != null) {
            "goal_${goalId}_${timestamp}.$extension"
        } else {
            "image_${timestamp}.$extension"
        }
    }
} 