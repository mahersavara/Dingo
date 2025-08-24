package io.sukhuat.dingo.data.cache

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.sukhuat.dingo.domain.model.yearplanner.SyncStatus
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "YearPlannerCacheManager"
private const val CACHE_PREFERENCES_NAME = "year_planner_cache"

// DataStore extension for cache
private val Context.yearPlannerCacheDataStore: DataStore<Preferences> by preferencesDataStore(
    name = CACHE_PREFERENCES_NAME
)

/**
 * Manages local caching for Year Planner data to support offline functionality
 * Uses DataStore for lightweight caching since project removed Room
 */
@Singleton
class YearPlannerCacheManager @Inject constructor(
    private val context: Context
) {
    
    private val gson = Gson()
    
    // DataStore keys for caching
    private fun yearPlanKey(year: Int) = stringPreferencesKey("year_plan_$year")
    private fun yearPlanTimestampKey(year: Int) = longPreferencesKey("year_plan_timestamp_$year")
    
    /**
     * Cache year plan data locally
     */
    suspend fun cacheYearPlan(yearPlan: YearPlan) {
        try {
            context.yearPlannerCacheDataStore.edit { preferences ->
                // Convert to JSON for storage (simplified caching)
                val yearPlanJson = gson.toJson(yearPlan)
                preferences[yearPlanKey(yearPlan.year)] = yearPlanJson
                preferences[yearPlanTimestampKey(yearPlan.year)] = System.currentTimeMillis()
            }
            Log.d(TAG, "Cached year plan for year ${yearPlan.year}")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching year plan for year ${yearPlan.year}", e)
        }
    }
    
    /**
     * Get cached year plan if available
     */
    suspend fun getCachedYearPlan(year: Int): YearPlan? {
        return try {
            context.yearPlannerCacheDataStore.data.first().let { preferences ->
                val yearPlanJson = preferences[yearPlanKey(year)]
                if (yearPlanJson != null) {
                    gson.fromJson(yearPlanJson, YearPlan::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached year plan for year $year", e)
            null
        }
    }
    
    /**
     * Check if cached data is fresh (within last hour)
     */
    suspend fun isCacheFresh(year: Int): Boolean {
        return try {
            context.yearPlannerCacheDataStore.data.first().let { preferences ->
                val timestamp = preferences[yearPlanTimestampKey(year)] ?: 0L
                val currentTime = System.currentTimeMillis()
                val oneHourInMillis = 60 * 60 * 1000 // 1 hour
                
                (currentTime - timestamp) < oneHourInMillis
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking cache freshness for year $year", e)
            false
        }
    }
    
    /**
     * Clear cached data for a specific year
     */
    suspend fun clearCacheForYear(year: Int) {
        try {
            context.yearPlannerCacheDataStore.edit { preferences ->
                preferences.remove(yearPlanKey(year))
                preferences.remove(yearPlanTimestampKey(year))
            }
            Log.d(TAG, "Cleared cache for year $year")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache for year $year", e)
        }
    }
    
    /**
     * Clear all cached year planner data
     */
    suspend fun clearAllCache() {
        try {
            context.yearPlannerCacheDataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d(TAG, "Cleared all year planner cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all cache", e)
        }
    }
    
    /**
     * Get all cached years
     */
    suspend fun getCachedYears(): List<Int> {
        return try {
            context.yearPlannerCacheDataStore.data.first().let { preferences ->
                preferences.asMap().keys
                    .mapNotNull { key ->
                        if (key.name.startsWith("year_plan_") && !key.name.endsWith("_timestamp")) {
                            key.name.removePrefix("year_plan_").toIntOrNull()
                        } else {
                            null
                        }
                    }
                    .sorted()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached years", e)
            emptyList()
        }
    }
}