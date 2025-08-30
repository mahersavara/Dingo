package io.sukhuat.dingo.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    /**
     * Process profile image URI into multiple optimized sizes
     * @param imageUri Original image URI
     * @return ProcessedProfileImage with all size variants
     */
    suspend fun processProfileImage(imageUri: Uri): ProcessedProfileImage {
        return withContext(Dispatchers.IO) {
            // Load original bitmap
            val originalBitmap = loadBitmapFromUri(imageUri)
                ?: throw IllegalArgumentException("Cannot load image from URI")

            // Auto-rotate based on EXIF data
            val rotatedBitmap = autoRotateImage(originalBitmap, imageUri)

            // Generate multiple sizes with appropriate compression
            val originalBytes = compressBitmap(
                resizeBitmap(rotatedBitmap, 512, 512),
                90,
                Bitmap.CompressFormat.JPEG
            )

            val mediumBytes = compressBitmap(
                resizeBitmap(rotatedBitmap, 256, 256),
                85,
                Bitmap.CompressFormat.JPEG
            )

            val smallBytes = compressBitmap(
                resizeBitmap(rotatedBitmap, 64, 64),
                80,
                Bitmap.CompressFormat.JPEG
            )

            // Clean up original bitmap
            if (originalBitmap != rotatedBitmap) {
                originalBitmap.recycle()
            }
            rotatedBitmap.recycle()

            ProcessedProfileImage(
                originalSize = originalBytes,
                mediumSize = mediumBytes,
                smallSize = smallBytes,
                originalDimensions = Pair(512, 512),
                mediumDimensions = Pair(256, 256),
                smallDimensions = Pair(64, 64)
            )
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
     * Auto-rotate bitmap based on EXIF orientation data
     */
    private suspend fun autoRotateImage(bitmap: Bitmap, uri: Uri): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }

                    if (rotation != 0f) {
                        val matrix = Matrix().apply { postRotate(rotation) }
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
                            // Only recycle if we created a new bitmap
                            bitmap.recycle()
                        }
                        rotatedBitmap
                    } else {
                        bitmap
                    }
                } ?: bitmap
            } catch (e: Exception) {
                // If we can't read EXIF, return original bitmap
                bitmap
            }
        }
    }

    /**
     * Resize bitmap maintaining aspect ratio with smart cropping
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // Calculate scale to fill the target dimensions
        val scaleX = targetWidth.toFloat() / originalWidth
        val scaleY = targetHeight.toFloat() / originalHeight
        val scale = maxOf(scaleX, scaleY)

        // Calculate scaled dimensions
        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()

        // Scale the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // Center crop to exact target dimensions
        val xOffset = (scaledWidth - targetWidth) / 2
        val yOffset = (scaledHeight - targetHeight) / 2

        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            xOffset.coerceAtLeast(0),
            yOffset.coerceAtLeast(0),
            targetWidth.coerceAtMost(scaledWidth),
            targetHeight.coerceAtMost(scaledHeight)
        )

        // Clean up intermediate bitmap if different
        if (scaledBitmap != croppedBitmap) {
            scaledBitmap.recycle()
        }

        return croppedBitmap
    }

    /**
     * Compress bitmap to byte array with specified quality and format
     */
    private fun compressBitmap(
        bitmap: Bitmap,
        quality: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        val bytes = stream.toByteArray()

        // Clean up
        bitmap.recycle()

        return bytes
    }

    /**
     * Get file extension for the given format
     */
    fun getFileExtension(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.JPEG -> "jpg"
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "jpg"
        }
    }

    /**
     * Validate image URI and get basic information
     */
    suspend fun validateImageUri(uri: Uri): ImageValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(stream, null, options)

                    val width = options.outWidth
                    val height = options.outHeight
                    val mimeType = options.outMimeType

                    when {
                        width <= 0 || height <= 0 -> {
                            ImageValidationResult.Invalid("Invalid image dimensions")
                        }
                        width > 4096 || height > 4096 -> {
                            ImageValidationResult.Invalid("Image too large (max 4096x4096)")
                        }
                        !isSupportedMimeType(mimeType) -> {
                            ImageValidationResult.Invalid("Unsupported image format")
                        }
                        else -> {
                            ImageValidationResult.Valid(
                                width = width,
                                height = height,
                                mimeType = mimeType ?: "image/jpeg"
                            )
                        }
                    }
                } ?: ImageValidationResult.Invalid("Cannot open image file")
            } catch (e: Exception) {
                ImageValidationResult.Invalid("Error validating image: ${e.message}")
            }
        }
    }

    /**
     * Check if MIME type is supported
     */
    private fun isSupportedMimeType(mimeType: String?): Boolean {
        return mimeType in listOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
        )
    }
}

/**
 * Result of processing a profile image with multiple sizes
 */
data class ProcessedProfileImage(
    val originalSize: ByteArray, // 512x512, JPEG 90%
    val mediumSize: ByteArray, // 256x256, JPEG 85%
    val smallSize: ByteArray, // 64x64, JPEG 80%
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
    ORIGINAL(512, 512, 90), // High quality for profile pages
    MEDIUM(256, 256, 85), // Standard quality for profile display
    SMALL(64, 64, 80) // Low quality for thumbnails and lists
}
