package io.sukhuat.dingo.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.persistence.EnhancedWidgetDataLoader
import io.sukhuat.dingo.widget.persistence.WidgetPersistenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced widget data updater that coordinates between the new persistence system
 * and existing widget components
 */
@Singleton
class EnhancedWidgetDataUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enhancedDataLoader: EnhancedWidgetDataLoader,
    private val persistenceManager: WidgetPersistenceManager,
    private val originalDataLoader: WidgetDataLoader
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Update all widgets with enhanced data loading
     */
    fun updateAllWidgets(forceRefresh: Boolean = false) {
        scope.launch {
            try {
                android.util.Log.d("EnhancedWidgetUpdater", "🔄 Starting enhanced widget update (force: $forceRefresh)")

                val appWidgetManager = AppWidgetManager.getInstance(context)

                // Update the single widget
                updateWidgetsOfType(
                    appWidgetManager = appWidgetManager,
                    componentName = ComponentName(context, WeeklyGoalWidgetProvider::class.java),
                    widgetType = "2x3",
                    forceRefresh = forceRefresh
                )

                // Update main widgets
                updateWidgetsOfType(
                    appWidgetManager = appWidgetManager,
                    componentName = ComponentName(context, WeeklyGoalWidgetProvider::class.java),
                    widgetType = "main",
                    forceRefresh = forceRefresh
                )

                android.util.Log.d("EnhancedWidgetUpdater", "✅ Enhanced widget update completed")
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetUpdater", "❌ Error updating widgets", e)
            }
        }
    }

    private suspend fun updateWidgetsOfType(
        appWidgetManager: AppWidgetManager,
        componentName: ComponentName,
        widgetType: String,
        forceRefresh: Boolean
    ) {
        try {
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isEmpty()) {
                android.util.Log.d("EnhancedWidgetUpdater", "📱 No $widgetType widgets found")
                return
            }

            android.util.Log.d("EnhancedWidgetUpdater", "📱 Updating ${widgetIds.size} $widgetType widgets")

            for (widgetId in widgetIds) {
                updateWidget(widgetId, widgetType, forceRefresh)
            }
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetUpdater", "❌ Error updating $widgetType widgets", e)
        }
    }

    /**
     * Update a specific widget with enhanced data
     */
    suspend fun updateWidget(widgetId: Int, widgetType: String = "main", forceRefresh: Boolean = false) {
        try {
            android.util.Log.d("EnhancedWidgetUpdater", "🔧 Updating widget $widgetId ($widgetType)")

            // Get widget state and week offset
            val widgetState = persistenceManager.getWidgetState(widgetId)
            val weekOffset = widgetState.weekOffset

            // Load data using enhanced loader
            val dataResult = if (weekOffset != 0) {
                enhancedDataLoader.loadGoalsWithOffset(weekOffset, widgetId, forceRefresh)
            } else {
                enhancedDataLoader.loadCurrentWeekGoals(forceRefresh, widgetId)
            }

            when (dataResult) {
                is io.sukhuat.dingo.widget.persistence.WidgetDataResult.Success -> {
                    android.util.Log.d("EnhancedWidgetUpdater", "📊 Loaded ${dataResult.data.size} goals for widget $widgetId")

                    // Update widget state
                    val updatedState = widgetState.copy(
                        errorState = null,
                        isConfigured = true
                    )
                    persistenceManager.saveWidgetState(updatedState)

                    // Bridge to existing widget update system
                    bridgeToExistingSystem(widgetId, widgetType, dataResult.data)

                    android.util.Log.d("EnhancedWidgetUpdater", "✅ Widget $widgetId updated successfully")
                }

                is io.sukhuat.dingo.widget.persistence.WidgetDataResult.Error -> {
                    android.util.Log.w("EnhancedWidgetUpdater", "⚠️ Error loading data for widget $widgetId: ${dataResult.error}")

                    // Update widget state with error
                    val errorState = widgetState.copy(
                        errorState = dataResult.error.toString()
                    )
                    persistenceManager.saveWidgetState(errorState)

                    // Try to show error state on widget
                    showErrorOnWidget(widgetId, widgetType, dataResult.error.toString())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetUpdater", "❌ Error updating widget $widgetId", e)
        }
    }

    /**
     * Bridge the enhanced data loading to the existing widget system
     */
    private suspend fun bridgeToExistingSystem(
        widgetId: Int,
        widgetType: String,
        goals: List<io.sukhuat.dingo.widget.models.WidgetGoal>
    ) {
        try {
            // Update the original cache system for compatibility
            val calendar = java.util.Calendar.getInstance()
            val weekOfYear = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            val year = calendar.get(java.util.Calendar.YEAR)
            originalDataLoader.cacheGoalsSync(weekOfYear, year, goals)

            // Trigger widget provider updates
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // Use the single widget provider for all cases
            val provider = WeeklyGoalWidgetProvider()
            provider.onUpdate(context, appWidgetManager, intArrayOf(widgetId))
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetUpdater", "❌ Error bridging to existing system", e)
        }
    }

    private fun showErrorOnWidget(widgetId: Int, widgetType: String, error: String) {
        // Implementation would depend on your existing error display mechanism
        android.util.Log.w("EnhancedWidgetUpdater", "📱 Showing error on widget $widgetId: $error")

        // For now, just trigger a regular update which should show cached/fallback data
        val appWidgetManager = AppWidgetManager.getInstance(context)
        // Use the single widget provider for error display
        val provider = WeeklyGoalWidgetProvider()
        provider.onUpdate(context, appWidgetManager, intArrayOf(widgetId))
    }

    /**
     * Handle widget navigation (week offset changes)
     */
    fun handleWidgetNavigation(widgetId: Int, direction: String) {
        scope.launch {
            try {
                val currentState = persistenceManager.getWidgetState(widgetId)
                val newOffset = when (direction) {
                    "previous" -> currentState.weekOffset - 1
                    "next" -> currentState.weekOffset + 1
                    "current" -> 0
                    else -> currentState.weekOffset
                }

                android.util.Log.d("EnhancedWidgetUpdater", "🗓️ Widget $widgetId navigation: $direction (offset: $newOffset)")

                // Update widget state with new offset
                val updatedState = currentState.copy(weekOffset = newOffset)
                persistenceManager.saveWidgetState(updatedState)

                // Update widget with new data
                updateWidget(widgetId, forceRefresh = false)
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetUpdater", "❌ Error handling widget navigation", e)
            }
        }
    }

    /**
     * Preload data for better performance
     */
    fun preloadWidgetData() {
        scope.launch {
            try {
                android.util.Log.d("EnhancedWidgetUpdater", "🚀 Preloading widget data")
                enhancedDataLoader.preloadData()
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetUpdater", "❌ Error preloading data", e)
            }
        }
    }

    /**
     * Get enhanced cache statistics for debugging
     */
    suspend fun getCacheStats(): Map<String, Any> {
        return try {
            enhancedDataLoader.getCacheStats()
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetUpdater", "❌ Error getting cache stats", e)
            emptyMap()
        }
    }

    /**
     * Clear enhanced cache
     */
    fun clearCache() {
        scope.launch {
            try {
                android.util.Log.d("EnhancedWidgetUpdater", "🧹 Clearing enhanced cache")
                enhancedDataLoader.clearCache()
                updateAllWidgets(forceRefresh = true)
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetUpdater", "❌ Error clearing cache", e)
            }
        }
    }
}
