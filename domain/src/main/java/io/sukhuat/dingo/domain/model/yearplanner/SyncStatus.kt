package io.sukhuat.dingo.domain.model.yearplanner

/**
 * Sync status for year planner data
 */
enum class SyncStatus {
    /**
     * Data is synchronized with Firebase
     */
    SYNCED,

    /**
     * Data has local changes pending sync to Firebase
     */
    PENDING,

    /**
     * Sync error occurred, will retry automatically
     */
    ERROR,

    /**
     * Device is offline, data will sync when connection is restored
     */
    OFFLINE
}
