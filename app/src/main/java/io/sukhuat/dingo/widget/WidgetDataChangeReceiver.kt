package io.sukhuat.dingo.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast receiver to handle goal data changes and update widgets accordingly
 */
@AndroidEntryPoint
class WidgetDataChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var widgetUpdateScheduler: WidgetUpdateScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_GOAL_CREATED,
            ACTION_GOAL_UPDATED,
            ACTION_GOAL_DELETED,
            ACTION_GOAL_STATUS_CHANGED -> {
                // Schedule immediate widget update when goal data changes
                CoroutineScope(Dispatchers.IO).launch {
                    widgetUpdateScheduler.scheduleImmediateUpdate()
                }
            }
            ACTION_WEEK_CHANGED -> {
                // Force update when week changes (e.g., at midnight Sunday)
                CoroutineScope(Dispatchers.IO).launch {
                    widgetUpdateScheduler.forceUpdateNow()
                }
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
            }
            context.sendBroadcast(intent)
        }

        fun notifyGoalUpdated(context: Context, goalId: String) {
            val intent = Intent(ACTION_GOAL_UPDATED).apply {
                putExtra("goal_id", goalId)
            }
            context.sendBroadcast(intent)
        }

        fun notifyGoalDeleted(context: Context, goalId: String) {
            val intent = Intent(ACTION_GOAL_DELETED).apply {
                putExtra("goal_id", goalId)
            }
            context.sendBroadcast(intent)
        }

        fun notifyGoalStatusChanged(context: Context, goalId: String, newStatus: String) {
            val intent = Intent(ACTION_GOAL_STATUS_CHANGED).apply {
                putExtra("goal_id", goalId)
                putExtra("new_status", newStatus)
            }
            context.sendBroadcast(intent)
        }

        fun notifyWeekChanged(context: Context) {
            val intent = Intent(ACTION_WEEK_CHANGED)
            context.sendBroadcast(intent)
        }
    }
}
