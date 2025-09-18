package io.sukhuat.dingo.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.service.WidgetNotificationService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WidgetNotificationService that directly updates widgets
 */
@Singleton
class WidgetNotificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widgetUpdateBroadcaster: WidgetUpdateBroadcaster,
    private val widgetUpdateScheduler: WidgetUpdateScheduler
) : WidgetNotificationService {

    private val TAG = "WidgetNotificationServiceImpl"

    init {
        android.util.Log.d(TAG, "🏗️ WidgetNotificationServiceImpl initialized with broadcaster: $widgetUpdateBroadcaster")
    }

    /**
     * Schedule immediate widget update with high priority cache refresh
     */
    private fun scheduleImmediateWidgetRefresh(operation: String, fallbackBroadcast: () -> Unit) {
        android.util.Log.d(TAG, "🎯 $operation - scheduling immediate widget refresh with data sync")

        try {
            // 1. Schedule high-priority WorkManager task for immediate data refresh
            // The WorkManager task will handle data fetching and cache updates
            widgetUpdateScheduler.scheduleImmediateUpdate()
            android.util.Log.d(TAG, "✅ Immediate refresh WorkManager task scheduled for $operation")

            // 2. Also trigger direct widget update as backup
            updateWidgetsWithAllMethods(operation, fallbackBroadcast)

        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to schedule immediate refresh for $operation", e)
            // Fall back to regular widget update
            updateWidgetsWithAllMethods(operation, fallbackBroadcast)
        }
    }

    private fun updateWidgetsWithAllMethods(operation: String, fallbackBroadcast: () -> Unit) {
        android.util.Log.d(TAG, "🎯 $operation - starting triple widget update approach")

        // Triple approach: Direct update + WorkManager + Broadcast
        try {
            // 1. Direct widget update (immediate)
            widgetUpdateBroadcaster.updateWidgets(context)
            android.util.Log.d(TAG, "✅ Direct widgets update called for $operation")

            // 2. Schedule via WorkManager as backup (immediate)
            widgetUpdateScheduler.scheduleImmediateUpdate()
            android.util.Log.d(TAG, "✅ WorkManager backup scheduled for $operation")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed primary widget updates for $operation", e)
        } finally {
            // 3. Always send broadcast as final fallback
            try {
                fallbackBroadcast()
                android.util.Log.d(TAG, "✅ Broadcast fallback sent for $operation")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "❌ Even broadcast fallback failed for $operation", e)
            }
        }
    }

    override fun notifyGoalCreated(goalId: String) {
        scheduleImmediateWidgetRefresh("Goal Created ($goalId)") {
            WidgetDataChangeReceiver.notifyGoalCreated(context, goalId)
        }
    }

    override fun notifyGoalUpdated(goalId: String) {
        scheduleImmediateWidgetRefresh("Goal Updated ($goalId)") {
            WidgetDataChangeReceiver.notifyGoalUpdated(context, goalId)
        }
    }

    override fun notifyGoalDeleted(goalId: String) {
        scheduleImmediateWidgetRefresh("Goal Deleted ($goalId)") {
            WidgetDataChangeReceiver.notifyGoalDeleted(context, goalId)
        }
    }

    override fun notifyGoalStatusChanged(goalId: String, newStatus: String) {
        scheduleImmediateWidgetRefresh("Goal Status Changed ($goalId -> $newStatus)") {
            WidgetDataChangeReceiver.notifyGoalStatusChanged(context, goalId, newStatus)
        }
    }

    override fun notifyWeekChanged() {
        updateWidgetsWithAllMethods("Week Changed") {
            WidgetDataChangeReceiver.notifyWeekChanged(context)
        }
    }
}
