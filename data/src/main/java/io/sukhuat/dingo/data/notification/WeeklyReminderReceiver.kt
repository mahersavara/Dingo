package io.sukhuat.dingo.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.sukhuat.dingo.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast receiver for handling weekly reminder notifications
 */
@AndroidEntryPoint
class WeeklyReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var goalRepository: GoalRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                // Get the count of active goals
                val activeGoals = goalRepository.getAllGoals().first()
                val activeGoalsCount = activeGoals.filter { it.status == io.sukhuat.dingo.domain.model.GoalStatus.ACTIVE }.size

                // Show the weekly reminder notification
                notificationService.showWeeklyReminderNotification(activeGoalsCount)
            } catch (e: Exception) {
                // Log error or handle gracefully
                // In a production app, you might want to use a proper logging framework
            }
        }
    }
}
