package io.sukhuat.dingo.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Glance AppWidgetReceiver for the Weekly Goal Widget
 */
class WeeklyGoalWidgetProvider : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalWidget
}
