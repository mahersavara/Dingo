package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Comprehensive image cache manager for goal images
 * Handles downloading Firebase Storage images and caching them locally
 */
class GoalImageCacheManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "GoalImageCacheManager"
        private const val CACHE_FOLDER = "goal_image_cache"
        private const val MAX_CACHE_SIZE_MB = 50L
        private const val CACHE_CLEANUP_THRESHOLD = 0.8f // Clean when 80% full
        
        @Volatile
        private var INSTANCE: GoalImageCacheManager? = null
        
        fun getInstance(context: Context): GoalImageCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoalImageCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // In-memory cache for quick access
    private val memoryCache = ConcurrentHashMap<String, Bitmap>()
    private val downloadMutexMap = ConcurrentHashMap<String, Mutex>()
    
    private val cacheDir = File(context.filesDir, CACHE_FOLDER).apply {
        if (!exists()) mkdirs()
    }
    
    /**
     * Get cached image or download if not available
     * @param firebaseUrl Firebase Storage URL
     * @return Local file URI for cached image, or null if failed
     */
    suspend fun getCachedImage(firebaseUrl: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Check if it's actually a Firebase URL
                if (!isFirebaseStorageUrl(firebaseUrl)) {
                    Log.d(TAG, "Not a Firebase Storage URL: $firebaseUrl")
                    return@withContext Uri.parse(firebaseUrl)
                }
                
                val cacheKey = generateCacheKey(firebaseUrl)
                val cachedFile = File(cacheDir, "$cacheKey.jpg")
                
                // Check if file exists in cache and is still valid
                if (cachedFile.exists() && cachedFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) {
                    Log.d(TAG, "Using cached image: ${cachedFile.absolutePath}")
                    return@withContext Uri.fromFile(cachedFile)
                }
                
                // Download image if not cached or expired
                downloadAndCache(firebaseUrl, cachedFile)?.let { 
                    Uri.fromFile(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached image for $firebaseUrl", e)
                null
            }
        }
    }
    
    /**
     * Download image from Firebase and cache it locally
     */
    private suspend fun downloadAndCache(firebaseUrl: String, cacheFile: File): File? {
        val cacheKey = generateCacheKey(firebaseUrl)
        
        // Use mutex to prevent concurrent downloads of the same image
        val mutex = downloadMutexMap.getOrPut(cacheKey) { Mutex() }
        
        return mutex.withLock {
            try {
                // Double-check if file was created while waiting for lock
                if (cacheFile.exists() && cacheFile.lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)) {
                    Log.d(TAG, "Image was cached while waiting: ${cacheFile.absolutePath}")
                    return@withLock cacheFile
                }
                
                Log.d(TAG, "Downloading image from Firebase: $firebaseUrl")
                
                val request = Request.Builder()
                    .url(firebaseUrl)
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        // Create temporary file first
                        val tempFile = File(cacheFile.parent, "${cacheFile.name}.tmp")
                        
                        responseBody.byteStream().use { inputStream ->
                            // Compress and save image
                            val compressedBytes = compressDownloadedImage(inputStream)
                            
                            FileOutputStream(tempFile).use { outputStream ->
                                outputStream.write(compressedBytes)
                                outputStream.flush()
                            }
                        }
                        
                        // Atomically move temp file to final location
                        if (tempFile.renameTo(cacheFile)) {
                            Log.d(TAG, "Successfully cached image: ${cacheFile.absolutePath}")
                            
                            // Clean cache if needed
                            cleanCacheIfNeeded()
                            
                            cacheFile
                        } else {
                            tempFile.delete()
                            Log.e(TAG, "Failed to rename temp file to cache file")
                            null
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to download image: ${response.code} ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading and caching image", e)
                null
            } finally {
                // Remove mutex after download completes
                downloadMutexMap.remove(cacheKey)
            }
        }
    }
    
    /**
     * Compress downloaded image to save storage space
     */
    private fun compressDownloadedImage(inputStream: InputStream): ByteArray {
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        if (bitmap == null) {
            throw IllegalArgumentException("Could not decode downloaded image")
        }
        
        // Calculate compression parameters
        val maxDimension = 800f // Slightly smaller for cached images
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        val scale = if (originalWidth > maxDimension || originalHeight > maxDimension) {
            minOf(maxDimension / originalWidth, maxDimension / originalHeight)
        } else {
            1f
        }
        
        val compressedBitmap = if (scale < 1f) {
            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()
            
            Log.d(TAG, "Resizing downloaded image from ${originalWidth}x$originalHeight to ${newWidth}x$newHeight")
            
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                if (it != bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }
        
        // Compress to JPEG with good quality for cached images
        return compressedBitmap.let { bmp ->
            val outputStream = java.io.ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // Higher quality for cached images
            if (bmp != bitmap) bmp.recycle()
            outputStream.toByteArray()
        }
    }
    
    /**
     * Generate cache key from Firebase URL
     */
    private fun generateCacheKey(url: String): String {
        return try {
            val bytes = MessageDigest.getInstance("MD5").digest(url.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback to simple hash if MD5 fails
            url.hashCode().toString().replace("-", "n")
        }
    }
    
    /**
     * Check if URL is a Firebase Storage URL
     */
    private fun isFirebaseStorageUrl(url: String): Boolean {
        return url.startsWith("https://firebasestorage.googleapis.com")
    }
    
    /**
     * Clean cache when it exceeds size limit
     */
    private fun cleanCacheIfNeeded() {
        try {
            val cacheSize = calculateCacheSize()
            val maxSize = MAX_CACHE_SIZE_MB * 1024 * 1024 // Convert to bytes
            
            if (cacheSize > maxSize * CACHE_CLEANUP_THRESHOLD) {
                Log.d(TAG, "Cache size ($cacheSize bytes) exceeds threshold, cleaning...")
                
                val files = cacheDir.listFiles()?.toList() ?: return
                
                // Sort by last access time (oldest first)
                val sortedFiles = files.sortedBy { it.lastModified() }
                
                var deletedSize = 0L
                val targetSize = maxSize * 0.6f // Clean down to 60% of max size
                
                for (file in sortedFiles) {
                    if (cacheSize - deletedSize <= targetSize) break
                    
                    val fileSize = file.length()
                    if (file.delete()) {
                        deletedSize += fileSize
                        Log.d(TAG, "Deleted cache file: ${file.name}")
                    }
                }
                
                Log.d(TAG, "Cache cleanup completed. Deleted $deletedSize bytes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cache cleanup", e)
        }
    }
    
    /**
     * Calculate total cache size
     */
    private fun calculateCacheSize(): Long {
        return try {
            cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }
    
    /**
     * Clear all cached images
     */
    fun clearCache() {
        try {
            Log.d(TAG, "Clearing image cache...")
            memoryCache.clear()
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Log.d(TAG, "Deleted cache file: ${file.name}")
                }
            }
            
            Log.d(TAG, "Cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return try {
            val files = cacheDir.listFiles() ?: emptyArray()
            val totalSize = files.sumOf { it.length() }
            val fileCount = files.size
            
            CacheStats(
                fileCount = fileCount,
                totalSizeBytes = totalSize,
                totalSizeMB = totalSize / (1024 * 1024).toDouble()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache stats", e)
            CacheStats(0, 0, 0.0)
        }
    }
    
    data class CacheStats(
        val fileCount: Int,
        val totalSizeBytes: Long,
        val totalSizeMB: Double
    )
}