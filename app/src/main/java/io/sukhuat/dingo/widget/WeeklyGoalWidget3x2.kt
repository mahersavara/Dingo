package io.sukhuat.dingo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
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
private val WEEK_OFFSET_KEY = androidx.datastore.preferences.core.intPreferencesKey("week_offset_3x2")

/**
 * Glance AppWidget for displaying weekly goals in 3x2 format (horizontal)
 */
object WeeklyGoalWidget3x2 : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.d("WeeklyGoalWidget3x2", "🎯 3x2 Widget - Loading real goal data")

        // Try to get real goals, fall back to samples if needed
        val widgetContent = try {
            // Get current week info
            val calendar = java.util.Calendar.getInstance()
            val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            android.util.Log.d("WeeklyGoalWidget3x2", "📅 Loading week: $currentWeek, year: $currentYear")

            // Try to get Hilt entry point (but don't fail if unavailable)
            val entryPoint = try {
                EntryPointAccessors.fromApplication(
                    context,
                    WeeklyGoalWidgetEntryPoint::class.java
                )
            } catch (e: Exception) {
                android.util.Log.w("WeeklyGoalWidget3x2", "Hilt not available, using samples", e)
                null
            }

            if (entryPoint != null) {
                try {
                    val dataLoader = entryPoint.getWidgetDataLoader()
                    val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
                    android.util.Log.d("WeeklyGoalWidget3x2", "📦 Found ${cachedGoals.size} goals")

                    if (cachedGoals.isNotEmpty()) {
                        WidgetContentType.Goals(cachedGoals)
                    } else {
                        android.util.Log.d("WeeklyGoalWidget3x2", "No goals, showing empty state")
                        WidgetContentType.Empty
                    }
                } catch (e: Exception) {
                    android.util.Log.w("WeeklyGoalWidget3x2", "DataLoader failed, using samples", e)
                    WidgetContentType.Debug("Using samples - DataLoader error: ${e.message}")
                }
            } else {
                android.util.Log.d("WeeklyGoalWidget3x2", "No Hilt, showing samples")
                WidgetContentType.Debug("Using samples - Hilt unavailable")
            }
        } catch (e: Exception) {
            android.util.Log.e("WeeklyGoalWidget3x2", "Complete failure, using samples", e)
            WidgetContentType.Debug("Using samples - Error: ${e.message}")
        }

        provideContent {
            when (widgetContent) {
                is WidgetContentType.Goals -> Goals3x2Content(widgetContent.goals)
                is WidgetContentType.Empty -> Empty3x2Content()
                is WidgetContentType.Debug -> Sample3x2Content(widgetContent.message)
            }
        }

        android.util.Log.d("WeeklyGoalWidget3x2", "✅ 3x2 Widget completed successfully")
    }
}

// Action callbacks for 3x2 week navigation
class PreviousWeek3x2Action : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentWeekOffset = prefs[WEEK_OFFSET_KEY] ?: 0
            val newWeekOffset = (currentWeekOffset - 1).coerceIn(-4, 0) // Max 4 weeks in past
            prefs[WEEK_OFFSET_KEY] = newWeekOffset
        }
        WeeklyGoalWidget3x2.update(context, glanceId)
    }
}

class NextWeek3x2Action : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentWeekOffset = prefs[WEEK_OFFSET_KEY] ?: 0
            val newWeekOffset = (currentWeekOffset + 1).coerceIn(-4, 0) // Max 4 weeks in past, current is 0
            prefs[WEEK_OFFSET_KEY] = newWeekOffset
        }
        WeeklyGoalWidget3x2.update(context, glanceId)
    }
}

@Composable
private fun WeekNavigation3x2WidgetContent(
    weekOfYear: Int,
    year: Int,
    currentWeek: Int,
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
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous week button (only show if not at max past weeks)
            if (weekOffset > -4) {
                Text(
                    text = "◀",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color(0xFFD97706))
                    ),
                    modifier = GlanceModifier
                        .clickable(onPreviousWeek())
                        .padding(horizontal = 4.dp)
                )
            } else {
                Spacer(modifier = GlanceModifier.width(20.dp))
            }

            // Week title - clickable to open main app
            Text(
                text = if (weekOffset == 0) {
                    "This Week"
                } else {
                    "Week $weekOfYear" + (if (year != currentYear) " $year" else "")
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF92400E))
                ),
                modifier = GlanceModifier
                    .clickable(onGoalClick())
                    .padding(horizontal = 8.dp)
            )

            // Next week button (only show if viewing past weeks)
            if (weekOffset < 0) {
                Text(
                    text = "▶",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color(0xFFD97706))
                    ),
                    modifier = GlanceModifier
                        .clickable(onNextWeek())
                        .padding(horizontal = 4.dp)
                )
            } else {
                Spacer(modifier = GlanceModifier.width(20.dp))
            }
        }

        // Horizontal 3x2 grid (6 goals in 2 rows of 3)
        Column {
            // Row 1
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                DemoGoalCard(
                    text = if (weekOffset == 0) "Exercise" else "Past Ex",
                    status = if (weekOffset == 0) "active" else "completed",
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                DemoGoalCard(
                    text = if (weekOffset == 0) "Read" else "Past Read",
                    status = "completed",
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                DemoGoalCard(
                    text = if (weekOffset == 0) "Learn" else "Past Learn",
                    status = if (weekOffset == 0) "active" else "failed",
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(modifier = GlanceModifier.height(3.dp))

            // Row 2
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                DemoGoalCard(
                    text = if (weekOffset == 0) "Meditate" else "Past Med",
                    status = if (weekOffset == 0) "active" else "completed",
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                DemoGoalCard(
                    text = if (weekOffset == 0) "Cook" else "Past Cook",
                    status = if (weekOffset == 0) "active" else "completed",
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                EmptyGoalSlot(
                    onClick = onGoalClick(),
                    modifier = GlanceModifier.defaultWeight()
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
            .height(50.dp)
            .background(ColorProvider(backgroundColor as Color))
            .padding(1.dp)
            .clickable(onClick)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(backgroundColor))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusIndicator as String,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(borderColor as Color)
                    )
                )

                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = ColorProvider(textColor as Color),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
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
            .height(50.dp)
            .background(ColorProvider(Color(0xFFF9FAFB))) // Light gray background
            .padding(1.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = TextStyle(
                fontSize = 16.sp,
                color = ColorProvider(Color(0xFF9CA3AF)) // Gray plus sign
            )
        )
    }
}

@Composable
private fun Goals3x2Content(goals: List<io.sukhuat.dingo.widget.models.WidgetGoal>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFDF2E9)))
            .padding(8.dp)
    ) {
        Text(
            text = "Weekly Goals",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color(0xFF92400E))
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Show ALL goals in scrollable 3x2 horizontal layout (3 goals per row)
        LazyColumn {
            items(goals.chunked(3)) { rowGoals ->
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    rowGoals.forEach { goal ->
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(2.dp)
                                .background(ColorProvider(Color.White))
                                .padding(4.dp)
                                .clickable(actionStartActivity(MainActivity::class.java)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Show image if available (local resource only for now)
                            goal.imageResId?.let { resId ->
                                Image(
                                    provider = ImageProvider(resId),
                                    contentDescription = goal.text,
                                    modifier = GlanceModifier.size(20.dp)
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                            }
                            
                            Text(
                                text = goal.text.take(if (goal.imageResId != null) 10 else 12),
                                style = TextStyle(
                                    fontSize = 7.sp,
                                    color = ColorProvider(Color.Black)
                                )
                            )
                        }
                    }
                    // Fill remaining slots with empty space for last row
                    repeat(3 - rowGoals.size) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to open app",
            style = TextStyle(
                fontSize = 8.sp,
                color = ColorProvider(Color(0xFF666666))
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )
    }
}

@Composable
private fun Empty3x2Content() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFDF2E9)))
            .padding(8.dp)
    ) {
        Text(
            text = "No Goals Yet",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color(0xFF92400E))
            )
        )
        Text(
            text = "Tap to add goals",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color(0xFF666666))
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )
    }
}

@Composable
private fun Sample3x2Content(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFDF2E9)))
            .padding(8.dp)
    ) {
        Text(
            text = "🎯 Weekly Goals",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color(0xFF92400E))
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Show ALL sample goals in scrollable 3x2 layout (3 goals per row)
        val sampleGoals = listOf(
            "Exercise" to true,
            "Read 30 min" to false,
            "Healthy meals" to true,
            "Call family" to false,
            "Learn skill" to true,
            "Save money" to false,
            "Meditate" to true,
            "Walk outside" to false,
            "Sleep 8hrs" to true,
            "Drink water" to false,
            "Clean room" to true,
            "Plan week" to false
        )

        LazyColumn {
            items(sampleGoals.chunked(3)) { rowGoals ->
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    rowGoals.forEach { (goalText, completed) ->
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(2.dp)
                                .background(ColorProvider(Color.White))
                                .padding(4.dp)
                                .clickable(actionStartActivity(MainActivity::class.java)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (completed) "✅" else "⭕",
                                style = TextStyle(fontSize = 8.sp)
                            )
                            Text(
                                text = goalText.take(8),
                                style = TextStyle(
                                    fontSize = 7.sp,
                                    color = ColorProvider(
                                        if (completed) Color(0xFF059669) else Color.Black
                                    )
                                )
                            )
                        }
                    }
                    // Fill remaining slots
                    repeat(3 - rowGoals.size) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = message,
            style = TextStyle(
                fontSize = 6.sp,
                color = ColorProvider(Color(0xFF999999))
            )
        )
    }
}
