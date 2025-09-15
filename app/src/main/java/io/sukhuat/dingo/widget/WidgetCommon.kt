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
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.widget.models.WidgetGoal

/**
 * Common result wrapper for widget data loading
 */
sealed class WidgetLoadResult {
    data class Success(val goals: List<WidgetGoal>) : WidgetLoadResult()
    data class Error(val message: String) : WidgetLoadResult()
}

/**
 * Common widget theme colors
 */
object WidgetTheme {
    val backgroundColor = Color(0xFFFDF2E9) // Warm cream background
    val primaryColor = Color(0xFF92400E) // Warm brown
    val accentColor = Color(0xFFD97706) // Orange
    val textColor = Color(0xFF666666) // Gray
    val successColor = Color(0xFF059669) // Green
    val errorColor = Color(0xFFDC2626) // Red
}

/**
 * Common goal card component for all widget layouts
 */
@Composable
fun WidgetGoalCard(
    goal: WidgetGoal,
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier,
    cardHeight: Int = 56
) {
    val (backgroundColor, borderColor, textColor, statusIndicator) = when (goal.status) {
        GoalStatus.COMPLETED -> {
            val bg = Color(0xFFF0F9FF) // Light blue background
            val border = WidgetTheme.successColor
            val text = Color(0xFF047857) // Dark green text
            val indicator = "✓"
            listOf(bg, border, text, indicator)
        }
        GoalStatus.FAILED -> {
            val bg = Color(0xFFFEF2F2) // Light red background
            val border = WidgetTheme.errorColor
            val text = Color(0xFF991B1B) // Dark red text
            val indicator = "✗"
            listOf(bg, border, text, indicator)
        }
        GoalStatus.ACTIVE -> {
            val bg = Color(0xFFFFFBEB) // Warm cream background
            val border = WidgetTheme.accentColor
            val text = WidgetTheme.primaryColor
            val indicator = "○"
            listOf(bg, border, text, indicator)
        }
        GoalStatus.ARCHIVED -> {
            val bg = Color(0xFFF9FAFB) // Light gray background
            val border = Color(0xFF6B7280) // Gray border
            val text = Color(0xFF374151) // Dark gray text
            val indicator = "◐"
            listOf(bg, border, text, indicator)
        }
    }

    Box(
        modifier = modifier
            .height(cardHeight.dp)
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

/**
 * Common demo goal card for sample content
 */
@Composable
fun DemoGoalCard(
    text: String,
    status: String,
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier,
    cardHeight: Int = 50,
    fontSize: Int = 8
) {
    val (backgroundColor, borderColor, textColor, statusIndicator) = when (status) {
        "completed" -> listOf(
            Color(0xFFF0F9FF),
            WidgetTheme.successColor,
            Color(0xFF047857),
            "✓"
        )
        "failed" -> listOf(
            Color(0xFFFEF2F2),
            WidgetTheme.errorColor,
            Color(0xFF991B1B),
            "✗"
        )
        else -> listOf(
            Color(0xFFFFFBEB),
            WidgetTheme.accentColor,
            WidgetTheme.primaryColor,
            "○"
        )
    }

    Box(
        modifier = modifier
            .height(cardHeight.dp)
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
                        fontSize = fontSize.sp,
                        color = ColorProvider(textColor as Color),
                        textAlign = TextAlign.Center
                    ),
                    maxLines = if (cardHeight > 50) 2 else 1
                )
            }
        }
    }
}

/**
 * Common empty goal slot component
 */
@Composable
fun EmptyGoalSlot(
    onClick: androidx.glance.action.Action,
    modifier: GlanceModifier = GlanceModifier,
    cardHeight: Int = 50
) {
    Box(
        modifier = modifier
            .height(cardHeight.dp)
            .background(ColorProvider(Color(0xFFF9FAFB))) // Light gray background
            .padding(1.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = TextStyle(
                fontSize = (if (cardHeight > 50) 18 else 16).sp,
                color = ColorProvider(Color(0xFF9CA3AF)) // Gray plus sign
            )
        )
    }
}

/**
 * Common error content for all widgets
 */
@Composable
fun ErrorWidgetContent(
    message: String? = null,
    onGoalClick: (() -> androidx.glance.action.Action)? = null
) {
    val modifier = if (onGoalClick != null) {
        GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetTheme.backgroundColor))
            .padding(8.dp)
            .clickable(onGoalClick())
    } else {
        GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetTheme.backgroundColor))
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
                    color = ColorProvider(WidgetTheme.errorColor)
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
                    color = ColorProvider(WidgetTheme.primaryColor),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
