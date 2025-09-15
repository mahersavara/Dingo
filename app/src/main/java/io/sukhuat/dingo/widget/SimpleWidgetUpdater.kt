package io.sukhuat.dingo.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple widget updater following favorite-apps-widget patterns
 * Provides straightforward widget updates without complex optimization logic
 */
@Singleton
class SimpleWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val updateScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Update all widgets - simple and direct approach
     */
    fun updateAllWidgets() {
        android.util.Log.d("SimpleWidgetUpdater", "🔄 Updating all widgets")

        updateScope.launch {
            try {
                // Update the single widget using Glance updateAll
                WeeklyGoalWidget.updateAll(context)

                android.util.Log.d("SimpleWidgetUpdater", "✅ Widget updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("SimpleWidgetUpdater", "❌ Error updating widget", e)
            }
        }
    }

    /**
     * Update widgets when app goes to background
     * This ensures widgets show latest data when user is on home screen
     */
    fun updateOnAppBackground() {
        android.util.Log.d("SimpleWidgetUpdater", "📱 App backgrounded - updating widgets")
        updateAllWidgets()
    }

    /**
     * Update widgets when data changes
     * Called when goals are modified in the app
     */
    fun updateOnDataChange() {
        android.util.Log.d("SimpleWidgetUpdater", "💾 Data changed - updating widgets")
        updateAllWidgets()
    }

    companion object {
        /**
         * Static method for updating widgets from broadcast receivers
         */
        fun updateWidgetsStatic(context: Context) {
            android.util.Log.d("SimpleWidgetUpdater", "📡 Static widget update requested")

            val updateScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            updateScope.launch {
                try {
                    WeeklyGoalWidget.updateAll(context)
                    android.util.Log.d("SimpleWidgetUpdater", "✅ Static widget update completed")
                } catch (e: Exception) {
                    android.util.Log.e("SimpleWidgetUpdater", "❌ Static widget update failed", e)
                }
            }
        }
    }
}
