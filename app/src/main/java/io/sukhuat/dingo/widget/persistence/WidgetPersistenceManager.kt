package io.sukhuat.dingo.widget.persistence

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced widget persistence manager inspired by modern Android widget best practices
 * Combines DataStore for configuration and SharedPreferences for widget runtime data
 */
@Singleton
class WidgetPersistenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        // DataStore for widget configuration and settings
        private val Context.widgetConfigDataStore by preferencesDataStore(name = "widget_config")

        // DataStore keys for configuration
        private val WIDGET_SIZE_KEY = stringPreferencesKey("widget_size")
        private val SHOW_WEEK_NAVIGATION_KEY = booleanPreferencesKey("show_week_navigation")
        private val AUTO_UPDATE_ENABLED_KEY = booleanPreferencesKey("auto_update_enabled")
        private val UPDATE_INTERVAL_KEY = intPreferencesKey("update_interval_minutes")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val SHOW_COMPLETED_GOALS_KEY = booleanPreferencesKey("show_completed_goals")

        // Cache and state keys
        private val CACHE_TIMESTAMP_KEY = longPreferencesKey("cache_timestamp")
        private val CACHED_GOALS_KEY = stringPreferencesKey("cached_goals")
        private val LAST_UPDATE_STATUS_KEY = stringPreferencesKey("last_update_status")
        private val ERROR_COUNT_KEY = intPreferencesKey("error_count")

        // Widget state keys per widget ID
        private fun widgetStateKey(widgetId: Int) = stringPreferencesKey("widget_state_$widgetId")
        private fun widgetWeekOffsetKey(widgetId: Int) = intPreferencesKey("widget_week_offset_$widgetId")

        // SharedPreferences for runtime widget access
        private const val WIDGET_RUNTIME_PREFS = "widget_runtime_data"
        private const val WIDGET_GOALS_SYNC_PREFS = "widget_goals_sync"

        // Cache duration - 15 minutes for better responsiveness
        private const val CACHE_DURATION_MS = 15 * 60 * 1000L
        private const val MAX_ERROR_COUNT = 3
    }

    /**
     * Widget Configuration Management
     */

    data class WidgetConfiguration(
        val widgetSize: String = "2x3",
        val showWeekNavigation: Boolean = true,
        val autoUpdateEnabled: Boolean = true,
        val updateIntervalMinutes: Int = 15,
        val themeMode: String = "auto",
        val showCompletedGoals: Boolean = true
    )

    suspend fun saveWidgetConfiguration(config: WidgetConfiguration) {
        context.widgetConfigDataStore.edit { prefs ->
            prefs[WIDGET_SIZE_KEY] = config.widgetSize
            prefs[SHOW_WEEK_NAVIGATION_KEY] = config.showWeekNavigation
            prefs[AUTO_UPDATE_ENABLED_KEY] = config.autoUpdateEnabled
            prefs[UPDATE_INTERVAL_KEY] = config.updateIntervalMinutes
            prefs[THEME_MODE_KEY] = config.themeMode
            prefs[SHOW_COMPLETED_GOALS_KEY] = config.showCompletedGoals
        }
    }

    fun getWidgetConfiguration(): Flow<WidgetConfiguration> {
        return context.widgetConfigDataStore.data.map { prefs ->
            WidgetConfiguration(
                widgetSize = prefs[WIDGET_SIZE_KEY] ?: "2x3",
                showWeekNavigation = prefs[SHOW_WEEK_NAVIGATION_KEY] ?: true,
                autoUpdateEnabled = prefs[AUTO_UPDATE_ENABLED_KEY] ?: true,
                updateIntervalMinutes = prefs[UPDATE_INTERVAL_KEY] ?: 15,
                themeMode = prefs[THEME_MODE_KEY] ?: "auto",
                showCompletedGoals = prefs[SHOW_COMPLETED_GOALS_KEY] ?: true
            )
        }
    }

    suspend fun getWidgetConfigurationSync(): WidgetConfiguration {
        val prefs = context.widgetConfigDataStore.data.first()
        return WidgetConfiguration(
            widgetSize = prefs[WIDGET_SIZE_KEY] ?: "2x3",
            showWeekNavigation = prefs[SHOW_WEEK_NAVIGATION_KEY] ?: true,
            autoUpdateEnabled = prefs[AUTO_UPDATE_ENABLED_KEY] ?: true,
            updateIntervalMinutes = prefs[UPDATE_INTERVAL_KEY] ?: 15,
            themeMode = prefs[THEME_MODE_KEY] ?: "auto",
            showCompletedGoals = prefs[SHOW_COMPLETED_GOALS_KEY] ?: true
        )
    }

    /**
     * Widget State Management (per widget instance)
     */

    data class WidgetState(
        val widgetId: Int,
        val weekOffset: Int = 0,
        val lastSelectedWeek: Int = -1,
        val lastSelectedYear: Int = -1,
        val isConfigured: Boolean = false,
        val errorState: String? = null
    )

    suspend fun saveWidgetState(state: WidgetState) {
        context.widgetConfigDataStore.edit { prefs ->
            val stateJson = gson.toJson(state)
            prefs[widgetStateKey(state.widgetId)] = stateJson
            prefs[widgetWeekOffsetKey(state.widgetId)] = state.weekOffset
        }
    }

    suspend fun getWidgetState(widgetId: Int): WidgetState {
        val prefs = context.widgetConfigDataStore.data.first()
        val stateJson = prefs[widgetStateKey(widgetId)]

        return if (stateJson != null) {
            try {
                gson.fromJson(stateJson, WidgetState::class.java)
            } catch (e: Exception) {
                WidgetState(widgetId = widgetId)
            }
        } else {
            WidgetState(widgetId = widgetId)
        }
    }

    suspend fun getWidgetWeekOffset(widgetId: Int): Int {
        val prefs = context.widgetConfigDataStore.data.first()
        return prefs[widgetWeekOffsetKey(widgetId)] ?: 0
    }

    suspend fun setWidgetWeekOffset(widgetId: Int, offset: Int) {
        context.widgetConfigDataStore.edit { prefs ->
            prefs[widgetWeekOffsetKey(widgetId)] = offset
        }
    }

    /**
     * Enhanced Goal Caching with Multi-layer Storage
     */

    data class CachedGoalData(
        val goals: List<WidgetGoal>,
        val weekOfYear: Int,
        val year: Int,
        val timestamp: Long = System.currentTimeMillis(),
        val isStale: Boolean = false,
        val errorCount: Int = 0
    )

    suspend fun cacheGoals(data: CachedGoalData) {
        // Save to DataStore for configuration persistence
        context.widgetConfigDataStore.edit { prefs ->
            val goalsJson = gson.toJson(data)
            prefs[CACHED_GOALS_KEY] = goalsJson
            prefs[CACHE_TIMESTAMP_KEY] = data.timestamp
            prefs[ERROR_COUNT_KEY] = data.errorCount
        }

        // Also save to SharedPreferences for immediate widget access
        cacheGoalsToSharedPreferences(data.goals)
    }

    private fun cacheGoalsToSharedPreferences(goals: List<WidgetGoal>) {
        try {
            val prefs = context.getSharedPreferences(WIDGET_GOALS_SYNC_PREFS, Context.MODE_PRIVATE)
            val goalsJson = gson.toJson(goals)

            prefs.edit()
                .putString("cached_goals", goalsJson)
                .putLong("cache_timestamp", System.currentTimeMillis())
                .putInt("goals_count", goals.size)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("WidgetPersistenceManager", "Error caching goals to SharedPreferences", e)
        }
    }

    suspend fun getCachedGoals(): CachedGoalData? {
        return try {
            val prefs = context.widgetConfigDataStore.data.first()
            val goalsJson = prefs[CACHED_GOALS_KEY]

            if (goalsJson != null) {
                val data = gson.fromJson(goalsJson, CachedGoalData::class.java)
                data.copy(isStale = isDataStale(data.timestamp))
            } else {
                getCachedGoalsFromSharedPreferences()
            }
        } catch (e: Exception) {
            android.util.Log.e("WidgetPersistenceManager", "Error loading cached goals", e)
            getCachedGoalsFromSharedPreferences()
        }
    }

    private fun getCachedGoalsFromSharedPreferences(): CachedGoalData? {
        return try {
            val prefs = context.getSharedPreferences(WIDGET_GOALS_SYNC_PREFS, Context.MODE_PRIVATE)
            val goalsJson = prefs.getString("cached_goals", null)
            val timestamp = prefs.getLong("cache_timestamp", 0L)

            if (goalsJson != null && timestamp > 0) {
                val goalsType = object : TypeToken<List<WidgetGoal>>() {}.type
                val goals: List<WidgetGoal> = gson.fromJson(goalsJson, goalsType)

                CachedGoalData(
                    goals = goals,
                    weekOfYear = -1, // Not stored in SharedPreferences
                    year = -1,
                    timestamp = timestamp,
                    isStale = isDataStale(timestamp)
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("WidgetPersistenceManager", "Error loading cached goals from SharedPreferences", e)
            null
        }
    }

    suspend fun isCacheValid(): Boolean {
        val prefs = context.widgetConfigDataStore.data.first()
        val timestamp = prefs[CACHE_TIMESTAMP_KEY] ?: 0L
        return !isDataStale(timestamp)
    }

    private fun isDataStale(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
    }

    /**
     * Error Handling and Recovery
     */

    suspend fun incrementErrorCount(): Int {
        val prefs = context.widgetConfigDataStore.data.first()
        val currentCount = prefs[ERROR_COUNT_KEY] ?: 0
        val newCount = currentCount + 1

        context.widgetConfigDataStore.edit {
            it[ERROR_COUNT_KEY] = newCount
            it[LAST_UPDATE_STATUS_KEY] = "error_${System.currentTimeMillis()}"
        }

        return newCount
    }

    suspend fun resetErrorCount() {
        context.widgetConfigDataStore.edit {
            it[ERROR_COUNT_KEY] = 0
            it[LAST_UPDATE_STATUS_KEY] = "success_${System.currentTimeMillis()}"
        }
    }

    suspend fun shouldRetryUpdate(): Boolean {
        val prefs = context.widgetConfigDataStore.data.first()
        val errorCount = prefs[ERROR_COUNT_KEY] ?: 0
        return errorCount < MAX_ERROR_COUNT
    }

    /**
     * Widget Runtime Data (Synchronous access for widgets)
     */

    fun saveRuntimeData(key: String, value: String) {
        val prefs = context.getSharedPreferences(WIDGET_RUNTIME_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    fun getRuntimeData(key: String, defaultValue: String = ""): String {
        val prefs = context.getSharedPreferences(WIDGET_RUNTIME_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveRuntimeInt(key: String, value: Int) {
        val prefs = context.getSharedPreferences(WIDGET_RUNTIME_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }

    fun getRuntimeInt(key: String, defaultValue: Int = 0): Int {
        val prefs = context.getSharedPreferences(WIDGET_RUNTIME_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(key, defaultValue)
    }

    /**
     * Cleanup and Maintenance
     */

    suspend fun clearCache() {
        context.widgetConfigDataStore.edit { prefs ->
            prefs.remove(CACHED_GOALS_KEY)
            prefs.remove(CACHE_TIMESTAMP_KEY)
            prefs.remove(ERROR_COUNT_KEY)
        }

        // Clear SharedPreferences cache too
        val prefs = context.getSharedPreferences(WIDGET_GOALS_SYNC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    suspend fun clearWidgetState(widgetId: Int) {
        context.widgetConfigDataStore.edit { prefs ->
            prefs.remove(widgetStateKey(widgetId))
            prefs.remove(widgetWeekOffsetKey(widgetId))
        }
    }

    suspend fun exportWidgetData(): Map<String, Any> {
        val config = getWidgetConfigurationSync()
        val cachedData = getCachedGoals()

        return mapOf(
            "configuration" to config,
            "cachedData" to (cachedData ?: emptyMap<String, Any>()),
            "timestamp" to System.currentTimeMillis()
        )
    }
}
