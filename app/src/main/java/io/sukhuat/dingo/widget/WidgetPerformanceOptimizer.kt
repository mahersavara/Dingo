package io.sukhuat.dingo.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * Performance optimization and monitoring for widgets
 */
@Singleton
class WidgetPerformanceOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MAX_UPDATE_TIME_MS = 1000L // 1 second max
        private const val MAX_MEMORY_MB = 20L // 20MB max memory usage
        private const val PERFORMANCE_LOG_THRESHOLD = 500L // Log if > 500ms
    }

    /**
     * Monitor and optimize widget update performance
     */
    suspend fun optimizedWidgetUpdate(
        widgetName: String,
        updateFunction: suspend () -> Unit
    ): PerformanceResult {
        val initialMemory = getMemoryUsage()

        val executionTime = measureTimeMillis {
            withTimeout(MAX_UPDATE_TIME_MS) {
                try {
                    updateFunction()
                } catch (e: TimeoutCancellationException) {
                    // Widget update timed out - fallback to cached data
                    throw WidgetPerformanceException("Widget update timeout: $widgetName")
                }
            }
        }

        val finalMemory = getMemoryUsage()
        val memoryUsed = finalMemory - initialMemory

        // Log performance if needed
        if (executionTime > PERFORMANCE_LOG_THRESHOLD) {
            logPerformanceIssue(widgetName, executionTime, memoryUsed)
        }

        // Check if memory usage is excessive
        if (memoryUsed > MAX_MEMORY_MB) {
            System.gc() // Suggest garbage collection
        }

        return PerformanceResult(
            widgetName = widgetName,
            executionTimeMs = executionTime,
            memoryUsedMB = memoryUsed,
            isOptimal = executionTime < MAX_UPDATE_TIME_MS && memoryUsed < MAX_MEMORY_MB
        )
    }

    /**
     * Batch update multiple widgets efficiently
     */
    suspend fun batchUpdateWidgets(
        updates: Map<String, suspend () -> Unit>
    ): List<PerformanceResult> {
        val results = mutableListOf<PerformanceResult>()

        // Use coroutine scope for parallel updates
        coroutineScope {
            val jobs = updates.map { (widgetName, updateFunction) ->
                async(Dispatchers.IO) {
                    optimizedWidgetUpdate(widgetName, updateFunction)
                }
            }

            // Collect all results
            jobs.forEach { job ->
                try {
                    results.add(job.await())
                } catch (e: Exception) {
                    // Add failed result
                    results.add(
                        PerformanceResult(
                            widgetName = "unknown",
                            executionTimeMs = -1,
                            memoryUsedMB = -1,
                            isOptimal = false,
                            error = e.message
                        )
                    )
                }
            }
        }

        return results
    }

    /**
     * Optimize memory usage by clearing caches when needed
     */
    suspend fun optimizeMemoryUsage() {
        val currentMemory = getMemoryUsage()

        if (currentMemory > MAX_MEMORY_MB * 2) {
            // Clear widget caches if memory usage is high
            try {
                // Clear image caches
                clearImageCaches()

                // Suggest garbage collection
                System.gc()

                // Clear any large data structures
                clearLargeDataStructures()
            } catch (e: Exception) {
                // Silently handle memory optimization errors
            }
        }
    }

    /**
     * Get current memory usage in MB
     */
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / (1024 * 1024) // Convert to MB
    }

    /**
     * Log performance issues for monitoring
     */
    private fun logPerformanceIssue(widgetName: String, timeMs: Long, memoryMB: Long) {
        // In a real app, this would log to analytics/crash reporting
        println("Widget Performance Issue: $widgetName took ${timeMs}ms, used ${memoryMB}MB")
    }

    /**
     * Clear image caches to free memory
     */
    private fun clearImageCaches() {
        // Clear Coil image cache if needed
        try {
            // This would clear image loading caches
        } catch (e: Exception) {
            // Handle cache clear errors
        }
    }

    /**
     * Clear large data structures to free memory
     */
    private fun clearLargeDataStructures() {
        // Clear any large collections or cached data
        // This could include clearing goal cache if memory is critically low
    }

    /**
     * Check if device has low memory
     */
    fun isLowMemory(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return memInfo.lowMemory || memInfo.availMem < memInfo.threshold
    }

    /**
     * Get optimal update interval based on device performance
     */
    fun getOptimalUpdateInterval(): Long {
        return when {
            isLowMemory() -> 60 * 60 * 1000L // 1 hour for low memory devices
            getMemoryUsage() > MAX_MEMORY_MB -> 45 * 60 * 1000L // 45 minutes for high memory usage
            else -> 30 * 60 * 1000L // 30 minutes for normal devices
        }
    }
}

/**
 * Performance monitoring result
 */
data class PerformanceResult(
    val widgetName: String,
    val executionTimeMs: Long,
    val memoryUsedMB: Long,
    val isOptimal: Boolean,
    val error: String? = null
)

/**
 * Exception for widget performance issues
 */
class WidgetPerformanceException(message: String) : Exception(message)
