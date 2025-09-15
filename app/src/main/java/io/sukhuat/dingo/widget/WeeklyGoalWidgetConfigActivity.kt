package io.sukhuat.dingo.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.AndroidEntryPoint
import io.sukhuat.dingo.common.theme.DingoTheme
import kotlinx.coroutines.launch

/**
 * Configuration activity for Weekly Goal Widget
 * Shows widget size selection and other preferences
 */
@AndroidEntryPoint
class WeeklyGoalWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If widget ID is invalid, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            DingoTheme {
                WeeklyGoalWidgetConfigScreen(
                    onConfigComplete = { selectedSize, showWeekNavigation ->
                        saveWidgetConfig(selectedSize, showWeekNavigation)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfig(selectedSize: WidgetSize, showWeekNavigation: Boolean) {
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            try {
                android.util.Log.d("WidgetConfig", "=== SAVING CONFIG START ===")
                android.util.Log.d("WidgetConfig", "Selected Size: $selectedSize")
                android.util.Log.d("WidgetConfig", "Widget ID: $appWidgetId")
                android.util.Log.d("WidgetConfig", "Show Navigation: $showWeekNavigation")

                // Trigger the single widget
                android.util.Log.d("WidgetConfig", "🎯 Triggering widget updateAll")
                WeeklyGoalWidget.updateAll(this@WeeklyGoalWidgetConfigActivity)

                // Also manually trigger provider
                val intent = android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                intent.component = android.content.ComponentName(this@WeeklyGoalWidgetConfigActivity, WeeklyGoalWidgetProvider::class.java)
                intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                sendBroadcast(intent)

                android.util.Log.d("WidgetConfig", "✅ Widget update triggered successfully")

                // Return successful result
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                android.util.Log.d("WidgetConfig", "✅ Configuration activity finishing with RESULT_OK")
                finish()
            } catch (e: Exception) {
                android.util.Log.e("WidgetConfig", "❌ Error saving widget config", e)

                // Even if there's an error, try to return OK
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            }
        }
    }

    companion object {
        val WIDGET_SIZE_KEY = stringPreferencesKey("widget_size")
        val SHOW_WEEK_NAVIGATION_KEY = booleanPreferencesKey("show_week_navigation")
    }
}

enum class WidgetSize(val displayName: String, val description: String) {
    SIZE_2X2("2×2", "Compact - 4 goals"),
    SIZE_2X3("2×3", "Vertical - 6 goals"),
    SIZE_3X2("3×2", "Horizontal - 6 goals")
}

@Composable
private fun WeeklyGoalWidgetConfigScreen(
    onConfigComplete: (WidgetSize, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var selectedSize by remember { mutableStateOf(WidgetSize.SIZE_2X2) }
    var showWeekNavigation by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2E9)) // Mountain Sunrise background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Configure Weekly Goal Widget",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Size Selection
            Text(
                text = "Choose widget size:",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF92400E)
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WidgetSize.values().forEach { size ->
                    WidgetSizeCard(
                        size = size,
                        isSelected = selectedSize == size,
                        onClick = { selectedSize = size }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Week Navigation Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFBEB)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWeekNavigation = !showWeekNavigation }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Week Navigation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E)
                            )
                        )
                        Text(
                            text = "Show previous/next week buttons",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                    }

                    Switch(
                        checked = showWeekNavigation,
                        onCheckedChange = { showWeekNavigation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFD97706),
                            checkedTrackColor = Color(0xFFFED7AA)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Cancel")
                }

                // Add Widget Button
                Button(
                    onClick = { onConfigComplete(selectedSize, showWeekNavigation) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706)
                    )
                ) {
                    Text("Add Widget", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun WidgetSizeCard(
    size: WidgetSize,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFED7AA) else Color(0xFFFFFBEB)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = size.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                )
                Text(
                    text = size.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    )
                )
            }

            // Visual representation of widget size
            Box(
                modifier = Modifier
                    .size(
                        width = when (size) {
                            WidgetSize.SIZE_2X2 -> 40.dp
                            WidgetSize.SIZE_2X3 -> 40.dp
                            WidgetSize.SIZE_3X2 -> 60.dp
                        },
                        height = when (size) {
                            WidgetSize.SIZE_2X2 -> 40.dp
                            WidgetSize.SIZE_2X3 -> 60.dp
                            WidgetSize.SIZE_3X2 -> 40.dp
                        }
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) Color(0xFFD97706) else Color(0xFF92400E)
                    )
            )
        }
    }
}
