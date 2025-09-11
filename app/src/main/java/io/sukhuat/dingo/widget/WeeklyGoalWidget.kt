package io.sukhuat.dingo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.EntryPointAccessors
import io.sukhuat.dingo.MainActivity

/**
 * Sealed class for widget content types - avoids composable try-catch issues
 */
sealed class WidgetContentType {
    data class Goals(val goals: List<io.sukhuat.dingo.widget.models.WidgetGoal>) : WidgetContentType()
    object Empty : WidgetContentType()
    data class Debug(val message: String) : WidgetContentType()
}

/**
 * Glance AppWidget for displaying weekly goals
 */
object WeeklyGoalWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.e("WIDGET_DEBUG", "🚨🚨🚨 MAIN WIDGET STARTING - DYNAMIC MODE")
        android.util.Log.e("WIDGET_DEBUG", "Context: $context")
        android.util.Log.e("WIDGET_DEBUG", "GlanceId: $id")
        android.util.Log.e("WIDGET_DEBUG", "Thread: ${Thread.currentThread().name}")
        android.util.Log.e("WIDGET_DEBUG", "Timestamp: ${System.currentTimeMillis()}")

        // Try to get real goals, fall back to samples if needed
        val widgetContent = try {
            // Get current week info
            val calendar = java.util.Calendar.getInstance()
            val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            android.util.Log.d("WeeklyGoalWidget", "📅 Loading week: $currentWeek, year: $currentYear")

            // Try to get Hilt entry point (but don't fail if unavailable)
            val entryPoint = try {
                EntryPointAccessors.fromApplication(
                    context,
                    WeeklyGoalWidgetEntryPoint::class.java
                )
            } catch (e: Exception) {
                android.util.Log.w("WeeklyGoalWidget", "Hilt not available, using samples", e)
                null
            }

            if (entryPoint != null) {
                try {
                    val dataLoader = entryPoint.getWidgetDataLoader()
                    val cachedGoals = dataLoader.getCachedGoalsSync(currentWeek, currentYear)
                    android.util.Log.d("WeeklyGoalWidget", "📦 Found ${cachedGoals.size} goals")

                    if (cachedGoals.isNotEmpty()) {
                        WidgetContentType.Goals(cachedGoals)
                    } else {
                        android.util.Log.d("WeeklyGoalWidget", "No goals, showing empty state")
                        WidgetContentType.Empty
                    }
                } catch (e: Exception) {
                    android.util.Log.w("WeeklyGoalWidget", "DataLoader failed, using samples", e)
                    WidgetContentType.Debug("Using samples - DataLoader error: ${e.message}")
                }
            } else {
                android.util.Log.d("WeeklyGoalWidget", "No Hilt, showing samples")
                WidgetContentType.Debug("Using samples - Hilt unavailable")
            }
        } catch (e: Exception) {
            android.util.Log.e("WeeklyGoalWidget", "Complete failure, using samples", e)
            WidgetContentType.Debug("Using samples - Error: ${e.message}")
        }

        provideContent {
            when (widgetContent) {
                is WidgetContentType.Goals -> GoalsContent(widgetContent.goals)
                is WidgetContentType.Empty -> EmptyContent()
                is WidgetContentType.Debug -> SampleContent(widgetContent.message)
            }
        }

        android.util.Log.e("WIDGET_DEBUG", "✅✅✅ MAIN WIDGET COMPLETED SUCCESSFULLY")
    }
}

@Composable
private fun GoalsContent(goals: List<io.sukhuat.dingo.widget.models.WidgetGoal>) {
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

        // Show ALL goals in scrollable 2x2 grid (2 goals per row)
        LazyColumn {
            items(goals.chunked(2)) { rowGoals ->
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
                                    modifier = GlanceModifier.size(24.dp)
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                            }
                            
                            Text(
                                text = goal.text.take(if (goal.imageResId != null) 15 else 20),
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    color = ColorProvider(Color.Black)
                                )
                            )
                        }
                    }
                    // Fill empty slot if odd number of goals in last row
                    if (rowGoals.size == 1) {
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
private fun EmptyContent() {
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
private fun SampleContent(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFDF2E9)))
            .padding(8.dp)
    ) {
        Text(
            text = "🎯 Sample Goals",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color(0xFF92400E))
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Show sample goals (all 12 possible goals)
        val sampleGoals = listOf(
            "Exercise daily" to true,
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

        // Scrollable list of all sample goals
        LazyColumn {
            items(sampleGoals.chunked(2)) { rowGoals ->
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    rowGoals.forEach { (goalText, completed) ->
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(2.dp)
                                .background(ColorProvider(Color.White))
                                .padding(4.dp)
                                .clickable(actionStartActivity(MainActivity::class.java))
                        ) {
                            Text(
                                text = if (completed) "✅" else "⭕",
                                style = TextStyle(fontSize = 10.sp)
                            )
                            Text(
                                text = goalText.take(12),
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    color = ColorProvider(
                                        if (completed) Color(0xFF059669) else Color.Black
                                    )
                                )
                            )
                        }
                    }
                    // Fill empty slot if odd number
                    if (rowGoals.size == 1) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = message,
            style = TextStyle(
                fontSize = 7.sp,
                color = ColorProvider(Color(0xFF999999))
            )
        )
    }
}
