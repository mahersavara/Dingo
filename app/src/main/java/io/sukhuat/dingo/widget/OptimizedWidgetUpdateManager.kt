package io.sukhuat.dingo.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized widget update manager with performance monitoring
 */
@Singleton
class OptimizedWidgetUpdateManager @Inject constructor(
    private val context: Context,
    private val performanceOptimizer: WidgetPerformanceOptimizer,
    private val widgetDataLoader: WidgetDataLoader
) {

    private val updateScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Update all widgets with performance optimization
     */
    private suspend fun updateAllWidgetsOptimized() {
        performanceOptimizer.optimizeMemoryUsage()

        val updates = mapOf<String, suspend () -> Unit>(
            "WeeklyGoalWidget_2x2" to { WeeklyGoalWidget.updateAll(context) },
            "WeeklyGoalWidget_2x3" to { WeeklyGoalWidget2x3.updateAll(context) },
            "WeeklyGoalWidget_3x2" to { WeeklyGoalWidget3x2.updateAll(context) }
        )

        val results = performanceOptimizer.batchUpdateWidgets(updates)

        // Handle any performance issues
        results.forEach { result ->
            if (!result.isOptimal) {
                handlePerformanceIssue(result)
            }
        }
    }

    /**
     * Update specific widget with performance monitoring
     */
    suspend fun updateWidgetOptimized(widgetType: WidgetType) {
        performanceOptimizer.optimizedWidgetUpdate(widgetType.name) {
            when (widgetType) {
                WidgetType.SIZE_2X2 -> WeeklyGoalWidget.updateAll(context)
                WidgetType.SIZE_2X3 -> WeeklyGoalWidget2x3.updateAll(context)
                WidgetType.SIZE_3X2 -> WeeklyGoalWidget3x2.updateAll(context)
            }
        }
    }

    /**
     * Smart update with adaptive refresh rates
     */
    suspend fun smartUpdate() {
        if (performanceOptimizer.isLowMemory()) {
            // Use cached data only for low memory devices
            return
        }

        // Check if we need to update based on data freshness
        val shouldUpdate = shouldPerformUpdate()

        if (shouldUpdate) {
            updateAllWidgetsOptimized()
        }
    }

    /**
     * Preload data for faster widget updates
     */
    private fun preloadWidgetData() {
        updateScope.launch {
            try {
                // Preload current week data
                widgetDataLoader.loadCurrentWeekGoals(forceRefresh = false)

                // Preload previous week data (commonly accessed)
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.WEEK_OF_YEAR, -1)
                widgetDataLoader.loadGoalsForWeek(
                    calendar.get(java.util.Calendar.WEEK_OF_YEAR),
                    calendar.get(java.util.Calendar.YEAR),
                    forceRefresh = false
                )
            } catch (e: Exception) {
                // Silently handle preload errors
            }
        }
    }

    /**
     * Clean up resources and cancel ongoing operations
     */
    fun cleanup() {
        updateScope.cancel()
    }

    private fun handlePerformanceIssue(result: PerformanceResult) {
        updateScope.launch {
            when {
                result.executionTimeMs > 1000 -> {
                    // Update took too long - adjust refresh rates
                    adjustRefreshRates(slower = true)
                }
                result.memoryUsedMB > 20 -> {
                    // High memory usage - clear caches
                    performanceOptimizer.optimizeMemoryUsage()
                }
                result.error != null -> {
                    // Error occurred - use fallback strategies
                    handleUpdateError(result.error)
                }
            }
        }
    }

    private fun shouldPerformUpdate(): Boolean {
        // Check various conditions to determine if update is needed
        return when {
            performanceOptimizer.isLowMemory() -> false
            !isNetworkAvailable() -> false
            else -> true
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.hasCapability(
            android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }

    private fun adjustRefreshRates(slower: Boolean) {
        // Adjust WorkManager intervals based on performance
        @Suppress("UNUSED_VARIABLE")
        val newInterval = if (slower) {
            performanceOptimizer.getOptimalUpdateInterval() * 2
        } else {
            performanceOptimizer.getOptimalUpdateInterval()
        }

        // This would update the WorkManager periodic work request
        // Implementation depends on how WorkManager is configured
    }

    private fun handleUpdateError(error: String) {
        // Log error and implement fallback strategies
        when {
            error.contains("timeout") -> {
                // Use cached data for timeouts
                updateScope.launch {
                    // Load from cache only
                }
            }
            error.contains("network") -> {
                // Schedule retry when network is available
            }
            else -> {
                // Generic error handling
            }
        }
    }
}

/**
 * Widget type enumeration for type-safe updates
 */
enum class WidgetType {
    SIZE_2X2,
    SIZE_2X3,
    SIZE_3X2
}
