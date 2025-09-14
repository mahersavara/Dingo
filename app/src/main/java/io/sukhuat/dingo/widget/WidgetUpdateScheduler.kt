package io.sukhuat.dingo.widget

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and manages background widget updates using WorkManager
 */
@Singleton
class WidgetUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceOptimizer: WidgetPerformanceOptimizer
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic widget updates with adaptive intervals
     */
    fun schedulePeriodicUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Use adaptive interval based on device performance
        val updateInterval = performanceOptimizer.getOptimalUpdateInterval()
        val intervalMinutes = updateInterval / (60 * 1000) // Convert to minutes

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WeeklyGoalWidgetUpdateWorker>(
            intervalMinutes,
            TimeUnit.MINUTES,
            (intervalMinutes / 2).coerceAtMost(15),
            TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag("widget_update")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WeeklyGoalWidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    /**
     * Schedule immediate widget update
     * Used when app data changes
     */
    fun scheduleImmediateUpdate() {
        android.util.Log.d("WidgetUpdateScheduler", "🚀 Scheduling immediate widget update")

        // For immediate updates, don't require network connectivity
        // since the data should already be available locally
        val immediateWorkRequest = OneTimeWorkRequestBuilder<WeeklyGoalWidgetUpdateWorker>()
            .addTag("widget_immediate_update")
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueue(immediateWorkRequest)
    }

    /**
     * Cancel all widget update work
     */
    fun cancelAllUpdates() {
        workManager.cancelUniqueWork(WeeklyGoalWidgetUpdateWorker.WORK_NAME)
        workManager.cancelAllWorkByTag("widget_update")
        workManager.cancelAllWorkByTag("widget_immediate_update")
    }

    /**
     * Force immediate update of all widgets
     */
    fun forceUpdateNow() {
        android.util.Log.d("WidgetUpdateScheduler", "🚀 Force updating widgets")

        val immediateWorkRequest = OneTimeWorkRequestBuilder<WeeklyGoalWidgetUpdateWorker>()
            .addTag("widget_force_update")
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueue(immediateWorkRequest)
    }

    /**
     * Get the status of widget update work
     */
    fun getUpdateWorkStatus() = workManager.getWorkInfosForUniqueWorkLiveData(
        WeeklyGoalWidgetUpdateWorker.WORK_NAME
    )
}
