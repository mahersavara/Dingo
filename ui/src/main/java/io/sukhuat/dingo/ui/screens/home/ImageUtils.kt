package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Compresses and saves an image to the app's private files directory.
 * 
 * @param context The application context
 * @param uri The URI of the original image
 * @return The URI of the compressed image
 */
suspend fun compressAndSaveImage(context: Context, uri: Uri): Uri {
    return withContext(Dispatchers.IO) {
        try {
            // Create a unique filename for the compressed image
            val filename = "img_${UUID.randomUUID()}.jpg"
            val outputFile = File(context.filesDir, filename)
            
            // Open input stream from the original URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
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
            
            // Resize the bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )
            
            // Compress and save the bitmap
            FileOutputStream(outputFile).use { outputStream ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
            }
            
            // Clean up memory
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            // Return the URI of the compressed image
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            // If anything goes wrong, return the original URI
            uri
        }
    }
} 