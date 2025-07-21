package io.sukhuat.dingo.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
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

        // Fix image orientation based on EXIF data
        val correctedBitmap = fixImageOrientation(context, imageUri, originalBitmap)

        // Scale down the corrected image if it's too large
        val maxDimension = 1024
        val scaledBitmap = if (correctedBitmap.width > maxDimension || correctedBitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(correctedBitmap.width, correctedBitmap.height)
            val newWidth = (correctedBitmap.width * scale).toInt()
            val newHeight = (correctedBitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(correctedBitmap, newWidth, newHeight, true)
        } else {
            correctedBitmap
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

        // Clean up intermediate bitmaps
        if (scaledBitmap != correctedBitmap) {
            correctedBitmap.recycle()
        }
        if (correctedBitmap != originalBitmap) {
            originalBitmap.recycle()
        }

        // Return the URI of the saved file
        Uri.fromFile(outputFile)
    } catch (e: IOException) {
        Log.e("ImageUtils", "Error compressing and saving image", e)
        null
    }
}

/**
 * Fix image orientation based on EXIF data
 * @param context Application context
 * @param imageUri URI of the original image
 * @param bitmap The bitmap to correct
 * @return Corrected bitmap with proper orientation
 */
private fun fixImageOrientation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        inputStream?.use { stream ->
            val exif = ExifInterface(stream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f)
                }
                else -> return bitmap // No transformation needed
            }

            return try {
                val correctedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
                correctedBitmap
            } catch (e: OutOfMemoryError) {
                Log.e("ImageUtils", "Out of memory creating rotated bitmap, returning original", e)
                bitmap
            }
        }
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error reading EXIF orientation data", e)
    }

    return bitmap
}
