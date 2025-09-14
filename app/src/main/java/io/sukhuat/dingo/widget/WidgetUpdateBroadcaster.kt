package io.sukhuat.dingo.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Helper class to broadcast widget updates
 */
class WidgetUpdateBroadcaster @Inject constructor() {

    /**
     * Update all widgets directly using Glance
     */
    fun updateWidgets(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                android.util.Log.d("WidgetUpdateBroadcaster", "🔄 Updating ALL widgets...")

                // Update all three widget sizes
                WeeklyGoalWidget.updateAll(context)
                WeeklyGoalWidget2x3.updateAll(context)
                WeeklyGoalWidget3x2.updateAll(context)

                android.util.Log.d("WidgetUpdateBroadcaster", "✅ All widgets updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("WidgetUpdateBroadcaster", "❌ Failed to update widgets", e)
            }
        }
    }
}
