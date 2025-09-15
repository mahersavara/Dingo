package io.sukhuat.dingo.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.persistence.EnhancedWidgetDataLoader
import io.sukhuat.dingo.widget.persistence.WidgetDataMigration
import io.sukhuat.dingo.widget.persistence.WidgetPersistenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced widget initializer that coordinates the setup of the improved widget system
 * This class should be called during app startup to initialize the enhanced widget persistence
 */
@Singleton
class EnhancedWidgetInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val persistenceManager: WidgetPersistenceManager,
    private val enhancedDataLoader: EnhancedWidgetDataLoader,
    private val widgetDataMigration: WidgetDataMigration,
    private val enhancedUpdater: EnhancedWidgetDataUpdater
) {
    private val initializationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Initialize the enhanced widget system
     * Call this during app startup (e.g., in Application.onCreate())
     */
    fun initialize() {
        initializationScope.launch {
            try {
                android.util.Log.d("EnhancedWidgetInitializer", "🚀 Initializing enhanced widget system")

                // Step 1: Check migration status and migrate if needed
                val migrationStatus = widgetDataMigration.getMigrationStatus()
                android.util.Log.d("EnhancedWidgetInitializer", "📊 Migration status: completed=${migrationStatus.isCompleted}, version=${migrationStatus.currentVersion}/${migrationStatus.targetVersion}")

                if (!migrationStatus.isCompleted) {
                    // Backup current data before migration
                    val backupResult = widgetDataMigration.backupCurrentData()
                    when (backupResult) {
                        is WidgetDataMigration.BackupResult.Success -> {
                            android.util.Log.d("EnhancedWidgetInitializer", "💾 Data backup successful")
                        }
                        is WidgetDataMigration.BackupResult.Error -> {
                            android.util.Log.w("EnhancedWidgetInitializer", "⚠️ Data backup failed: ${backupResult.message}")
                        }
                    }

                    // Perform migration
                    val migrationResult = widgetDataMigration.migrateIfNeeded()
                    when (migrationResult) {
                        is WidgetDataMigration.MigrationResult.Success -> {
                            android.util.Log.d("EnhancedWidgetInitializer", "✅ Migration successful: ${migrationResult.message}")

                            // Clean up old data after successful migration
                            if (widgetDataMigration.cleanupOldData()) {
                                android.util.Log.d("EnhancedWidgetInitializer", "🧹 Old data cleanup completed")
                            }
                        }
                        is WidgetDataMigration.MigrationResult.Error -> {
                            android.util.Log.e("EnhancedWidgetInitializer", "❌ Migration failed: ${migrationResult.message}")
                        }
                    }
                }

                // Step 2: Validate widget configuration
                validateWidgetConfiguration()

                // Step 3: Preload data for better performance
                preloadWidgetData()

                // Step 4: Initialize widget update scheduling
                scheduleWidgetUpdates()

                android.util.Log.d("EnhancedWidgetInitializer", "✅ Enhanced widget system initialization completed")
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetInitializer", "❌ Widget system initialization failed", e)
            }
        }
    }

    private suspend fun validateWidgetConfiguration() {
        try {
            val config = persistenceManager.getWidgetConfigurationSync()
            android.util.Log.d("EnhancedWidgetInitializer", "⚙️ Widget config: size=${config.widgetSize}, autoUpdate=${config.autoUpdateEnabled}, interval=${config.updateIntervalMinutes}min")

            // Validate configuration values
            val validatedConfig = config.copy(
                updateIntervalMinutes = config.updateIntervalMinutes.coerceIn(5, 120), // 5 minutes to 2 hours
                widgetSize = if (config.widgetSize in listOf("2x3", "3x2", "auto")) config.widgetSize else "2x3"
            )

            // Save validated configuration if changes were made
            if (validatedConfig != config) {
                persistenceManager.saveWidgetConfiguration(validatedConfig)
                android.util.Log.d("EnhancedWidgetInitializer", "🔧 Widget configuration validated and updated")
            }
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetInitializer", "❌ Configuration validation failed", e)

            // Save default configuration as fallback
            val defaultConfig = WidgetPersistenceManager.WidgetConfiguration()
            persistenceManager.saveWidgetConfiguration(defaultConfig)
        }
    }

    private suspend fun preloadWidgetData() {
        try {
            val config = persistenceManager.getWidgetConfigurationSync()
            if (config.autoUpdateEnabled) {
                android.util.Log.d("EnhancedWidgetInitializer", "🚀 Starting widget data preload")
                enhancedDataLoader.preloadData()
                android.util.Log.d("EnhancedWidgetInitializer", "✅ Widget data preload completed")
            } else {
                android.util.Log.d("EnhancedWidgetInitializer", "⏸️ Auto-update disabled, skipping data preload")
            }
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetInitializer", "❌ Data preload failed", e)
        }
    }

    private fun scheduleWidgetUpdates() {
        try {
            // This would integrate with your existing WorkManager scheduling
            // For now, just trigger an initial update
            enhancedUpdater.updateAllWidgets(forceRefresh = false)
            android.util.Log.d("EnhancedWidgetInitializer", "⏰ Widget update scheduling initialized")
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetInitializer", "❌ Widget update scheduling failed", e)
        }
    }

    /**
     * Get system health status for debugging
     */
    suspend fun getSystemHealthStatus(): WidgetSystemHealth {
        return try {
            val migrationStatus = widgetDataMigration.getMigrationStatus()
            val config = persistenceManager.getWidgetConfigurationSync()
            val cacheStats = enhancedDataLoader.getCacheStats()

            WidgetSystemHealth(
                isInitialized = true,
                migrationCompleted = migrationStatus.isCompleted,
                migrationVersion = migrationStatus.currentVersion,
                configurationValid = true,
                cacheHealthy = cacheStats["cacheValid"] as? Boolean ?: false,
                lastUpdateTime = System.currentTimeMillis(),
                errorCount = cacheStats["errorCount"] as? Int ?: 0,
                details = mapOf(
                    "autoUpdateEnabled" to config.autoUpdateEnabled,
                    "updateInterval" to config.updateIntervalMinutes,
                    "widgetSize" to config.widgetSize,
                    "cacheStats" to cacheStats
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetInitializer", "❌ Health status check failed", e)
            WidgetSystemHealth(
                isInitialized = false,
                migrationCompleted = false,
                migrationVersion = 0,
                configurationValid = false,
                cacheHealthy = false,
                lastUpdateTime = System.currentTimeMillis(),
                errorCount = 999,
                details = mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }

    /**
     * Force refresh all widgets (useful for debugging)
     */
    fun forceRefreshAllWidgets() {
        android.util.Log.d("EnhancedWidgetInitializer", "🔄 Force refreshing all widgets")
        enhancedUpdater.updateAllWidgets(forceRefresh = true)
    }

    /**
     * Clear all caches and reset widget data
     */
    fun resetWidgetData() {
        initializationScope.launch {
            try {
                android.util.Log.d("EnhancedWidgetInitializer", "🔄 Resetting widget data")

                enhancedUpdater.clearCache()

                // Reset to default configuration
                val defaultConfig = WidgetPersistenceManager.WidgetConfiguration()
                persistenceManager.saveWidgetConfiguration(defaultConfig)

                // Force update all widgets
                enhancedUpdater.updateAllWidgets(forceRefresh = true)

                android.util.Log.d("EnhancedWidgetInitializer", "✅ Widget data reset completed")
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetInitializer", "❌ Widget data reset failed", e)
            }
        }
    }

    data class WidgetSystemHealth(
        val isInitialized: Boolean,
        val migrationCompleted: Boolean,
        val migrationVersion: Int,
        val configurationValid: Boolean,
        val cacheHealthy: Boolean,
        val lastUpdateTime: Long,
        val errorCount: Int,
        val details: Map<String, Any>
    )
}
