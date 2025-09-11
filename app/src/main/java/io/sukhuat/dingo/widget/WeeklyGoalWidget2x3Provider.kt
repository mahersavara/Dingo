package io.sukhuat.dingo.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Glance AppWidgetReceiver for the 2x3 Weekly Goal Widget
 */
class WeeklyGoalWidget2x3Provider : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalWidget2x3
}
