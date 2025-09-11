package io.sukhuat.dingo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import io.sukhuat.dingo.MainActivity
import java.util.*

// Define the key here since the provider was simplified
private val WEEK_OFFSET_KEY = androidx.datastore.preferences.core.intPreferencesKey("week_offset_2x3")

// Using common WidgetLoadResult and ErrorWidgetContent from WidgetCommon.kt

@Composable
private fun LoadingWidgetContent() {
    val backgroundColor = Color(0xFFFDF2E9) // Warm cream background

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Loading goals...",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF666666)),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * Glance AppWidget for displaying weekly goals in 2x3 format (vertical)
 */
object WeeklyGoalWidget2x3 : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.d("WeeklyGoalWidget2x3", "🔍 provideGlance started for widget ID: $id")

        // Get dependencies using Hilt EntryPoint
        val entryPoint = try {
            EntryPointAccessors.fromApplication(
                context,
                WeeklyGoalWidgetEntryPoint::class.java
            )
        } catch (e: Exception) {
            android.util.Log.e("WeeklyGoalWidget2x3", "❌ Failed to get Hilt entry point", e)
            provideContent {
                ErrorWidgetContent(
                    message = "Widget initialization failed",
                    onGoalClick = { actionStartActivity(MainActivity::class.java) }
                )
            }
            return
        }

        provideContent {
            // Get current week offset from widget state
            val weekOffset = currentState<androidx.datastore.preferences.core.Preferences>()[WEEK_OFFSET_KEY] ?: 0
            val (weekOfYear, year) = WidgetState.getTargetWeek(weekOffset)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            android.util.Log.d("WeeklyGoalWidget2x3", "📅 Widget rendering - Week: $weekOfYear, year: $year, offset: $weekOffset")

            // Load cached data synchronously
            val dataLoader = entryPoint.getWidgetDataLoader()
            val cachedGoals = dataLoader.getCachedGoalsSync(weekOfYear, year)
            val isDataFresh = dataLoader.isCachedDataFresh(weekOfYear, year)

            android.util.Log.d("WeeklyGoalWidget2x3", "📦 Loaded ${cachedGoals.size} cached goals, fresh: $isDataFresh")

            // Render widget based on cached data
            if (cachedGoals.isNotEmpty()) {
                android.util.Log.d("WeeklyGoalWidget2x3", "✅ Rendering with ${cachedGoals.size} cached goals")
                WeekNavigation2x3WidgetContent(
                    goals = cachedGoals,
                    weekOfYear = weekOfYear,
                    year = year,
                    currentYear = currentYear,
                    weekOffset = weekOffset,
                    onGoalClick = { actionStartActivity(MainActivity::class.java) },
                    onPreviousWeek = { actionRunCallback<PreviousWeek2x3Action>() },
                    onNextWeek = { actionRunCallback<NextWeek2x3Action>() }
                )
            } else {
                android.util.Log.d("WeeklyGoalWidget2x3", "📝 Rendering empty state")
                // Show empty state - no goals for this week
                WeekNavigation2x3WidgetContent(
                    goals = emptyList(), // Empty list will show "No goals" state
                    weekOfYear = weekOfYear,
                    year = year,
                    currentYear = currentYear,
                    weekOffset = weekOffset,
                    onGoalClick = { actionStartActivity(MainActivity::class.java) },
                    onPreviousWeek = { actionRunCallback<PreviousWeek2x3Action>() },
                    onNextWeek = { actionRunCallback<NextWeek2x3Action>() }
                )
            }
        }
    }
}

// Action callbacks for 2x3 week navigation
class PreviousWeek2x3Action : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentWeekOffset = prefs[WEEK_OFFSET_KEY] ?: 0
            val newWeekOffset = (currentWeekOffset - 1).coerceIn(-4, 0) // Max 4 weeks in past
            prefs[WEEK_OFFSET_KEY] = newWeekOffset
        }
        WeeklyGoalWidget2x3.update(context, glanceId)
    }
}

class NextWeek2x3Action : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentWeekOffset = prefs[WEEK_OFFSET_KEY] ?: 0
            val newWeekOffset = (currentWeekOffset + 1).coerceIn(-4, 0) // Max 4 weeks in past, current is 0
            prefs[WEEK_OFFSET_KEY] = newWeekOffset
        }
        WeeklyGoalWidget2x3.update(context, glanceId)
    }
}

@Composable
private fun WeekNavigation2x3WidgetContent(
    goals: List<io.sukhuat.dingo.widget.models.WidgetGoal>,
    weekOfYear: Int,
    year: Int,
    currentYear: Int,
    weekOffset: Int,
    onGoalClick: () -> androidx.glance.action.Action,
    onPreviousWeek: () -> androidx.glance.action.Action,
    onNextWeek: () -> androidx.glance.action.Action
) {
    // Mountain Sunrise gradient background
    val backgroundColor = Color(0xFFFDF2E9) // Warm cream background

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp)
    ) {
        // Header with week navigation
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous week button (only show if not at max past weeks)
            if (weekOffset > -4) {
                Text(
                    text = "◀",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color(0xFFD97706))
                    ),
                    modifier = GlanceModifier
                        .clickable(onPreviousWeek())
                        .padding(horizontal = 6.dp)
                )
            } else {
                Spacer(modifier = GlanceModifier.width(26.dp))
            }

            // Week title - clickable to open main app
            Text(
                text = if (weekOffset == 0) {
                    "This Week"
                } else {
                    "Week $weekOfYear" + (if (year != currentYear) " $year" else "")
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    color = ColorProvider(Color(0xFF92400E))
                ),
                modifier = GlanceModifier
                    .clickable(onGoalClick())
                    .padding(horizontal = 12.dp)
            )

            // Next week button (only show if viewing past weeks)
            if (weekOffset < 0) {
                Text(
                    text = "▶",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color(0xFFD97706))
                    ),
                    modifier = GlanceModifier
                        .clickable(onNextWeek())
                        .padding(horizontal = 6.dp)
                )
            } else {
                Spacer(modifier = GlanceModifier.width(26.dp))
            }
        }

        // Goals Grid (2x3 layout) - Real data
        if (goals.isEmpty()) {
            // Empty state
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(onGoalClick()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No goals this week\nTap to add some!",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = ColorProvider(Color(0xFF6B7280)),
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            // Show ALL goals in scrollable 2x3 layout (2 goals per row)
            LazyColumn {
                items(goals.chunked(2)) { rowGoals ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        rowGoals.forEach { goal ->
                            GoalCard(
                                goal = goal,
                                onClick = onGoalClick(),
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                        
                        // Fill empty slot if odd number of goals in last row  
                        if (rowGoals.size == 1) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            EmptyGoalSlot(
                                onClick = onGoalClick(),
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: io.sukhuat.dingo.widget.models.WidgetGoal,
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier
) {
    // Mountain Sunrise theme colors
    val (backgroundColor, borderColor, textColor, statusIndicator) = when (goal.status) {
        io.sukhuat.dingo.domain.model.GoalStatus.COMPLETED -> {
            val bg = Color(0xFFF0F9FF) // Light blue background
            val border = Color(0xFF059669) // Green border
            val text = Color(0xFF047857) // Dark green text
            val indicator = "✓"
            listOf(bg, border, text, indicator)
        }
        io.sukhuat.dingo.domain.model.GoalStatus.FAILED -> {
            val bg = Color(0xFFFEF2F2) // Light red background
            val border = Color(0xFFDC2626) // Red border
            val text = Color(0xFF991B1B) // Dark red text
            val indicator = "✗"
            listOf(bg, border, text, indicator)
        }
        io.sukhuat.dingo.domain.model.GoalStatus.ACTIVE -> {
            val bg = Color(0xFFFFFBEB) // Warm cream background
            val border = Color(0xFFD97706) // Warm orange border
            val text = Color(0xFF92400E) // Warm brown text
            val indicator = "○"
            listOf(bg, border, text, indicator)
        }
        io.sukhuat.dingo.domain.model.GoalStatus.ARCHIVED -> {
            val bg = Color(0xFFF9FAFB) // Light gray background
            val border = Color(0xFF6B7280) // Gray border
            val text = Color(0xFF374151) // Dark gray text
            val indicator = "◐"
            listOf(bg, border, text, indicator)
        }
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .background(ColorProvider(backgroundColor as Color))
            .padding(1.dp)
            .clickable(onClick)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(backgroundColor))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusIndicator as String,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(borderColor as Color)
                    )
                )

                Text(
                    text = goal.text,
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = ColorProvider(textColor as Color),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun DemoGoalCard(
    text: String,
    status: String,
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier
) {
    val (backgroundColor, borderColor, textColor, statusIndicator) = when (status) {
        "completed" -> listOf(
            Color(0xFFF0F9FF),
            Color(0xFF059669),
            Color(0xFF047857),
            "✓"
        )
        "failed" -> listOf(
            Color(0xFFFEF2F2),
            Color(0xFFDC2626),
            Color(0xFF991B1B),
            "✗"
        )
        else -> listOf(
            Color(0xFFFFFBEB),
            Color(0xFFD97706),
            Color(0xFF92400E),
            "○"
        )
    }

    Box(
        modifier = modifier
            .height(56.dp)
            .background(ColorProvider(backgroundColor as Color))
            .padding(1.dp)
            .clickable(onClick)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(backgroundColor))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusIndicator as String,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(borderColor as Color)
                    )
                )

                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = ColorProvider(textColor as Color),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun EmptyGoalSlot(
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(ColorProvider(Color(0xFFF9FAFB))) // Light gray background
            .padding(1.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = TextStyle(
                fontSize = 18.sp,
                color = ColorProvider(Color(0xFF9CA3AF)) // Gray plus sign
            )
        )
    }
}
