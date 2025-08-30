package io.sukhuat.dingo.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.sukhuat.dingo.data.image.ImageSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Profile image cache manager with memory and disk caching
 * Provides efficient caching for profile images with automatic cleanup
 */
@Singleton
class ProfileImageCacheManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val CACHE_DIR_NAME = "profile_images"
        private const val MAX_CACHE_SIZE_MB = 50L
        private const val MAX_MEMORY_CACHE_SIZE = 10 * 1024 * 1024 // 10MB
    }

    // Memory cache for bitmaps
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }

        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: Bitmap?, newValue: Bitmap?) {
            oldValue?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }
    }

    // Disk cache directory
    private val cacheDir by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Get cached profile image bitmap
     * @param userId User ID
     * @param imageUrl Image URL to cache
     * @param size Preferred image size
     * @return Cached bitmap or null if not available
     */
    suspend fun getCachedImage(
        userId: String,
        imageUrl: String,
        size: ImageSize = ImageSize.MEDIUM
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            val cacheKey = generateCacheKey(userId, size)

            // Check memory cache first
            memoryCache.get(cacheKey)?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    return@withContext bitmap
                } else {
                    // Remove recycled bitmap from cache
                    memoryCache.remove(cacheKey)
                }
            }

            // Check disk cache
            val cachedFile = getCachedImageFile(userId, size)
            if (cachedFile.exists() && cachedFile.isFile) {
                try {
                    val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                    if (bitmap != null && !bitmap.isRecycled) {
                        // Add to memory cache for future use
                        memoryCache.put(cacheKey, bitmap)
                        return@withContext bitmap
                    }
                } catch (e: Exception) {
                    // File might be corrupted, delete it
                    cachedFile.delete()
                }
            }

            // Not in cache, download and cache
            downloadAndCache(userId, imageUrl, size)
        }
    }

    /**
     * Download image from URL and cache it
     * @param userId User ID
     * @param imageUrl Image URL to download
     * @param size Image size to cache
     * @return Downloaded bitmap or null if failed
     */
    private suspend fun downloadAndCache(
        userId: String,
        imageUrl: String,
        size: ImageSize
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Download image data
                val imageData = downloadImageData(imageUrl)
                    ?: return@withContext null

                // Decode bitmap
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    ?: return@withContext null

                if (bitmap.isRecycled) {
                    return@withContext null
                }

                // Cache to disk
                val cachedFile = getCachedImageFile(userId, size)
                try {
                    FileOutputStream(cachedFile).use { fos ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            size.quality,
                            fos
                        )
                    }
                } catch (e: Exception) {
                    // Failed to save to disk, but we still have the bitmap
                }

                // Add to memory cache
                val cacheKey = generateCacheKey(userId, size)
                memoryCache.put(cacheKey, bitmap)

                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Download image data from URL
     */
    private suspend fun downloadImageData(imageUrl: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                URL(imageUrl).readBytes()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Preload profile image into cache
     * @param userId User ID
     * @param imageUrl Image URL to preload
     * @param sizes Sizes to preload (default: all sizes)
     */
    suspend fun preloadProfileImage(
        userId: String,
        imageUrl: String,
        sizes: List<ImageSize> = ImageSize.values().toList()
    ) {
        withContext(Dispatchers.IO) {
            sizes.forEach { size ->
                // This will download and cache if not already cached
                getCachedImage(userId, imageUrl, size)
            }
        }
    }

    /**
     * Clear cached images for a specific user
     * @param userId User ID
     */
    suspend fun clearUserCache(userId: String) {
        withContext(Dispatchers.IO) {
            ImageSize.values().forEach { size ->
                val cacheKey = generateCacheKey(userId, size)

                // Remove from memory cache
                memoryCache.remove(cacheKey)?.let { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }

                // Remove from disk cache
                val cachedFile = getCachedImageFile(userId, size)
                if (cachedFile.exists()) {
                    cachedFile.delete()
                }
            }
        }
    }

    /**
     * Clear all cached images
     */
    suspend fun clearAllCache() {
        withContext(Dispatchers.IO) {
            // Clear memory cache
            memoryCache.evictAll()

            // Clear disk cache
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        }
    }

    /**
     * Get cache size information
     * @return CacheInfo with memory and disk usage
     */
    suspend fun getCacheInfo(): CacheInfo {
        return withContext(Dispatchers.IO) {
            val memorySizeBytes = memoryCache.size()

            val diskSizeBytes = if (cacheDir.exists()) {
                cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
            } else {
                0L
            }

            CacheInfo(
                memorySizeBytes = memorySizeBytes.toLong(),
                diskSizeBytes = diskSizeBytes,
                totalFilesCount = cacheDir.listFiles()?.size ?: 0
            )
        }
    }

    /**
     * Clean up cache if it exceeds size limits
     */
    suspend fun performCacheMaintenance() {
        withContext(Dispatchers.IO) {
            val cacheInfo = getCacheInfo()

            // Clean up disk cache if it's too large
            if (cacheInfo.diskSizeBytes > MAX_CACHE_SIZE_MB * 1024 * 1024) {
                cleanupDiskCache()
            }
        }
    }

    /**
     * Clean up old files from disk cache (LRU based on last modified time)
     */
    private fun cleanupDiskCache() {
        if (!cacheDir.exists()) return

        val files = cacheDir.listFiles() ?: return

        // Sort by last modified time (oldest first)
        val sortedFiles = files.sortedBy { it.lastModified() }

        var currentSize = files.sumOf { it.length() }
        val targetSize = (MAX_CACHE_SIZE_MB * 1024 * 1024 * 0.8).toLong() // 80% of max size

        // Delete oldest files until we're under the target size
        for (file in sortedFiles) {
            if (currentSize <= targetSize) break

            currentSize -= file.length()
            file.delete()
        }
    }

    /**
     * Generate cache key for memory cache
     */
    private fun generateCacheKey(userId: String, size: ImageSize): String {
        return "${userId}_${size.name.lowercase()}"
    }

    /**
     * Get file for cached image on disk
     */
    private fun getCachedImageFile(userId: String, size: ImageSize): File {
        return File(cacheDir, "${userId}_${size.name.lowercase()}.jpg")
    }
}

/**
 * Information about cache usage
 */
data class CacheInfo(
    val memorySizeBytes: Long,
    val diskSizeBytes: Long,
    val totalFilesCount: Int
) {
    val totalSizeBytes: Long = memorySizeBytes + diskSizeBytes

    val memorySizeMB: Float = memorySizeBytes / (1024f * 1024f)
    val diskSizeMB: Float = diskSizeBytes / (1024f * 1024f)
    val totalSizeMB: Float = totalSizeBytes / (1024f * 1024f)
}
