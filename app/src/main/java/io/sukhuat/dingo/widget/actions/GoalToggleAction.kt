package io.sukhuat.dingo.widget.actions

import android.content.Context
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.widget.WeeklyGoalWidget
import io.sukhuat.dingo.widget.WeeklyGoalWidget2x3
import io.sukhuat.dingo.widget.WeeklyGoalWidget3x2
import io.sukhuat.dingo.widget.WeeklyGoalWidgetEntryPoint
import io.sukhuat.dingo.widget.WidgetDataChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Action callback for toggling goal completion status from widget
 */
class GoalToggleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val goalId = parameters[GOAL_ID_KEY] ?: return
        val widgetSize = parameters[WIDGET_SIZE_KEY] ?: "2x2"

        withContext(Dispatchers.IO) {
            try {
                // Get repository through Hilt entry point
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WeeklyGoalWidgetEntryPoint::class.java
                )
                val goalRepository = entryPoint.getGoalRepository()

                // Get current goal
                val allGoals = goalRepository.getAllGoals().first()
                val currentGoal = allGoals.find { it.id == goalId } ?: return@withContext

                // Toggle status between ACTIVE and COMPLETED
                val newStatus = when (currentGoal.status) {
                    GoalStatus.ACTIVE -> GoalStatus.COMPLETED
                    GoalStatus.COMPLETED -> GoalStatus.ACTIVE
                    else -> return@withContext // Don't toggle FAILED or ARCHIVED goals
                }

                // Update goal status
                val updatedGoal = currentGoal.copy(status = newStatus)
                goalRepository.updateGoal(updatedGoal)

                // Show feedback toast
                withContext(Dispatchers.Main) {
                    val message = when (newStatus) {
                        GoalStatus.COMPLETED -> "Goal completed! 🎉"
                        GoalStatus.ACTIVE -> "Goal reactivated"
                        else -> ""
                    }
                    if (message.isNotEmpty()) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }

                // Notify widget data changed
                WidgetDataChangeReceiver.notifyGoalStatusChanged(
                    context,
                    goalId,
                    newStatus.name
                )

                // Update the specific widget
                when (widgetSize) {
                    "2x2" -> WeeklyGoalWidget.update(context, glanceId)
                    "2x3" -> WeeklyGoalWidget2x3.update(context, glanceId)
                    "3x2" -> WeeklyGoalWidget3x2.update(context, glanceId)
                }
            } catch (e: Exception) {
                // Show error toast
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Failed to update goal. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        val GOAL_ID_KEY = ActionParameters.Key<String>("goal_id")
        val WIDGET_SIZE_KEY = ActionParameters.Key<String>("widget_size")
    }
}
