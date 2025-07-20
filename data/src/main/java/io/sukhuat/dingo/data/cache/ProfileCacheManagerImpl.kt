package io.sukhuat.dingo.data.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.model.ProfileCacheManager
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProfileCacheManager using DataStore for persistence
 * and in-memory cache for performance
 */
@Singleton
class ProfileCacheManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : ProfileCacheManager {

    companion object {
        private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 50

        private val Context.profileCacheDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "profile_cache"
        )
    }

    // In-memory cache for quick access
    private val memoryCache = ConcurrentHashMap<String, CacheEntry<Any>>()

    // DataStore for persistent cache
    private val dataStore = context.profileCacheDataStore

    override suspend fun cacheProfile(userId: String, profile: UserProfile) {
        try {
            val cacheKey = "profile_$userId"
            val cacheEntry = CacheEntry(profile, System.currentTimeMillis())

            // Store in memory cache
            memoryCache[cacheKey] = cacheEntry as CacheEntry<Any>

            // Store in persistent cache
            val profileJson = gson.toJson(profile)
            val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
            val dataKey = stringPreferencesKey("${cacheKey}_data")

            dataStore.edit { preferences ->
                preferences[timestampKey] = cacheEntry.timestamp.toString()
                preferences[dataKey] = profileJson
            }

            // Clean up old entries if cache is getting too large
            if (memoryCache.size > MAX_CACHE_SIZE) {
                cleanupOldEntries()
            }
        } catch (e: Exception) {
            throw ProfileError.CacheError("cache_profile: ${e.message}")
        }
    }

    override suspend fun getCachedProfile(userId: String): UserProfile? {
        return try {
            val cacheKey = "profile_$userId"

            // Try memory cache first
            val memoryCacheEntry = memoryCache[cacheKey]
            if (memoryCacheEntry != null && !isExpired(memoryCacheEntry.timestamp)) {
                return memoryCacheEntry.data as? UserProfile
            }

            // Try persistent cache
            val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
            val dataKey = stringPreferencesKey("${cacheKey}_data")

            val preferences = dataStore.data.first()
            val timestampStr = preferences[timestampKey]
            val profileJson = preferences[dataKey]

            if (timestampStr != null && profileJson != null) {
                val timestamp = timestampStr.toLongOrNull() ?: return null

                if (!isExpired(timestamp)) {
                    val profile = gson.fromJson(profileJson, UserProfile::class.java)

                    // Update memory cache
                    memoryCache[cacheKey] = CacheEntry(profile, timestamp) as CacheEntry<Any>

                    return profile
                }
            }

            null
        } catch (e: JsonSyntaxException) {
            // Invalid cached data, remove it
            invalidateProfileCache(userId)
            null
        } catch (e: Exception) {
            throw ProfileError.CacheError("get_cached_profile: ${e.message}")
        }
    }

    override suspend fun cacheStatistics(userId: String, statistics: ProfileStatistics) {
        try {
            val cacheKey = "statistics_$userId"
            val cacheEntry = CacheEntry(statistics, System.currentTimeMillis())

            // Store in memory cache
            memoryCache[cacheKey] = cacheEntry as CacheEntry<Any>

            // Store in persistent cache
            val statisticsJson = gson.toJson(statistics)
            val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
            val dataKey = stringPreferencesKey("${cacheKey}_data")

            dataStore.edit { preferences ->
                preferences[timestampKey] = cacheEntry.timestamp.toString()
                preferences[dataKey] = statisticsJson
            }
        } catch (e: Exception) {
            throw ProfileError.CacheError("cache_statistics: ${e.message}")
        }
    }

    override suspend fun getCachedStatistics(userId: String): ProfileStatistics? {
        return try {
            val cacheKey = "statistics_$userId"

            // Try memory cache first
            val memoryCacheEntry = memoryCache[cacheKey]
            if (memoryCacheEntry != null && !isExpired(memoryCacheEntry.timestamp)) {
                return memoryCacheEntry.data as? ProfileStatistics
            }

            // Try persistent cache
            val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
            val dataKey = stringPreferencesKey("${cacheKey}_data")

            val preferences = dataStore.data.first()
            val timestampStr = preferences[timestampKey]
            val statisticsJson = preferences[dataKey]

            if (timestampStr != null && statisticsJson != null) {
                val timestamp = timestampStr.toLongOrNull() ?: return null

                if (!isExpired(timestamp)) {
                    val statistics = gson.fromJson(statisticsJson, ProfileStatistics::class.java)

                    // Update memory cache
                    memoryCache[cacheKey] = CacheEntry(statistics, timestamp) as CacheEntry<Any>

                    return statistics
                }
            }

            null
        } catch (e: JsonSyntaxException) {
            // Invalid cached data, remove it
            invalidateStatisticsCache(userId)
            null
        } catch (e: Exception) {
            throw ProfileError.CacheError("get_cached_statistics: ${e.message}")
        }
    }

    override suspend fun invalidateCache(userId: String) {
        try {
            invalidateProfileCache(userId)
            invalidateStatisticsCache(userId)
        } catch (e: Exception) {
            throw ProfileError.CacheError("invalidate_cache: ${e.message}")
        }
    }

    override suspend fun clearExpiredCache() {
        try {
            val currentTime = System.currentTimeMillis()

            // Clear expired memory cache entries
            val expiredKeys = memoryCache.entries
                .filter { isExpired(it.value.timestamp) }
                .map { it.key }

            expiredKeys.forEach { memoryCache.remove(it) }

            // Clear expired persistent cache entries
            val preferences = dataStore.data.first()
            val keysToRemove = mutableListOf<Preferences.Key<String>>()

            preferences.asMap().forEach { (key, value) ->
                if (key.name.endsWith("_timestamp")) {
                    val timestamp = (value as? String)?.toLongOrNull()
                    if (timestamp != null && isExpired(timestamp)) {
                        val baseKey = key.name.removeSuffix("_timestamp")
                        keysToRemove.add(stringPreferencesKey("${baseKey}_timestamp"))
                        keysToRemove.add(stringPreferencesKey("${baseKey}_data"))
                    }
                }
            }

            if (keysToRemove.isNotEmpty()) {
                dataStore.edit { prefs ->
                    keysToRemove.forEach { key ->
                        prefs.remove(key)
                    }
                }
            }
        } catch (e: Exception) {
            throw ProfileError.CacheError("clear_expired_cache: ${e.message}")
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    suspend fun getCacheStatistics(): CacheStatistics {
        return try {
            val preferences = dataStore.data.first()
            val persistentEntries = preferences.asMap().keys
                .filter { it.name.endsWith("_data") }
                .size

            CacheStatistics(
                memoryEntries = memoryCache.size,
                persistentEntries = persistentEntries,
                totalSizeBytes = estimateCacheSize()
            )
        } catch (e: Exception) {
            CacheStatistics(0, 0, 0)
        }
    }

    /**
     * Check if cache entry is expired
     */
    private fun isExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }

    /**
     * Clean up old entries from memory cache
     */
    private fun cleanupOldEntries() {
        val currentTime = System.currentTimeMillis()
        val entriesToRemove = memoryCache.entries
            .sortedBy { it.value.timestamp }
            .take(memoryCache.size - MAX_CACHE_SIZE + 10) // Remove extra entries

        entriesToRemove.forEach { (key, _) ->
            memoryCache.remove(key)
        }
    }

    /**
     * Invalidate profile cache for specific user
     */
    private suspend fun invalidateProfileCache(userId: String) {
        val cacheKey = "profile_$userId"
        memoryCache.remove(cacheKey)

        val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
        val dataKey = stringPreferencesKey("${cacheKey}_data")

        dataStore.edit { preferences ->
            preferences.remove(timestampKey)
            preferences.remove(dataKey)
        }
    }

    /**
     * Invalidate statistics cache for specific user
     */
    private suspend fun invalidateStatisticsCache(userId: String) {
        val cacheKey = "statistics_$userId"
        memoryCache.remove(cacheKey)

        val timestampKey = stringPreferencesKey("${cacheKey}_timestamp")
        val dataKey = stringPreferencesKey("${cacheKey}_data")

        dataStore.edit { preferences ->
            preferences.remove(timestampKey)
            preferences.remove(dataKey)
        }
    }

    /**
     * Estimate total cache size in bytes
     */
    private fun estimateCacheSize(): Long {
        return try {
            memoryCache.values.sumOf { entry ->
                gson.toJson(entry.data).length.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Cache entry wrapper with timestamp
     */
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )
}

/**
 * Cache statistics for monitoring
 */
data class CacheStatistics(
    val memoryEntries: Int,
    val persistentEntries: Int,
    val totalSizeBytes: Long
)
