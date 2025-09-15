package io.sukhuat.dingo.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Glance AppWidgetReceiver for the Weekly Goal Widget (2x3 with image & status)
 */
class WeeklyGoalWidgetProvider : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalWidget
}
