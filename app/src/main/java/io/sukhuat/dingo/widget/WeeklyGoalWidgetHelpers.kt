package io.sukhuat.dingo.widget

import android.content.Context
import kotlinx.coroutines.withTimeout

/**
 * Try to load goals from cache when authentication fails
 */
suspend fun tryLoadCachedGoals(dataLoader: WidgetDataLoader): WidgetDataResult<List<io.sukhuat.dingo.widget.models.WidgetGoal>> {
    return try {
        val calendar = java.util.Calendar.getInstance()
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)

        if (cachedGoals.isNotEmpty()) {
            android.util.Log.d("WeeklyGoalWidget", "✅ Found ${cachedGoals.size} cached goals")
            WidgetDataResult.Success(cachedGoals, isStale = true)
        } else {
            android.util.Log.w("WeeklyGoalWidget", "No cached goals available")
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.AuthenticationError)
        }
    } catch (e: Exception) {
        android.util.Log.e("WeeklyGoalWidget", "❌ Error loading cached goals", e)
        WidgetDataResult.Error(WidgetErrorHandler.WidgetError.DataLoadFailure)
    }
}

/**
 * Check if authentication is required (simple heuristic)
 */
fun isAuthenticationRequired(context: Context): Boolean {
    return try {
        // Check if Firebase Auth is available and user is logged in
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null
    } catch (e: Exception) {
        android.util.Log.w("WeeklyGoalWidget", "Could not check auth state", e)
        true // Assume auth is required if we can't check
    }
}

/**
 * Optimized goal loading for widgets - fast and reliable
 */
suspend fun loadGoalsForWidget(dataLoader: WidgetDataLoader): WidgetDataResult<List<io.sukhuat.dingo.widget.models.WidgetGoal>> {
    val calendar = java.util.Calendar.getInstance()
    val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
    val currentYear = calendar.get(java.util.Calendar.YEAR)

    return try {
        android.util.Log.d("WeeklyGoalWidget", "🔍 Checking for cached goals first...")

        // Step 1: Check if we have fresh cached data
        if (dataLoader.isCachedDataFresh(currentWeek, currentYear)) {
            val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
            if (cachedGoals.isNotEmpty()) {
                android.util.Log.d("WeeklyGoalWidget", "⚡ Using fresh cached data: ${cachedGoals.size} goals")
                return WidgetDataResult.Success(cachedGoals, isStale = false)
            }
        }

        android.util.Log.d("WeeklyGoalWidget", "🌐 Attempting live data load with timeout...")

        // Step 2: Try to load fresh data with timeout
        val result = withTimeout(5000L) { // 5 second timeout
            dataLoader.loadCurrentWeekGoals(forceRefresh = true)
        }

        when (result) {
            is WidgetDataResult.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val goals = result.data as List<io.sukhuat.dingo.widget.models.WidgetGoal>
                android.util.Log.d("WeeklyGoalWidget", "✅ Fresh data loaded: ${goals.size} goals")
                result
            }
            is WidgetDataResult.Error<*> -> {
                android.util.Log.w("WeeklyGoalWidget", "⚠️ Fresh load failed, trying stale cache...")
                // Fall back to stale cached data
                val staleGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
                if (staleGoals.isNotEmpty()) {
                    android.util.Log.d("WeeklyGoalWidget", "📦 Using stale cached data: ${staleGoals.size} goals")
                    WidgetDataResult.Success(staleGoals, isStale = true)
                } else {
                    result
                }
            }
            else -> result
        }
    } catch (authException: SecurityException) {
        android.util.Log.w("WeeklyGoalWidget", "🔐 Authentication error, using cached data only")
        val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
        if (cachedGoals.isNotEmpty()) {
            WidgetDataResult.Success(cachedGoals, isStale = true)
        } else {
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.AuthenticationError)
        }
    } catch (timeoutException: kotlinx.coroutines.TimeoutCancellationException) {
        android.util.Log.w("WeeklyGoalWidget", "⏰ Timeout, using cached data")
        val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
        if (cachedGoals.isNotEmpty()) {
            WidgetDataResult.Success(cachedGoals, isStale = true)
        } else {
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.CustomError("Loading timeout"))
        }
    } catch (e: Exception) {
        android.util.Log.e("WeeklyGoalWidget", "❌ Widget loading error", e)
        // Final fallback to any cached data
        val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
        if (cachedGoals.isNotEmpty()) {
            android.util.Log.d("WeeklyGoalWidget", "🆘 Emergency fallback to cached data: ${cachedGoals.size} goals")
            WidgetDataResult.Success(cachedGoals, isStale = true)
        } else {
            WidgetDataResult.Error(WidgetErrorHandler.WidgetError.DataLoadFailure)
        }
    }
}
