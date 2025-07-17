package io.sukhuat.dingo.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling app notifications
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {

    companion object {
        private const val CHANNEL_ID_GENERAL = "dingo_general"
        private const val CHANNEL_ID_GOALS = "dingo_goals"
        private const val CHANNEL_ID_REMINDERS = "dingo_reminders"

        private const val NOTIFICATION_ID_GOAL_COMPLETION = 1001
        private const val NOTIFICATION_ID_WEEKLY_REMINDER = 1002
        private const val NOTIFICATION_ID_GENERAL = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for different types of notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Goal completion notifications channel
            val goalsChannel = NotificationChannel(
                CHANNEL_ID_GOALS,
                "Goal Completions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for goal completions and achievements"
            }

            // Reminder notifications channel
            val remindersChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly reminders and goal check-ins"
            }

            notificationManager.createNotificationChannels(
                listOf(generalChannel, goalsChannel, remindersChannel)
            )
        }
    }

    /**
     * Show a goal completion notification
     */
    suspend fun showGoalCompletionNotification(goalTitle: String) {
        val preferences = userPreferencesDataStore.userPreferences.first()

        if (!preferences.notificationsEnabled || !preferences.goalCompletionNotifications) {
            return
        }

        if (!hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GOALS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("Goal Completed! 🎉")
            .setContentText("Congratulations! You completed: $goalTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GOAL_COMPLETION, notification)
    }

    /**
     * Show a weekly reminder notification
     */
    suspend fun showWeeklyReminderNotification(activeGoalsCount: Int) {
        val preferences = userPreferencesDataStore.userPreferences.first()

        if (!preferences.notificationsEnabled || !preferences.weeklyRemindersEnabled) {
            return
        }

        if (!hasNotificationPermission()) {
            return
        }

        val message = when {
            activeGoalsCount == 0 -> "Time to set some new goals for this week!"
            activeGoalsCount == 1 -> "You have 1 active goal. Keep pushing forward!"
            else -> "You have $activeGoalsCount active goals. Let's make progress!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("Weekly Goal Check-in 📅")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_WEEKLY_REMINDER, notification)
    }

    /**
     * Show a general notification
     */
    suspend fun showGeneralNotification(title: String, message: String) {
        val preferences = userPreferencesDataStore.userPreferences.first()

        if (!preferences.notificationsEnabled) {
            return
        }

        if (!hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createMainActivityPendingIntent())
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GENERAL, notification)
    }

    /**
     * Check if the app has notification permission
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Create a pending intent to open the main activity
     */
    private fun createMainActivityPendingIntent(): PendingIntent {
        // You'll need to replace this with your actual MainActivity class
        val intent = Intent().apply {
            setClassName(context, "io.sukhuat.dingo.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Cancel a specific notification
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
