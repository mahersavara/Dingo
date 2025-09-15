package io.sukhuat.dingo.widget.persistence

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.WeeklyGoalWidgetRepository
import io.sukhuat.dingo.widget.WidgetErrorHandler
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced widget data loader with improved persistence and error handling
 * Based on modern Android widget best practices
 */
@Singleton
class EnhancedWidgetDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WeeklyGoalWidgetRepository,
    private val persistenceManager: WidgetPersistenceManager,
    private val errorHandler: WidgetErrorHandler
) {

    /**
     * Load goals with enhanced caching and fallback strategies
     */
    suspend fun loadGoalsForWeek(
        weekOfYear: Int,
        year: Int,
        forceRefresh: Boolean = false,
        widgetId: Int = -1
    ): WidgetDataResult<List<WidgetGoal>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("EnhancedWidgetDataLoader", "📦 Loading goals for week $weekOfYear/$year (force: $forceRefresh, widget: $widgetId)")

            // Check configuration first
            val config = persistenceManager.getWidgetConfigurationSync()
            if (!config.autoUpdateEnabled && !forceRefresh) {
                android.util.Log.d("EnhancedWidgetDataLoader", "⏸️ Auto-update disabled, using cached data")
                return@withContext loadFromCache(weekOfYear, year)
            }

            // Check if we should use cached data
            if (!forceRefresh && persistenceManager.isCacheValid()) {
                val cachedData = persistenceManager.getCachedGoals()
                if (cachedData != null && cachedData.goals.isNotEmpty()) {
                    android.util.Log.d("EnhancedWidgetDataLoader", "📋 Using valid cached data (${cachedData.goals.size} goals)")
                    return@withContext WidgetDataResult.Success(
                        data = cachedData.goals,
                        isStale = cachedData.isStale
                    )
                }
            }

            // Check network connectivity
            if (!errorHandler.isNetworkAvailable()) {
                android.util.Log.w("EnhancedWidgetDataLoader", "📴 Network unavailable, trying cached data")
                return@withContext loadFromCache(weekOfYear, year, allowStale = true)
            }

            // Check error count for circuit breaker pattern
            if (!persistenceManager.shouldRetryUpdate()) {
                android.util.Log.w("EnhancedWidgetDataLoader", "🚫 Too many errors, using cached data")
                return@withContext loadFromCache(weekOfYear, year, allowStale = true)
            }

            // Load fresh data from repository
            val freshGoals = loadFreshData(weekOfYear, year)

            // Cache the results with metadata
            val cachedData = WidgetPersistenceManager.CachedGoalData(
                goals = freshGoals,
                weekOfYear = weekOfYear,
                year = year,
                timestamp = System.currentTimeMillis(),
                isStale = false,
                errorCount = 0
            )
            persistenceManager.cacheGoals(cachedData)

            // Reset error count on success
            persistenceManager.resetErrorCount()

            android.util.Log.d("EnhancedWidgetDataLoader", "✅ Successfully loaded ${freshGoals.size} fresh goals")
            WidgetDataResult.Success(freshGoals)
        } catch (e: SecurityException) {
            android.util.Log.e("EnhancedWidgetDataLoader", "🔒 Authentication error", e)
            persistenceManager.incrementErrorCount()
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.AuthenticationError)
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetDataLoader", "❌ Error loading goals", e)
            persistenceManager.incrementErrorCount()

            // Try to return cached data as fallback
            val fallbackResult = loadFromCache(weekOfYear, year, allowStale = true)
            if (fallbackResult is WidgetDataResult.Success) {
                fallbackResult.copy(isStale = true)
            } else {
                WidgetDataResult.Error(WidgetErrorHandler.WidgetError.CustomError(e.message ?: "Unknown error"))
            }
        }
    }

    private suspend fun loadFreshData(weekOfYear: Int, year: Int): List<WidgetGoal> {
        return try {
            val goals = widgetRepository.getGoalsForWeek(weekOfYear, year)
            android.util.Log.d("EnhancedWidgetDataLoader", "🔄 Loaded ${goals.size} goals from repository")
            goals
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetDataLoader", "❌ Repository error", e)
            throw e
        }
    }

    private suspend fun loadFromCache(
        weekOfYear: Int,
        year: Int,
        allowStale: Boolean = false
    ): WidgetDataResult<List<WidgetGoal>> {
        val cachedData = persistenceManager.getCachedGoals()

        return when {
            cachedData == null || cachedData.goals.isEmpty() -> {
                android.util.Log.w("EnhancedWidgetDataLoader", "📭 No cached data available")
                WidgetDataResult.Error(WidgetErrorHandler.WidgetError.CustomError("No cached data available"))
            }

            cachedData.isStale && !allowStale -> {
                android.util.Log.w("EnhancedWidgetDataLoader", "⏰ Cached data is stale")
                WidgetDataResult.Error(WidgetErrorHandler.WidgetError.CustomError("Cached data is stale"))
            }

            else -> {
                android.util.Log.d("EnhancedWidgetDataLoader", "📋 Using cached data (${cachedData.goals.size} goals, stale: ${cachedData.isStale})")
                WidgetDataResult.Success(
                    data = cachedData.goals,
                    isStale = cachedData.isStale
                )
            }
        }
    }

    /**
     * Load current week goals with enhanced logic
     */
    suspend fun loadCurrentWeekGoals(
        forceRefresh: Boolean = false,
        widgetId: Int = -1
    ): WidgetDataResult<List<WidgetGoal>> {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return loadGoalsForWeek(currentWeek, currentYear, forceRefresh, widgetId)
    }

    /**
     * Load goals with week offset for navigation
     */
    suspend fun loadGoalsWithOffset(
        weekOffset: Int,
        widgetId: Int = -1,
        forceRefresh: Boolean = false
    ): WidgetDataResult<List<WidgetGoal>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

        val targetWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        // Save the week offset for this widget
        if (widgetId != -1) {
            persistenceManager.setWidgetWeekOffset(widgetId, weekOffset)
        }

        return loadGoalsForWeek(targetWeek, targetYear, forceRefresh, widgetId)
    }

    /**
     * Preload data in background for better performance
     */
    suspend fun preloadData() = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("EnhancedWidgetDataLoader", "🚀 Preloading widget data")

            val config = persistenceManager.getWidgetConfigurationSync()
            if (!config.autoUpdateEnabled) {
                android.util.Log.d("EnhancedWidgetDataLoader", "⏸️ Auto-update disabled, skipping preload")
                return@withContext
            }

            // Preload current week and next week
            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            // Current week
            loadGoalsForWeek(currentWeek, currentYear, forceRefresh = false)

            // Next week
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val nextWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val nextYear = calendar.get(Calendar.YEAR)
            loadGoalsForWeek(nextWeek, nextYear, forceRefresh = false)

            android.util.Log.d("EnhancedWidgetDataLoader", "✅ Preload completed")
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetDataLoader", "❌ Preload failed", e)
        }
    }

    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        android.util.Log.d("EnhancedWidgetDataLoader", "🧹 Clearing widget cache")
        persistenceManager.clearCache()
    }

    /**
     * Get cache statistics for debugging
     */
    suspend fun getCacheStats(): Map<String, Any> {
        val cachedData = persistenceManager.getCachedGoals()
        return mapOf(
            "hasCachedData" to (cachedData != null),
            "cachedGoalsCount" to (cachedData?.goals?.size ?: 0),
            "cacheTimestamp" to (cachedData?.timestamp ?: 0L),
            "isStale" to (cachedData?.isStale ?: true),
            "errorCount" to (cachedData?.errorCount ?: 0),
            "cacheValid" to persistenceManager.isCacheValid()
        )
    }
}

/**
 * Enhanced result wrapper with additional metadata
 */
sealed class WidgetDataResult<out T> {
    data class Success<T>(
        val data: T,
        val isStale: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    ) : WidgetDataResult<T>()

    data class Error(
        val error: WidgetErrorHandler.WidgetError,
        val timestamp: Long = System.currentTimeMillis()
    ) : WidgetDataResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error
}
