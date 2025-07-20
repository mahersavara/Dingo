package io.sukhuat.dingo.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.model.ProfileError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Manager for optimizing image loading, processing, and caching for profile pictures
 */
@Singleton
class ImageOptimizationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MAX_IMAGE_WIDTH = 512
        private const val MAX_IMAGE_HEIGHT = 512
        private const val JPEG_QUALITY = 85
        private const val MAX_FILE_SIZE_BYTES = 1024 * 1024 // 1MB
    }

    /**
     * Optimize image for profile picture use
     */
    suspend fun optimizeProfileImage(imageUri: Uri): OptimizedImage = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw ProfileError.ImageProcessingError("Cannot open image file")

            // Get image dimensions without loading full bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                throw ProfileError.ImageProcessingError("Invalid image dimensions")
            }

            // Calculate optimal sample size
            val sampleSize = calculateSampleSize(originalWidth, originalHeight, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)

            // Load and decode the bitmap with sample size
            val decodedBitmap = decodeBitmapWithSampleSize(imageUri, sampleSize)
                ?: throw ProfileError.ImageProcessingError("Failed to decode image")

            // Handle image rotation based on EXIF data
            val rotatedBitmap = handleImageRotation(imageUri, decodedBitmap)

            // Resize to exact dimensions if needed
            val resizedBitmap = resizeBitmapIfNeeded(rotatedBitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)

            // Compress to JPEG with quality control
            val compressedBytes = compressBitmap(resizedBitmap, JPEG_QUALITY)

            // If still too large, reduce quality
            val finalBytes = if (compressedBytes.size > MAX_FILE_SIZE_BYTES) {
                reduceImageQuality(resizedBitmap, MAX_FILE_SIZE_BYTES)
            } else {
                compressedBytes
            }

            // Clean up bitmaps
            if (decodedBitmap != rotatedBitmap) decodedBitmap.recycle()
            if (rotatedBitmap != resizedBitmap) rotatedBitmap.recycle()
            resizedBitmap.recycle()

            OptimizedImage(
                data = finalBytes,
                width = min(originalWidth, MAX_IMAGE_WIDTH),
                height = min(originalHeight, MAX_IMAGE_HEIGHT),
                sizeBytes = finalBytes.size.toLong(),
                mimeType = "image/jpeg"
            )
        } catch (e: ProfileError) {
            throw e
        } catch (e: OutOfMemoryError) {
            throw ProfileError.ImageProcessingError("Image too large for device memory")
        } catch (e: Exception) {
            throw ProfileError.ImageProcessingError("Image processing failed: ${e.message}")
        }
    }

    /**
     * Create thumbnail from optimized image
     */
    suspend fun createThumbnail(optimizedImage: OptimizedImage, size: Int = 128): ImageBitmap = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(optimizedImage.data, 0, optimizedImage.data.size)
                ?: throw ProfileError.ImageProcessingError("Failed to decode optimized image")

            val thumbnailBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
            bitmap.recycle()

            val imageBitmap = thumbnailBitmap.asImageBitmap()
            thumbnailBitmap.recycle()

            imageBitmap
        } catch (e: Exception) {
            throw ProfileError.ImageProcessingError("Thumbnail creation failed: ${e.message}")
        }
    }

    /**
     * Calculate optimal sample size for image decoding
     */
    private fun calculateSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
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
     * Decode bitmap with specified sample size
     */
    private fun decodeBitmapWithSampleSize(imageUri: Uri, sampleSize: Int): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Handle image rotation based on EXIF data
     */
    private fun handleImageRotation(imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        } catch (e: Exception) {
            // If rotation fails, return original bitmap
            bitmap
        }
    }

    /**
     * Resize bitmap if it exceeds maximum dimensions
     */
    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait or square
            (maxHeight * aspectRatio).toInt() to maxHeight
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }
        return resizedBitmap
    }

    /**
     * Compress bitmap to JPEG with specified quality
     */
    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Reduce image quality until it fits within size limit
     */
    private fun reduceImageQuality(bitmap: Bitmap, maxSizeBytes: Int): ByteArray {
        var quality = JPEG_QUALITY
        var compressedBytes: ByteArray

        do {
            compressedBytes = compressBitmap(bitmap, quality)
            quality -= 10
        } while (compressedBytes.size > maxSizeBytes && quality > 10)

        return compressedBytes
    }

    /**
     * Get image metadata without loading full image
     */
    suspend fun getImageMetadata(imageUri: Uri): ImageMetadata = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw ProfileError.ImageProcessingError("Cannot open image file")

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            val mimeType = options.outMimeType ?: "unknown"
            val fileSize = try {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    stream.available().toLong()
                } ?: 0L
            } catch (e: Exception) {
                0L
            }

            ImageMetadata(
                width = options.outWidth,
                height = options.outHeight,
                mimeType = mimeType,
                sizeBytes = fileSize
            )
        } catch (e: Exception) {
            throw ProfileError.ImageProcessingError("Failed to read image metadata: ${e.message}")
        }
    }
}

/**
 * Data class representing an optimized image
 */
data class OptimizedImage(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptimizedImage

        if (!data.contentEquals(other.data)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (sizeBytes != other.sizeBytes) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Data class representing image metadata
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val mimeType: String,
    val sizeBytes: Long
)
