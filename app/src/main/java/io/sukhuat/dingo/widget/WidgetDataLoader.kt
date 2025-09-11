package io.sukhuat.dingo.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// Create DataStore instance for widget cache
private val Context.widgetCacheDataStore by preferencesDataStore(name = "widget_cache")

/**
 * Handles data loading with offline support and caching
 */
@Singleton
class WidgetDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetRepository: WeeklyGoalWidgetRepository,
    private val errorHandler: WidgetErrorHandler
) {
    private val gson = Gson()

    companion object {
        private val CACHE_TIMESTAMP_KEY = longPreferencesKey("cache_timestamp")
        private val CACHED_GOALS_KEY = stringPreferencesKey("cached_goals")
        private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }

    /**
     * Load goals with offline support and caching
     */
    suspend fun loadGoalsForWeek(
        weekOfYear: Int,
        year: Int,
        forceRefresh: Boolean = false
    ): WidgetDataResult<List<WidgetGoal>> {
        return try {
            // Check if we should use cached data
            if (!forceRefresh && shouldUseCachedData()) {
                val cachedGoals = getCachedGoals(weekOfYear, year)
                if (cachedGoals.isNotEmpty()) {
                    return WidgetDataResult.Success(cachedGoals)
                }
            }

            // Check network connectivity
            if (!errorHandler.isNetworkAvailable()) {
                // Try to return cached data even if expired
                val cachedGoals = getCachedGoals(weekOfYear, year)
                return if (cachedGoals.isNotEmpty()) {
                    WidgetDataResult.Success(cachedGoals, isStale = true)
                } else {
                    WidgetDataResult.Error(WidgetErrorHandler.WidgetError.NetworkUnavailable)
                }
            }

            // Load fresh data from repository
            val goals = widgetRepository.getGoalsForWeek(weekOfYear, year)

            // Cache the results
            cacheGoals(goals)

            WidgetDataResult.Success(goals)
        } catch (e: SecurityException) {
            // User not authenticated
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.AuthenticationError)
        } catch (e: Exception) {
            // Try to return cached data on any error
            val cachedGoals = getCachedGoals(weekOfYear, year)
            if (cachedGoals.isNotEmpty()) {
                WidgetDataResult.Success(cachedGoals, isStale = true)
            } else {
                WidgetDataResult.Error(WidgetErrorHandler.WidgetError.DataLoadFailure)
            }
        }
    }

    /**
     * Load current week goals with caching
     */
    suspend fun loadCurrentWeekGoals(forceRefresh: Boolean = false): WidgetDataResult<List<WidgetGoal>> {
        val calendar = Calendar.getInstance()
        return loadGoalsForWeek(
            calendar.get(Calendar.WEEK_OF_YEAR),
            calendar.get(Calendar.YEAR),
            forceRefresh
        )
    }

    private suspend fun shouldUseCachedData(): Boolean {
        val prefs = context.widgetCacheDataStore.data.first()
        val timestamp = prefs[CACHE_TIMESTAMP_KEY] ?: 0L
        return System.currentTimeMillis() - timestamp < CACHE_DURATION_MS
    }

    private suspend fun getCachedGoals(weekOfYear: Int, year: Int): List<WidgetGoal> {
        return try {
            val prefs = context.widgetCacheDataStore.data.first()
            val cachedJson = prefs[CACHED_GOALS_KEY] ?: return emptyList()

            val type = object : TypeToken<List<CachedGoal>>() {}.type
            val cachedGoals: List<CachedGoal> = gson.fromJson(cachedJson, type)

            // Filter by week and convert to WidgetGoal
            cachedGoals
                .filter { it.weekOfYear == weekOfYear && it.yearCreated == year }
                .map { it.toWidgetGoal() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun cacheGoals(goals: List<WidgetGoal>) {
        try {
            val cachedGoals = goals.map { CachedGoal.fromWidgetGoal(it) }
            val json = gson.toJson(cachedGoals)

            context.widgetCacheDataStore.edit { prefs ->
                prefs[CACHED_GOALS_KEY] = json
                prefs[CACHE_TIMESTAMP_KEY] = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            // Silently fail cache operations
        }
    }

    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        context.widgetCacheDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Load cached goals synchronously for widget use
     * This is safe for widgets since it doesn't block the UI thread
     */
    fun getCachedGoalsSync(weekOfYear: Int, year: Int): List<WidgetGoal> {
        return try {
            val prefs = context.getSharedPreferences("widget_goals_sync", Context.MODE_PRIVATE)
            val key = "goals_${weekOfYear}_$year"
            val cachedJson = prefs.getString(key, null) ?: return emptyList()

            val type = object : TypeToken<List<CachedGoal>>() {}.type
            val cachedGoals: List<CachedGoal> = gson.fromJson(cachedJson, type) ?: emptyList()

            // Convert to WidgetGoal and return
            cachedGoals.map { it.toWidgetGoal() }
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataLoader", "Error loading cached goals sync", e)
            emptyList()
        }
    }

    /**
     * Cache goals synchronously in SharedPreferences for widget access
     */
    fun cacheGoalsSync(weekOfYear: Int, year: Int, goals: List<WidgetGoal>) {
        try {
            val prefs = context.getSharedPreferences("widget_goals_sync", Context.MODE_PRIVATE)
            val key = "goals_${weekOfYear}_$year"

            val cachedGoals = goals.map { CachedGoal.fromWidgetGoal(it) }
            val json = gson.toJson(cachedGoals)

            prefs.edit()
                .putString(key, json)
                .putLong("timestamp_$key", System.currentTimeMillis())
                .apply()

            android.util.Log.d("WidgetDataLoader", "Cached ${goals.size} goals for week $weekOfYear/$year")
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataLoader", "Error caching goals sync", e)
        }
    }

    /**
     * Check if cached data is fresh (within 30 minutes)
     */
    fun isCachedDataFresh(weekOfYear: Int, year: Int): Boolean {
        return try {
            val prefs = context.getSharedPreferences("widget_goals_sync", Context.MODE_PRIVATE)
            val key = "goals_${weekOfYear}_$year"
            val timestamp = prefs.getLong("timestamp_$key", 0L)
            val age = System.currentTimeMillis() - timestamp
            age < CACHE_DURATION_MS
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Result wrapper for widget data loading
 */
sealed class WidgetDataResult<T> {
    data class Success<T>(val data: T, val isStale: Boolean = false) : WidgetDataResult<T>()
    data class Error<T>(val error: WidgetErrorHandler.WidgetError) : WidgetDataResult<T>()
    data class Loading<T>(val message: String = "Loading...") : WidgetDataResult<T>()
}

/**
 * Cached goal data class for serialization
 */
private data class CachedGoal(
    val id: String,
    val text: String,
    val imageResId: Int?,
    val customImage: String?,
    val status: String,
    val weekOfYear: Int,
    val yearCreated: Int,
    val position: Int
) {
    fun toWidgetGoal(): WidgetGoal {
        return WidgetGoal(
            id = id,
            text = text,
            imageResId = imageResId,
            customImage = customImage,
            status = io.sukhuat.dingo.domain.model.GoalStatus.valueOf(status),
            weekOfYear = weekOfYear,
            yearCreated = yearCreated,
            position = position
        )
    }

    companion object {
        fun fromWidgetGoal(goal: WidgetGoal): CachedGoal {
            return CachedGoal(
                id = goal.id,
                text = goal.text,
                imageResId = goal.imageResId,
                customImage = goal.customImage,
                status = goal.status.name,
                weekOfYear = goal.weekOfYear,
                yearCreated = goal.yearCreated,
                position = goal.position
            )
        }
    }
}
