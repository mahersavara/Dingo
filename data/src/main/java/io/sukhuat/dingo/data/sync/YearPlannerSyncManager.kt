package io.sukhuat.dingo.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import io.sukhuat.dingo.data.cache.YearPlannerCacheManager
import io.sukhuat.dingo.data.remote.FirebaseYearPlannerService
import io.sukhuat.dingo.domain.model.yearplanner.SyncStatus
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "YearPlannerSyncManager"
private const val SYNC_RETRY_DELAY = 5000L // 5 seconds
private const val MAX_RETRY_ATTEMPTS = 3

/**
 * Manages synchronization between local cache and Firebase for Year Planner data
 * Handles offline/online scenarios with automatic retry logic
 */
@Singleton
class YearPlannerSyncManager @Inject constructor(
    private val firebaseService: FirebaseYearPlannerService,
    private val cacheManager: YearPlannerCacheManager,
    private val context: Context
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Get year plan with offline support
     * Returns cached data if offline, Firebase data if online
     */
    fun getYearPlanWithSync(year: Int): Flow<YearPlan?> {
        return if (isNetworkAvailable()) {
            // Online: Get from Firebase and cache the result
            firebaseService.getYearPlan(year)
        } else {
            // Offline: Return cached data
            flowOf(null) // Will be handled by repository to return cached data
        }
    }
    
    /**
     * Save year plan with sync management
     * Caches locally if offline, syncs when online
     */
    suspend fun saveYearPlanWithSync(yearPlan: YearPlan): Boolean {
        return if (isNetworkAvailable()) {
            // Online: Save to Firebase and cache
            val success = firebaseService.saveYearPlan(yearPlan)
            if (success) {
                cacheManager.cacheYearPlan(yearPlan.markAsSynced())
            } else {
                // Save locally if Firebase fails
                cacheManager.cacheYearPlan(yearPlan.markSyncError())
            }
            success
        } else {
            // Offline: Save to cache with pending status
            cacheManager.cacheYearPlan(yearPlan.markOffline())
            // Schedule sync when online
            scheduleSyncWhenOnline()
            true // Return success for offline save
        }
    }
    
    /**
     * Update month content with sync management
     */
    suspend fun updateMonthContentWithSync(year: Int, monthIndex: Int, content: String): Boolean {
        return if (isNetworkAvailable()) {
            // Online: Update Firebase
            val success = firebaseService.updateMonthContent(year, monthIndex, content)
            if (!success) {
                // TODO: Cache the pending update for later sync
                Log.w(TAG, "Failed to update month content online, should cache for later sync")
            }
            success
        } else {
            // Offline: Cache the update
            Log.d(TAG, "Offline: Caching month content update for year $year, month $monthIndex")
            // TODO: Implement pending updates cache
            true
        }
    }
    
    /**
     * Force sync of all pending data
     */
    suspend fun forceSyncPendingData(): Boolean {
        if (!isNetworkAvailable()) {
            Log.w(TAG, "Cannot force sync: No network connection")
            return false
        }
        
        return try {
            val cachedYears = cacheManager.getCachedYears()
            var allSynced = true
            
            for (year in cachedYears) {
                val cachedYearPlan = cacheManager.getCachedYearPlan(year)
                if (cachedYearPlan != null && cachedYearPlan.syncStatus != SyncStatus.SYNCED) {
                    val success = syncYearPlan(cachedYearPlan)
                    if (!success) {
                        allSynced = false
                        Log.w(TAG, "Failed to sync year plan for year $year")
                    }
                }
            }
            
            allSynced
        } catch (e: Exception) {
            Log.e(TAG, "Error during force sync", e)
            false
        }
    }
    
    /**
     * Sync a single year plan to Firebase
     */
    private suspend fun syncYearPlan(yearPlan: YearPlan): Boolean {
        var attempts = 0
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                val success = firebaseService.saveYearPlan(yearPlan)
                if (success) {
                    // Update cache with synced status
                    cacheManager.cacheYearPlan(yearPlan.markAsSynced())
                    Log.d(TAG, "Successfully synced year plan for year ${yearPlan.year}")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync attempt ${attempts + 1} failed for year ${yearPlan.year}", e)
            }
            
            attempts++
            if (attempts < MAX_RETRY_ATTEMPTS) {
                delay(SYNC_RETRY_DELAY * attempts) // Exponential backoff
            }
        }
        
        // Mark as sync error after all attempts failed
        cacheManager.cacheYearPlan(yearPlan.markSyncError())
        return false
    }
    
    /**
     * Schedule sync when network becomes available
     */
    private fun scheduleSyncWhenOnline() {
        syncScope.launch {
            // Simple polling approach - in production, use NetworkCallback
            while (!isNetworkAvailable()) {
                delay(SYNC_RETRY_DELAY)
            }
            
            Log.d(TAG, "Network available, starting sync of pending data")
            forceSyncPendingData()
        }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }
    
    /**
     * Get sync status for a year
     */
    suspend fun getSyncStatus(year: Int): SyncStatus {
        return if (isNetworkAvailable()) {
            SyncStatus.SYNCED
        } else {
            val cachedYearPlan = cacheManager.getCachedYearPlan(year)
            cachedYearPlan?.syncStatus ?: SyncStatus.OFFLINE
        }
    }
}