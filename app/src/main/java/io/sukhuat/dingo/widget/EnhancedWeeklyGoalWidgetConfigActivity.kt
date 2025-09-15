package io.sukhuat.dingo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.widget.persistence.WidgetPersistenceManager
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced widget configuration activity with improved persistence
 * Based on modern Android widget configuration best practices
 */
@AndroidEntryPoint
class EnhancedWeeklyGoalWidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var persistenceManager: WidgetPersistenceManager

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
                EnhancedWidgetConfigScreen(
                    widgetId = appWidgetId,
                    persistenceManager = persistenceManager,
                    onConfigComplete = { config ->
                        saveWidgetConfiguration(config)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfiguration(config: WidgetConfiguration) {
        lifecycleScope.launch {
            try {
                // Save global widget configuration
                persistenceManager.saveWidgetConfiguration(config.toPersistenceConfig())

                // Save widget-specific state
                val widgetState = WidgetPersistenceManager.WidgetState(
                    widgetId = appWidgetId,
                    weekOffset = 0,
                    lastSelectedWeek = -1,
                    lastSelectedYear = -1,
                    isConfigured = true,
                    errorState = null
                )
                persistenceManager.saveWidgetState(widgetState)

                // Update the widget
                updateWidget()

                // Set result and finish
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)
                finish()
            } catch (e: Exception) {
                android.util.Log.e("EnhancedWidgetConfig", "Error saving configuration", e)
                // You might want to show an error dialog here
                finish()
            }
        }
    }

    private suspend fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetProvider = WeeklyGoalWidgetProvider()

        // Trigger widget update
        widgetProvider.onUpdate(this, appWidgetManager, intArrayOf(appWidgetId))
    }

    private suspend fun getCurrentWidgetSize(): String {
        return persistenceManager.getWidgetConfigurationSync().widgetSize
    }

    companion object {
        fun createConfigIntent(context: Context, appWidgetId: Int): Intent {
            return Intent(context, EnhancedWeeklyGoalWidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
        }
    }
}

/**
 * Widget configuration data class
 */
data class WidgetConfiguration(
    val size: EnhancedWidgetSize = EnhancedWidgetSize.SIZE_2X3,
    val showWeekNavigation: Boolean = true,
    val autoUpdate: Boolean = true,
    val updateInterval: Int = 15,
    val theme: EnhancedWidgetTheme = EnhancedWidgetTheme.AUTO,
    val showCompleted: Boolean = true,
    val compactMode: Boolean = false
) {
    fun toPersistenceConfig(): WidgetPersistenceManager.WidgetConfiguration {
        return WidgetPersistenceManager.WidgetConfiguration(
            widgetSize = size.value,
            showWeekNavigation = showWeekNavigation,
            autoUpdateEnabled = autoUpdate,
            updateIntervalMinutes = updateInterval,
            themeMode = theme.value,
            showCompletedGoals = showCompleted
        )
    }
}

enum class EnhancedWidgetSize(val value: String, val displayName: String) {
    SIZE_2X3("2x3", "Compact (2×3)"),
    SIZE_3X2("3x2", "Wide (3×2)"),
    SIZE_AUTO("auto", "Auto-detect")
}

enum class EnhancedWidgetTheme(val value: String, val displayName: String) {
    AUTO("auto", "Follow System"),
    LIGHT("light", "Light Theme"),
    DARK("dark", "Dark Theme")
}

@Composable
fun EnhancedWidgetConfigScreen(
    widgetId: Int,
    persistenceManager: WidgetPersistenceManager,
    onConfigComplete: (WidgetConfiguration) -> Unit,
    onCancel: () -> Unit
) {
    var config by remember { mutableStateOf(WidgetConfiguration()) }

    // Load existing configuration if available
    LaunchedEffect(widgetId) {
        try {
            val existingConfig = persistenceManager.getWidgetConfigurationSync()
            config = WidgetConfiguration(
                size = EnhancedWidgetSize.values().find { it.value == existingConfig.widgetSize } ?: EnhancedWidgetSize.SIZE_2X3,
                showWeekNavigation = existingConfig.showWeekNavigation,
                autoUpdate = existingConfig.autoUpdateEnabled,
                updateInterval = existingConfig.updateIntervalMinutes,
                theme = EnhancedWidgetTheme.values().find { it.value == existingConfig.themeMode } ?: EnhancedWidgetTheme.AUTO,
                showCompleted = existingConfig.showCompletedGoals
            )
        } catch (e: Exception) {
            android.util.Log.e("EnhancedWidgetConfig", "Error loading existing config", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "Configure Goal Widget",
            style = MaterialTheme.typography.headlineMedium
        )

        // Widget Size Selection
        ConfigSection(title = "Widget Size") {
            EnhancedWidgetSize.values().forEach { size ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = config.size == size,
                            onClick = { config = config.copy(size = size) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = config.size == size,
                        onClick = { config = config.copy(size = size) }
                    )
                    Text(
                        text = size.displayName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Display Options
        ConfigSection(title = "Display Options") {
            SwitchRow(
                label = "Show Week Navigation",
                checked = config.showWeekNavigation,
                onCheckedChange = { config = config.copy(showWeekNavigation = it) }
            )

            SwitchRow(
                label = "Show Completed Goals",
                checked = config.showCompleted,
                onCheckedChange = { config = config.copy(showCompleted = it) }
            )

            SwitchRow(
                label = "Compact Mode",
                checked = config.compactMode,
                onCheckedChange = { config = config.copy(compactMode = it) }
            )
        }

        // Theme Selection
        ConfigSection(title = "Theme") {
            EnhancedWidgetTheme.values().forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = config.theme == theme,
                            onClick = { config = config.copy(theme = theme) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = config.theme == theme,
                        onClick = { config = config.copy(theme = theme) }
                    )
                    Text(
                        text = theme.displayName,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Update Settings
        ConfigSection(title = "Update Settings") {
            SwitchRow(
                label = "Auto Update",
                checked = config.autoUpdate,
                onCheckedChange = { config = config.copy(autoUpdate = it) }
            )

            if (config.autoUpdate) {
                Text(
                    text = "Update Interval: ${config.updateInterval} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Slider(
                    value = config.updateInterval.toFloat(),
                    onValueChange = { config = config.copy(updateInterval = it.toInt()) },
                    valueRange = 5f..60f,
                    steps = 10,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onConfigComplete(config) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Widget")
            }
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
