package io.sukhuat.dingo.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Background worker to update all widget instances
 * Runs periodically to keep widget data fresh
 */
class WeeklyGoalWidgetUpdateWorker constructor(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("WidgetWorker", "🔄 Background widget update started")

                // First, load and cache fresh data
                loadAndCacheGoalData()

                // Then update all widget instances
                updateAllWidgets()

                android.util.Log.d("WidgetWorker", "✅ Background widget update completed")
                Result.success()
            } catch (exception: Exception) {
                android.util.Log.e("WidgetWorker", "❌ Widget update failed", exception)
                // Log error and retry with exponential backoff
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }

    private suspend fun loadAndCacheGoalData() {
        try {
            // Get dependencies using Hilt EntryPoint
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                WeeklyGoalWidgetEntryPoint::class.java
            )
            val widgetRepository = entryPoint.getWidgetRepository()
            val dataLoader = entryPoint.getWidgetDataLoader()

            // Cache current week and past 4 weeks
            val calendar = Calendar.getInstance()
            for (weekOffset in 0 downTo -4) {
                val weekCalendar = Calendar.getInstance()
                weekCalendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
                val weekOfYear = weekCalendar.get(Calendar.WEEK_OF_YEAR)
                val year = weekCalendar.get(Calendar.YEAR)

                android.util.Log.d("WidgetWorker", "📊 Loading goals for week $weekOfYear/$year")

                // Load goals from repository
                val goals = widgetRepository.getGoalsForWeek(weekOfYear, year)

                // Cache them synchronously for widget access
                dataLoader.cacheGoalsSync(weekOfYear, year, goals)

                android.util.Log.d("WidgetWorker", "💾 Cached ${goals.size} goals for week $weekOfYear/$year")
            }
        } catch (e: Exception) {
            android.util.Log.e("WidgetWorker", "❌ Error loading and caching goal data", e)
            throw e
        }
    }

    private suspend fun updateAllWidgets() {
        try {
            // Update all widget sizes
            WeeklyGoalWidget.updateAll(applicationContext)
            WeeklyGoalWidget2x3.updateAll(applicationContext)
            WeeklyGoalWidget3x2.updateAll(applicationContext)
        } catch (e: Exception) {
            // Continue with other widgets even if one fails
            throw e
        }
    }

    /*@dagger.assisted.AssistedFactory
    interface Factory {
        fun create(context: Context, workerParams: WorkerParameters): WeeklyGoalWidgetUpdateWorker
    }*/

    companion object {
        const val WORK_NAME = "weekly_goal_widget_update"
        private const val MAX_RETRIES = 3
    }
}
