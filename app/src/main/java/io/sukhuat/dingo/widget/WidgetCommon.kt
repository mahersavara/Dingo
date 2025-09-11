package io.sukhuat.dingo.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.sukhuat.dingo.MainActivity
import io.sukhuat.dingo.widget.models.WidgetGoal

/**
 * Common result wrapper for widget data loading
 */
sealed class WidgetLoadResult {
    data class Success(val goals: List<WidgetGoal>) : WidgetLoadResult()
    data class Error(val message: String) : WidgetLoadResult()
}

/**
 * Common error content for all widgets
 */
@Composable
fun ErrorWidgetContent(
    message: String? = null,
    onGoalClick: (() -> androidx.glance.action.Action)? = null
) {
    // Mountain Sunrise gradient background
    val backgroundColor = Color(0xFFFDF2E9) // Warm cream background

    val modifier = if (onGoalClick != null) {
        GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp)
            .clickable(onGoalClick())
    } else {
        GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp)
            .clickable(actionStartActivity(MainActivity::class.java))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ColorProvider(Color(0xFFDC2626))
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = message ?: "Unable to load goals",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color(0xFF991B1B)),
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Tap to open app",
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color(0xFF92400E)),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
