package io.sukhuat.dingo.data.sync

import android.util.Log
import io.sukhuat.dingo.data.repository.GoalRepositoryImpl
import io.sukhuat.dingo.data.util.ConnectionStatus
import io.sukhuat.dingo.data.util.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SyncManager"

/**
 * Manages synchronization between local database and Firebase
 * It observes network connectivity and triggers sync when the network becomes available
 * Note: With the migration to Firebase-only, this class now primarily logs connectivity changes
 * as synchronization is handled automatically by Firebase
 */
@Singleton
class SyncManager @Inject constructor(
    private val goalRepository: GoalRepositoryImpl,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isSyncObserverActive = false

    /**
     * Start observing network connectivity and log when network becomes available
     */
    fun startSyncObserver() {
        if (isSyncObserverActive) return

        isSyncObserverActive = true

        syncScope.launch {
            try {
                networkConnectivityObserver.observe()
                    .distinctUntilChanged()
                    .collectLatest { status ->
                        when (status) {
                            ConnectionStatus.AVAILABLE -> {
                                Log.d(TAG, "Network connection available")
                                // Firebase handles sync automatically
                            }
                            ConnectionStatus.UNAVAILABLE -> {
                                Log.d(TAG, "Network connection unavailable")
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in network observer", e)
                isSyncObserverActive = false
            }
        }
    }

    /**
     * This method is now a no-op as Firebase handles synchronization automatically
     * Kept for backward compatibility with existing code
     */
    fun syncData() {
        Log.d(TAG, "syncData called - no action needed with Firebase implementation")
        // Firebase handles synchronization automatically
    }
}
