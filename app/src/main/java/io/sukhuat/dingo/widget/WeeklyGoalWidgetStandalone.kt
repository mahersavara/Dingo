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
 * Standalone widget that shows immediate content without dependencies
 */
object WeeklyGoalWidgetStandalone : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.e("WIDGET_STANDALONE", "🚀 STANDALONE WIDGET - NO DEPENDENCIES")
        android.util.Log.e("WIDGET_STANDALONE", "Context: $context")
        android.util.Log.e("WIDGET_STANDALONE", "GlanceId: $id")
        android.util.Log.e("WIDGET_STANDALONE", "Timestamp: ${System.currentTimeMillis()}")

        provideContent {
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

                // Show sample goals directly
                val sampleGoals = listOf(
                    "Exercise 3x this week" to GoalStatus.ACTIVE,
                    "Read for 30 minutes" to GoalStatus.COMPLETED,
                    "Cook healthy meals" to GoalStatus.ACTIVE,
                    "Call family members" to GoalStatus.ACTIVE
                )

                // Display in 2x2 grid
                sampleGoals.chunked(2).forEach { rowGoals ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        rowGoals.forEach { (goalText, status) ->
                            Column(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .padding(2.dp)
                                    .background(ColorProvider(Color.White))
                                    .padding(4.dp)
                                    .clickable(actionStartActivity(MainActivity::class.java))
                            ) {
                                Text(
                                    text = if (status == GoalStatus.COMPLETED) "✅" else "⭕",
                                    style = TextStyle(fontSize = 12.sp)
                                )
                                Text(
                                    text = goalText.take(12),
                                    style = TextStyle(
                                        fontSize = 8.sp,
                                        color = ColorProvider(
                                            when (status) {
                                                GoalStatus.COMPLETED -> Color(0xFF059669)
                                                else -> Color.Black
                                            }
                                        )
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "Tap to open Dingo app",
                    style = TextStyle(
                        fontSize = 8.sp,
                        color = ColorProvider(Color(0xFF666666))
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
                )

                Text(
                    text = "Updated: ${System.currentTimeMillis()}",
                    style = TextStyle(
                        fontSize = 7.sp,
                        color = ColorProvider(Color(0xFF999999))
                    )
                )
            }
        }

        android.util.Log.e("WIDGET_STANDALONE", "✅ STANDALONE WIDGET COMPLETED")
    }
}
