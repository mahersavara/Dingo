package io.sukhuat.dingo.widget

import android.content.Context
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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.sukhuat.dingo.MainActivity
import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * Simple version that shows test data immediately - for debugging
 */
object WeeklyGoalWidgetSimple : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.d("WeeklyGoalWidgetSimple", "🎯 SIMPLE WIDGET - IMMEDIATE TEST DATA")

        // Create test goals immediately
        val testGoals = listOf(
            io.sukhuat.dingo.widget.models.WidgetGoal(
                id = "test1",
                text = "Test Goal 1",
                imageResId = null,
                customImage = null,
                status = GoalStatus.ACTIVE,
                weekOfYear = 50,
                yearCreated = 2024,
                position = 0
            ),
            io.sukhuat.dingo.widget.models.WidgetGoal(
                id = "test2",
                text = "Test Goal 2",
                imageResId = null,
                customImage = null,
                status = GoalStatus.COMPLETED,
                weekOfYear = 50,
                yearCreated = 2024,
                position = 1
            ),
            io.sukhuat.dingo.widget.models.WidgetGoal(
                id = "test3",
                text = "Test Goal 3",
                imageResId = null,
                customImage = null,
                status = GoalStatus.ACTIVE,
                weekOfYear = 50,
                yearCreated = 2024,
                position = 2
            )
        )

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFFFDF2E9)))
                    .padding(8.dp)
            ) {
                Text(
                    text = "✅ WIDGET WORKING!",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(Color(0xFF92400E))
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "Test Goals:",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFF666666))
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Show test goals in a 2x2 grid
                testGoals.take(4).chunked(2).forEach { rowGoals ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        rowGoals.forEach { goal ->
                            Column(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .padding(2.dp)
                                    .background(ColorProvider(Color.White))
                                    .padding(4.dp)
                                    .clickable(actionStartActivity(MainActivity::class.java))
                            ) {
                                Text(
                                    text = goal.text.take(15),
                                    style = TextStyle(
                                        fontSize = 8.sp,
                                        color = ColorProvider(
                                            when (goal.status) {
                                                GoalStatus.COMPLETED -> Color.Green
                                                else -> Color.Black
                                            }
                                        )
                                    )
                                )
                            }
                        }
                        if (rowGoals.size == 1) {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "Time: ${System.currentTimeMillis()}",
                    style = TextStyle(
                        fontSize = 7.sp,
                        color = ColorProvider(Color(0xFF999999))
                    )
                )
            }
        }

        android.util.Log.d("WeeklyGoalWidgetSimple", "✅ Simple widget completed successfully")
    }
}
