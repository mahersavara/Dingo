package io.sukhuat.dingo.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Glance AppWidgetReceiver for the 3x2 Weekly Goal Widget
 */
class WeeklyGoalWidget3x2Provider : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalWidget3x2
}
