package io.sukhuat.dingo.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Image processor for profile picture optimization and multi-size generation
 * Handles auto-rotation, compression, and size variants for optimal performance
 */
@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun processProfileImage(imageUri: Uri): ProcessedProfileImage {
        println("ImageProcessor: processProfileImage called with URI: $imageUri")
        return withContext(Dispatchers.IO) {
            try {
                println("ImageProcessor: Loading original bitmap from URI")
                // Load original bitmap
                val originalBitmap = loadBitmapFromUri(imageUri)
                    ?: throw IllegalArgumentException("Cannot load image from URI")

                println("ImageProcessor: Original bitmap loaded - ${originalBitmap.width}x${originalBitmap.height}")

                // Auto-rotate based on EXIF data
                println("ImageProcessor: Auto-rotating image based on EXIF data")
                val rotatedBitmap = autoRotateImage(originalBitmap, imageUri)
                println("ImageProcessor: Image rotation completed")

                // Generate multiple sizes with aggressive compression for small file sizes
                println("ImageProcessor: Generating original size (256x256) - targeting ~15KB")
                val originalBytes = compressBitmapToTargetSize(
                    resizeBitmap(rotatedBitmap, 256, 256), // Reduced from 512 to 256
                    targetSizeKB = 15
                )
                println("ImageProcessor: Original size generated - ${originalBytes.size} bytes")

                println("ImageProcessor: Generating medium size (128x128) - targeting ~8KB")
                val mediumBytes = compressBitmapToTargetSize(
                    resizeBitmap(rotatedBitmap, 128, 128), // Reduced from 256 to 128
                    targetSizeKB = 8
                )
                println("ImageProcessor: Medium size generated - ${mediumBytes.size} bytes")

                println("ImageProcessor: Generating small size (64x64) - targeting ~3KB")
                val smallBytes = compressBitmapToTargetSize(
                    resizeBitmap(rotatedBitmap, 64, 64), // Keep same size
                    targetSizeKB = 3
                )
                println("ImageProcessor: Small size generated - ${smallBytes.size} bytes")

                // Clean up original bitmap
                if (originalBitmap != rotatedBitmap) {
                    originalBitmap.recycle()
                }
                rotatedBitmap.recycle()
                println("ImageProcessor: Bitmap cleanup completed")

                val result = ProcessedProfileImage(
                    originalSize = originalBytes,
                    mediumSize = mediumBytes,
                    smallSize = smallBytes,
                    originalDimensions = Pair(256, 256), // Updated dimensions
                    mediumDimensions = Pair(128, 128), // Updated dimensions
                    smallDimensions = Pair(64, 64)
                )
                println("ImageProcessor: Image processing completed successfully")
                result
            } catch (e: Exception) {
                println("ImageProcessor: ERROR during image processing: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    /**
     * Load bitmap from URI with proper error handling
     */
    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Resize bitmap while maintaining aspect ratio and handling edge cases
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // Calculate the scale factor while maintaining aspect ratio
        val scaleX = targetWidth.toFloat() / originalWidth
        val scaleY = targetHeight.toFloat() / originalHeight
        val scale = minOf(scaleX, scaleY)

        // Calculate new dimensions
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        // Create scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Create final bitmap with exact target size (center crop if needed)
        val finalBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)

        // Calculate position for centering
        val left = (targetWidth - newWidth) / 2f
        val top = (targetHeight - newHeight) / 2f

        // Draw the scaled bitmap centered
        canvas.drawBitmap(scaledBitmap, left, top, null)

        // Clean up intermediate bitmap
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        return finalBitmap
    }

    /**
     * Auto-rotate image based on EXIF orientation data
     */
    private fun autoRotateImage(bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.use { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    /**
     * Compress bitmap to target file size using iterative quality adjustment
     * @param bitmap Bitmap to compress
     * @param targetSizeKB Target file size in kilobytes
     * @return Compressed byte array within target size
     */
    private fun compressBitmapToTargetSize(
        bitmap: Bitmap,
        targetSizeKB: Int
    ): ByteArray {
        val targetSizeBytes = targetSizeKB * 1024
        var quality = 90
        var compressedBytes: ByteArray

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            compressedBytes = stream.toByteArray()

            println("ImageProcessor: Compression quality $quality -> ${compressedBytes.size} bytes (target: $targetSizeBytes)")

            if (compressedBytes.size > targetSizeBytes && quality > 10) {
                quality -= 10
            } else {
                break
            }
        } while (compressedBytes.size > targetSizeBytes && quality > 10)

        // Clean up bitmap
        bitmap.recycle()

        println("ImageProcessor: Final compression: quality $quality, size ${compressedBytes.size} bytes")
        return compressedBytes
    }

    /**
     * Validate image URI and get basic information
     */
    suspend fun validateImageUri(uri: Uri): ImageValidationResult {
        println("ImageProcessor: validateImageUri called with URI: $uri")
        return withContext(Dispatchers.IO) {
            try {
                println("ImageProcessor: Opening input stream for URI validation")
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    println("ImageProcessor: Input stream opened successfully, decoding bounds")
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, options)

                    val width = options.outWidth
                    val height = options.outHeight
                    val mimeType = options.outMimeType

                    println("ImageProcessor: Image decoded - width=$width, height=$height, mimeType=$mimeType")

                    when {
                        width <= 0 || height <= 0 -> {
                            println("ImageProcessor: Validation failed - Invalid dimensions")
                            ImageValidationResult.Invalid("Invalid image dimensions")
                        }
                        width > 4096 || height > 4096 -> {
                            println("ImageProcessor: Validation failed - Image too large")
                            ImageValidationResult.Invalid("Image too large (max 4096x4096)")
                        }
                        !isSupportedMimeType(mimeType) -> {
                            println("ImageProcessor: Validation failed - Unsupported format: $mimeType")
                            ImageValidationResult.Invalid("Unsupported image format")
                        }
                        else -> {
                            println("ImageProcessor: Validation successful - Valid image")
                            ImageValidationResult.Valid(
                                width = width,
                                height = height,
                                mimeType = mimeType ?: "image/jpeg"
                            )
                        }
                    }
                } ?: run {
                    println("ImageProcessor: ERROR - Cannot open input stream for URI")
                    ImageValidationResult.Invalid("Cannot open image file")
                }
            } catch (e: Exception) {
                println("ImageProcessor: ERROR during validation: ${e.message}")
                e.printStackTrace()
                ImageValidationResult.Invalid("Error validating image: ${e.message}")
            }
        }
    }

    /**
     * Check if the MIME type is supported for image processing
     */
    private fun isSupportedMimeType(mimeType: String?): Boolean {
        return mimeType in listOf(
            "image/jpeg",
            "image/jpg", "image/png",
            "image/webp"
        )
    }
}

/**
 * Result of processing a profile image with multiple sizes
 */
data class ProcessedProfileImage(
    val originalSize: ByteArray, // 256x256, ~15KB
    val mediumSize: ByteArray, // 128x128, ~8KB
    val smallSize: ByteArray, // 64x64, ~3KB
    val originalDimensions: Pair<Int, Int>,
    val mediumDimensions: Pair<Int, Int>,
    val smallDimensions: Pair<Int, Int>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedProfileImage

        if (!originalSize.contentEquals(other.originalSize)) return false
        if (!mediumSize.contentEquals(other.mediumSize)) return false
        if (!smallSize.contentEquals(other.smallSize)) return false
        if (originalDimensions != other.originalDimensions) return false
        if (mediumDimensions != other.mediumDimensions) return false
        if (smallDimensions != other.smallDimensions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalSize.contentHashCode()
        result = 31 * result + mediumSize.contentHashCode()
        result = 31 * result + smallSize.contentHashCode()
        result = 31 * result + originalDimensions.hashCode()
        result = 31 * result + mediumDimensions.hashCode()
        result = 31 * result + smallDimensions.hashCode()
        return result
    }
}

/**
 * Result of image validation
 */
sealed class ImageValidationResult {
    data class Valid(
        val width: Int,
        val height: Int,
        val mimeType: String
    ) : ImageValidationResult()

    data class Invalid(
        val reason: String
    ) : ImageValidationResult()
}

/**
 * Image size variants for different use cases
 */
enum class ImageSize(val width: Int, val height: Int, val quality: Int) {
    ORIGINAL(256, 256, 90), // Updated to match new compression
    MEDIUM(128, 128, 85), // Updated to match new compression
    SMALL(64, 64, 80) // Thumbnails and lists
}
