package io.sukhuat.dingo.widget.persistence

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.widget.WidgetDataLoader
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migration utility to transition from the old widget data system to the enhanced persistence system
 * Ensures backward compatibility and smooth data transfer
 */
@Singleton
class WidgetDataMigration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val persistenceManager: WidgetPersistenceManager,
    private val originalDataLoader: WidgetDataLoader
) {
    private val gson = Gson()

    companion object {
        private const val MIGRATION_PREFS = "widget_migration_state"
        private const val MIGRATION_VERSION_KEY = "migration_version"
        private const val CURRENT_MIGRATION_VERSION = 1

        // Old system preference files
        private const val OLD_WIDGET_PREFS = "widget_goals_sync"
        private const val OLD_WEEK_OFFSET_PREFS = "widget_week_offsets"
        private const val OLD_CONFIG_PREFS = "widget_config"
    }

    /**
     * Check if migration is needed and perform it
     */
    suspend fun migrateIfNeeded(): MigrationResult = withContext(Dispatchers.IO) {
        try {
            val migrationPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
            val currentVersion = migrationPrefs.getInt(MIGRATION_VERSION_KEY, 0)

            if (currentVersion >= CURRENT_MIGRATION_VERSION) {
                android.util.Log.d("WidgetDataMigration", "✅ Migration not needed (version: $currentVersion)")
                return@withContext MigrationResult.Success("Already migrated")
            }

            android.util.Log.d("WidgetDataMigration", "🔄 Starting widget data migration (from version $currentVersion)")

            // Perform migration steps
            val steps = mutableListOf<String>()

            // Step 1: Migrate cached goals data
            if (migrateCachedGoalsData()) {
                steps.add("Cached goals data migrated")
            }

            // Step 2: Migrate widget configurations
            if (migrateWidgetConfigurations()) {
                steps.add("Widget configurations migrated")
            }

            // Step 3: Migrate widget states and week offsets
            if (migrateWidgetStates()) {
                steps.add("Widget states migrated")
            }

            // Step 4: Update migration version
            migrationPrefs.edit()
                .putInt(MIGRATION_VERSION_KEY, CURRENT_MIGRATION_VERSION)
                .putLong("migration_timestamp", System.currentTimeMillis())
                .apply()

            android.util.Log.d("WidgetDataMigration", "✅ Migration completed successfully")
            MigrationResult.Success("Migration completed: ${steps.joinToString(", ")}")
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Migration failed", e)
            MigrationResult.Error("Migration failed: ${e.message}")
        }
    }

    private suspend fun migrateCachedGoalsData(): Boolean {
        return try {
            val oldPrefs = context.getSharedPreferences(OLD_WIDGET_PREFS, Context.MODE_PRIVATE)
            val cachedGoalsJson = oldPrefs.getString("cached_goals", null)
            val cacheTimestamp = oldPrefs.getLong("cache_timestamp", 0L)

            if (cachedGoalsJson != null && cacheTimestamp > 0) {
                val goalsType = object : TypeToken<List<WidgetGoal>>() {}.type
                val goals: List<WidgetGoal> = gson.fromJson(cachedGoalsJson, goalsType)

                // Migrate to new system
                val cachedData = WidgetPersistenceManager.CachedGoalData(
                    goals = goals,
                    weekOfYear = -1, // Not available in old system
                    year = -1,
                    timestamp = cacheTimestamp,
                    isStale = System.currentTimeMillis() - cacheTimestamp > 30 * 60 * 1000L,
                    errorCount = 0
                )

                persistenceManager.cacheGoals(cachedData)
                android.util.Log.d("WidgetDataMigration", "📦 Migrated ${goals.size} cached goals")
                true
            } else {
                android.util.Log.d("WidgetDataMigration", "📦 No cached goals to migrate")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Error migrating cached goals", e)
            false
        }
    }

    private suspend fun migrateWidgetConfigurations(): Boolean {
        return try {
            val oldPrefs = context.getSharedPreferences(OLD_CONFIG_PREFS, Context.MODE_PRIVATE)

            // Try to extract old configuration values
            val widgetSize = oldPrefs.getString("widget_size", "2x3") ?: "2x3"
            val showWeekNav = oldPrefs.getBoolean("show_week_navigation", true)
            val autoUpdate = oldPrefs.getBoolean("auto_update_enabled", true)
            val updateInterval = oldPrefs.getInt("update_interval_minutes", 15)
            val themeMode = oldPrefs.getString("theme_mode", "auto") ?: "auto"

            // Create new configuration
            val newConfig = WidgetPersistenceManager.WidgetConfiguration(
                widgetSize = widgetSize,
                showWeekNavigation = showWeekNav,
                autoUpdateEnabled = autoUpdate,
                updateIntervalMinutes = updateInterval,
                themeMode = themeMode,
                showCompletedGoals = true // Default value
            )

            persistenceManager.saveWidgetConfiguration(newConfig)
            android.util.Log.d("WidgetDataMigration", "⚙️ Migrated widget configuration")
            true
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Error migrating configuration", e)

            // Save default configuration
            val defaultConfig = WidgetPersistenceManager.WidgetConfiguration()
            persistenceManager.saveWidgetConfiguration(defaultConfig)
            true
        }
    }

    private suspend fun migrateWidgetStates(): Boolean {
        return try {
            var migratedCount = 0

            // Try to get widget IDs from Android system
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val widgetComponents = listOf(
                android.content.ComponentName(context, "io.sukhuat.dingo.widget.WeeklyGoalWidgetProvider"),
                android.content.ComponentName(context, "io.sukhuat.dingo.widget.WeeklyGoalWidget2x3Provider"),
                android.content.ComponentName(context, "io.sukhuat.dingo.widget.WeeklyGoalWidget3x2Provider")
            )

            for (component in widgetComponents) {
                try {
                    val widgetIds = appWidgetManager.getAppWidgetIds(component)
                    for (widgetId in widgetIds) {
                        // Try to get old week offset data
                        val weekOffset = getOldWeekOffset(widgetId)

                        // Create new widget state
                        val widgetState = WidgetPersistenceManager.WidgetState(
                            widgetId = widgetId,
                            weekOffset = weekOffset,
                            lastSelectedWeek = -1,
                            lastSelectedYear = -1,
                            isConfigured = true,
                            errorState = null
                        )

                        persistenceManager.saveWidgetState(widgetState)
                        migratedCount++
                    }
                } catch (e: Exception) {
                    android.util.Log.w("WidgetDataMigration", "Warning: Could not process component ${component.className}", e)
                }
            }

            android.util.Log.d("WidgetDataMigration", "🔧 Migrated $migratedCount widget states")
            true
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Error migrating widget states", e)
            false
        }
    }

    private fun getOldWeekOffset(widgetId: Int): Int {
        return try {
            // Check different possible locations for week offset data
            val offsetPrefs = context.getSharedPreferences(OLD_WEEK_OFFSET_PREFS, Context.MODE_PRIVATE)
            val offset1 = offsetPrefs.getInt("week_offset_$widgetId", 0)
            if (offset1 != 0) return offset1

            // Check DataStore keys that might have been used
            val mainPrefs = context.getSharedPreferences("widget_preferences", Context.MODE_PRIVATE)
            val offset2 = mainPrefs.getInt("widget_week_offset_$widgetId", 0)
            if (offset2 != 0) return offset2

            0 // Default offset
        } catch (e: Exception) {
            android.util.Log.w("WidgetDataMigration", "Could not get old week offset for widget $widgetId", e)
            0
        }
    }

    /**
     * Backup current data before migration
     */
    suspend fun backupCurrentData(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backup = mutableMapOf<String, Any>()

            // Backup old SharedPreferences data
            backupSharedPreferences(OLD_WIDGET_PREFS, backup)
            backupSharedPreferences(OLD_CONFIG_PREFS, backup)
            backupSharedPreferences(OLD_WEEK_OFFSET_PREFS, backup)

            // Save backup to file
            val backupJson = gson.toJson(backup)
            val backupPrefs = context.getSharedPreferences("widget_backup", Context.MODE_PRIVATE)
            backupPrefs.edit()
                .putString("backup_data", backupJson)
                .putLong("backup_timestamp", System.currentTimeMillis())
                .apply()

            android.util.Log.d("WidgetDataMigration", "💾 Data backup completed")
            BackupResult.Success("Backup saved successfully")
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Backup failed", e)
            BackupResult.Error("Backup failed: ${e.message}")
        }
    }

    private fun backupSharedPreferences(prefsName: String, backup: MutableMap<String, Any>) {
        try {
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val prefsMap = prefs.all
            if (prefsMap.isNotEmpty()) {
                backup[prefsName] = prefsMap
            }
        } catch (e: Exception) {
            android.util.Log.w("WidgetDataMigration", "Could not backup preferences: $prefsName", e)
        }
    }

    /**
     * Clean up old data after successful migration
     */
    suspend fun cleanupOldData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Only clean up if migration was successful
            val migrationPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
            val migrationVersion = migrationPrefs.getInt(MIGRATION_VERSION_KEY, 0)

            if (migrationVersion < CURRENT_MIGRATION_VERSION) {
                android.util.Log.w("WidgetDataMigration", "⚠️ Skipping cleanup - migration not completed")
                return@withContext false
            }

            // Clear old preference files
            val oldPrefFiles = listOf(OLD_WIDGET_PREFS, OLD_CONFIG_PREFS, OLD_WEEK_OFFSET_PREFS)
            for (prefsName in oldPrefFiles) {
                try {
                    val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                } catch (e: Exception) {
                    android.util.Log.w("WidgetDataMigration", "Could not clear preferences: $prefsName", e)
                }
            }

            android.util.Log.d("WidgetDataMigration", "🧹 Old data cleanup completed")
            true
        } catch (e: Exception) {
            android.util.Log.e("WidgetDataMigration", "❌ Cleanup failed", e)
            false
        }
    }

    /**
     * Get migration status
     */
    fun getMigrationStatus(): MigrationStatus {
        val migrationPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        val currentVersion = migrationPrefs.getInt(MIGRATION_VERSION_KEY, 0)
        val timestamp = migrationPrefs.getLong("migration_timestamp", 0L)

        return MigrationStatus(
            currentVersion = currentVersion,
            targetVersion = CURRENT_MIGRATION_VERSION,
            isCompleted = currentVersion >= CURRENT_MIGRATION_VERSION,
            timestamp = timestamp
        )
    }

    sealed class MigrationResult {
        data class Success(val message: String) : MigrationResult()
        data class Error(val message: String) : MigrationResult()
    }

    sealed class BackupResult {
        data class Success(val message: String) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    data class MigrationStatus(
        val currentVersion: Int,
        val targetVersion: Int,
        val isCompleted: Boolean,
        val timestamp: Long
    )
}
