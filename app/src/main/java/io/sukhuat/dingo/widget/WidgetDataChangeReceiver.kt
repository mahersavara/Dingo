package io.sukhuat.dingo.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Broadcast receiver to handle goal data changes and update widgets accordingly
 */
@AndroidEntryPoint
class WidgetDataChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var simpleWidgetUpdater: SimpleWidgetUpdater

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("WidgetDataChangeReceiver", "🎯 Broadcast received: ${intent.action}")

        when (intent.action) {
            ACTION_GOAL_CREATED,
            ACTION_GOAL_UPDATED,
            ACTION_GOAL_DELETED,
            ACTION_GOAL_STATUS_CHANGED -> {
                android.util.Log.d("WidgetDataChangeReceiver", "⚡ Goal data changed - updating widgets immediately")

                // Use simplified widget updater for instant response
                simpleWidgetUpdater.updateOnDataChange()
                android.util.Log.d("WidgetDataChangeReceiver", "✅ Simplified widget update completed")
            }
            ACTION_WEEK_CHANGED -> {
                android.util.Log.d("WidgetDataChangeReceiver", "📅 Week changed - force updating widgets")

                // Use simplified widget updater for week change
                simpleWidgetUpdater.updateOnDataChange()
                android.util.Log.d("WidgetDataChangeReceiver", "✅ Week change widget update completed")
            }
        }
    }

    companion object {
        const val ACTION_GOAL_CREATED = "io.sukhuat.dingo.GOAL_CREATED"
        const val ACTION_GOAL_UPDATED = "io.sukhuat.dingo.GOAL_UPDATED"
        const val ACTION_GOAL_DELETED = "io.sukhuat.dingo.GOAL_DELETED"
        const val ACTION_GOAL_STATUS_CHANGED = "io.sukhuat.dingo.GOAL_STATUS_CHANGED"
        const val ACTION_WEEK_CHANGED = "io.sukhuat.dingo.WEEK_CHANGED"

        /**
         * Send broadcast to update widgets when goal data changes
         */
        fun notifyGoalCreated(context: Context, goalId: String) {
            val intent = Intent(ACTION_GOAL_CREATED).apply {
                putExtra("goal_id", goalId)
                setClass(context, WidgetDataChangeReceiver::class.java)
            }
            android.util.Log.d("WidgetDataChangeReceiver", "📡 Sending GOAL_CREATED broadcast for goalId: $goalId")
            context.sendBroadcast(intent)
        }

        fun notifyGoalUpdated(context: Context, goalId: String) {
            val intent = Intent(ACTION_GOAL_UPDATED).apply {
                putExtra("goal_id", goalId)
                setClass(context, WidgetDataChangeReceiver::class.java)
            }
            android.util.Log.d("WidgetDataChangeReceiver", "📡 Sending GOAL_UPDATED broadcast for goalId: $goalId")
            context.sendBroadcast(intent)
        }

        fun notifyGoalDeleted(context: Context, goalId: String) {
            val intent = Intent(ACTION_GOAL_DELETED).apply {
                putExtra("goal_id", goalId)
                setClass(context, WidgetDataChangeReceiver::class.java)
            }
            android.util.Log.d("WidgetDataChangeReceiver", "📡 Sending GOAL_DELETED broadcast for goalId: $goalId")
            context.sendBroadcast(intent)
        }

        fun notifyGoalStatusChanged(context: Context, goalId: String, newStatus: String) {
            val intent = Intent(ACTION_GOAL_STATUS_CHANGED).apply {
                putExtra("goal_id", goalId)
                putExtra("new_status", newStatus)
                setClass(context, WidgetDataChangeReceiver::class.java)
            }
            android.util.Log.d("WidgetDataChangeReceiver", "📡 Sending GOAL_STATUS_CHANGED broadcast for goalId: $goalId, status: $newStatus")
            context.sendBroadcast(intent)
        }

        fun notifyWeekChanged(context: Context) {
            val intent = Intent(ACTION_WEEK_CHANGED).apply {
                setClass(context, WidgetDataChangeReceiver::class.java)
            }
            android.util.Log.d("WidgetDataChangeReceiver", "📡 Sending WEEK_CHANGED broadcast")
            context.sendBroadcast(intent)
        }
    }
}
