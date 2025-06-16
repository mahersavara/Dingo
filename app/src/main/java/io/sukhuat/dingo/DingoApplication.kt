package io.sukhuat.dingo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sukhuat.dingo.data.sync.SyncManager
import javax.inject.Inject

/**
 * Application class for the Dingo app
 * It initializes the SyncManager to handle data synchronization
 */
@HiltAndroidApp
class DingoApplication : Application() {
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Start observing network connectivity for data sync
        syncManager.startSyncObserver()
    }
}
