package io.sukhuat.dingo.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling recurring notifications
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val WEEKLY_REMINDER_REQUEST_CODE = 2001
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule weekly reminder notifications
     * @param dayOfWeek Calendar day of week (Calendar.SUNDAY, Calendar.MONDAY, etc.)
     * @param hourOfDay Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleWeeklyReminder(
        dayOfWeek: Int = Calendar.SUNDAY,
        hourOfDay: Int = 19, // 7 PM
        minute: Int = 0
    ) {
        val intent = Intent(context, WeeklyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEEKLY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the next occurrence of the specified day and time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed this week, schedule for next week
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        // Schedule the repeating alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, // Weekly
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, // Weekly
                pendingIntent
            )
        }
    }

    /**
     * Cancel weekly reminder notifications
     */
    fun cancelWeeklyReminder() {
        val intent = Intent(context, WeeklyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEEKLY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Check if weekly reminders are scheduled
     */
    fun isWeeklyReminderScheduled(): Boolean {
        val intent = Intent(context, WeeklyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEEKLY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        return pendingIntent != null
    }
}
