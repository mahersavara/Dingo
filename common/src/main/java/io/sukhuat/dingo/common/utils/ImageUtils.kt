package io.sukhuat.dingo.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Gets a safe Uri from a string representation.
 * Handles both local file URIs and Firebase Storage URLs.
 */
fun getSafeImageUri(context: Context, uriString: String?): Uri? {
    if (uriString.isNullOrEmpty()) {
        return null
    }
    
    return try {
        // Check if it's a Firebase Storage URL
        if (uriString.startsWith("https://firebasestorage.googleapis.com")) {
            // For Firebase Storage URLs, we can use them directly
            Log.d("ImageUtils", "Firebase Storage URL detected: $uriString")
            Uri.parse(uriString)
        } else {
            // For local URIs, ensure they're valid
            val uri = uriString.toUri()
            
            // Additional validation for local files
            if (uriString.startsWith("file://")) {
                val file = File(uri.path ?: "")
                if (!file.exists()) {
                    Log.w("ImageUtils", "Local file doesn't exist: $uriString")
                    null
                } else {
                    uri
                }
            } else {
                uri
            }
        }
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error parsing URI: $uriString", e)
        null
    }
}

/**
 * Compresses an image from the given URI and saves it to local storage.
 * Returns the URI of the compressed image.
 */
fun compressAndSaveImage(context: Context, imageUri: Uri, quality: Int = 80): Uri? {
    return try {
        // Create a unique filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "IMG_${timestamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
        
        // Get the input stream from the URI
        val inputStream = context.contentResolver.openInputStream(imageUri)
        
        // Decode the bitmap
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (originalBitmap == null) {
            Log.e("ImageUtils", "Failed to decode bitmap from URI: $imageUri")
            return null
        }
        
        // Scale down the image if it's too large
        val maxDimension = 1024
        val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
            val newWidth = (originalBitmap.width * scale).toInt()
            val newHeight = (originalBitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }
        
        // Create output file in app's private directory
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        val outputFile = File(imagesDir, filename)
        val outputStream = FileOutputStream(outputFile)
        
        // Compress and save
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.flush()
        outputStream.close()
        
        // If we created a new scaled bitmap, recycle the original
        if (scaledBitmap != originalBitmap) {
            originalBitmap.recycle()
        }
        
        // Return the URI of the saved file
        Uri.fromFile(outputFile)
    } catch (e: IOException) {
        Log.e("ImageUtils", "Error compressing and saving image", e)
        null
    }
} 