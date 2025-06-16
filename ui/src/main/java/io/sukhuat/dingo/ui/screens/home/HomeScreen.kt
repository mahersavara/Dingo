package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.LocaleHelper
import io.sukhuat.dingo.common.localization.LanguagePreferences
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import io.sukhuat.dingo.common.components.popInAnimation
import io.sukhuat.dingo.common.components.rememberResponsiveValues
import io.sukhuat.dingo.common.components.ScreenSizeClass
import io.sukhuat.dingo.common.components.BubbleComponent
import io.sukhuat.dingo.common.components.MediaType
import io.sukhuat.dingo.common.components.GoalCompletionCelebration
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.ui.components.DingoAppScaffold
import io.sukhuat.dingo.ui.components.WeeklyWrapUp
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get responsive values
    val responsiveValues = rememberResponsiveValues()

    // Get state from view model
    val uiState by viewModel.uiState.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val completedGoals by viewModel.completedGoals.collectAsState()
    val archivedGoals by viewModel.archivedGoals.collectAsState()
    val soundEnabled by viewModel.soundEnabled
    val vibrationEnabled by viewModel.vibrationEnabled

    // Track if settings dialog is shown
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Track if bubble editor is shown
    var showBubbleEditor by remember { mutableStateOf(false) }
    var selectedGoalForEdit by remember { mutableStateOf<Goal?>(null) }
    var selectedGoalIndex by remember { mutableStateOf(-1) }
    var bubbleEditorPosition by remember { mutableStateOf(Pair(0f, 0f)) }

    // Setup media player for sound effects
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var soundPlaybackError by remember { mutableStateOf(false) }

    // Initialize variables that were showing as unresolved
    var completedGoal by remember { mutableStateOf<Goal?>(null) }
    var confettiTargetPosition by remember { mutableStateOf(Pair(0.5f, 0.3f)) }
    var showCelebration by remember { mutableStateOf(false) }

    // Function to handle language change
    val handleLanguageChange: (String) -> Unit = { languageCode ->
        // Save the language preference
        val languagePreferences = LanguagePreferences(context)
        coroutineScope.launch {
            // Save the language code to preferences
            languagePreferences.setLanguageCode(languageCode)
            
            // Apply the new locale
            LocaleHelper.setLocale(context, languageCode)
            
            // Recreate the activity to apply the language change
            if (context is android.app.Activity) {
                // Force activity recreation with animation
                val intent = context.intent
                context.finish()
                context.startActivity(intent)
                context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    // Initialize MediaPlayer for sound effects
    LaunchedEffect(Unit) {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.success_sound)
            mediaPlayer?.setOnErrorListener { _, _, _ ->
                soundPlaybackError = true
                true
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error initializing MediaPlayer", e)
            soundPlaybackError = true
        }
    }

    // Clean up media player when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error releasing MediaPlayer", e)
            }
        }
    }

    // Function to play success sound
    val playSuccessSound = {
        if (soundEnabled && !soundPlaybackError) {
            try {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error playing sound", e)
                soundPlaybackError = true
            }
        }

        // Provide haptic feedback as fallback or additional feedback
        if (vibrationEnabled) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        }
    }

    // Function to celebrate goal completion
    val celebrateGoalCompletion = { goal: Goal ->
        // Play sound
        playSuccessSound()

        // Trigger vibration
        if (vibrationEnabled) {
            viewModel.vibrateOnGoalCompleted()
        }

        // Show celebration animation
        completedGoal = goal
        confettiTargetPosition = Pair(0.5f, 0.3f)
        showCelebration = true
    }

    // Function to save custom image
    val saveCustomImage = { uri: Uri ->
        // In a real app, you would save the image to internal storage or cloud storage
        // For this example, we'll just return the URI as a string
        uri.toString()
    }

    // Function to show status change message
    val showStatusChangeMessage = { status: GoalStatus, goalText: String ->
        coroutineScope.launch {
            val message = when (status) {
                GoalStatus.COMPLETED -> "Goal completed: $goalText"
                GoalStatus.FAILED -> "Goal marked as failed: $goalText"
                GoalStatus.ARCHIVED -> "Goal archived: $goalText"
                GoalStatus.ACTIVE -> "Goal activated: $goalText"
            }
            snackbarHostState.showSnackbar(message = message)
        }
    }

    // Track if goal creation dialog is shown
    var showGoalCreationDialog by remember { mutableStateOf(false) }
    var selectedEmptyIndex by remember { mutableStateOf(-1) }

    // Track goal context menu
    var showGoalContextMenu by remember { mutableStateOf(false) }
    var contextMenuGoal by remember { mutableStateOf<Goal?>(null) }
    var contextMenuPosition by remember { mutableStateOf(Pair(0f, 0f)) }

    DingoAppScaffold(
        title = "Dingo",
        showTopBar = true,
        useGradientBackground = true,
        isLoading = uiState is HomeUiState.Loading,
        errorMessage = if (uiState is HomeUiState.Error) {
            (uiState as HomeUiState.Error).message
        } else {
            null
        },
        topBarActions = {
            // Remove the duplicate profile icon since it's already handled by the scaffold
        },
        showUserMenu = true,
        isAuthenticated = true, // Assuming the user is authenticated since we're on the home screen
        currentLanguage = LocalAppLanguage.current,
        onLanguageChange = handleLanguageChange,
        onSettingsClick = onNavigateToSettings,
        onLogoutClick = { viewModel.signOut(onSignOut) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use different layouts based on screen size
            when (responsiveValues.screenSizeClass) {
                ScreenSizeClass.COMPACT -> {
                    // Phone portrait layout - vertical
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(responsiveValues.contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Weekly overview header
                        WeeklyOverviewHeader(textSizeMultiplier = responsiveValues.headerTextSize)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Goals grid
                        GoalsGrid(
                            goals = goals,
                            onGoalClick = { goal ->
                                if (goal.status == GoalStatus.ACTIVE) {
                                    // Mark goal as complete
                                    viewModel.updateGoalStatus(goal.id, GoalStatus.COMPLETED)

                                    // Show celebration
                                    celebrateGoalCompletion(goal)
                                }
                            },
                            onGoalLongPress = { goal, position ->
                                // Show bubble editor
                                selectedGoalForEdit = goal
                                
                                // Calculate offset for the BubbleEditor to position it next to the goal
                                // Position the bubble to the right of the goal with a slight offset
                                val offsetX = position.first + 60f  // Move right from the goal
                                val offsetY = position.second - 30f  // Move slightly up
                                
                                bubbleEditorPosition = Pair(offsetX, offsetY)
                                showBubbleEditor = true
                            },
                            onEmptyCellClick = {
                                showGoalCreationDialog = true
                            }
                        )
                    }
                }

                ScreenSizeClass.MEDIUM, ScreenSizeClass.EXPANDED -> {
                    // Tablet layout - horizontal split
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(responsiveValues.contentPadding),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left side - Weekly overview
                        Column(
                            modifier = Modifier
                                .weight(0.3f)
                                .padding(end = responsiveValues.contentPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Weekly overview header
                            WeeklyOverviewHeader(textSizeMultiplier = responsiveValues.headerTextSize)

                            Spacer(modifier = Modifier.height(32.dp))

                            // Additional stats or info could go here
                            Text(
                                text = "Goal Statistics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize * responsiveValues.headerTextSize
                                ),
                                fontWeight = FontWeight.Bold,
                                color = RusticGold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sample stats
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val activeGoalsCount = goals.size
                                val completedGoalsCount = completedGoals.size
                                val totalGoalsCount =
                                    activeGoalsCount + completedGoalsCount + archivedGoals.size

                                StatItem(
                                    label = "Active Goals",
                                    value = "$activeGoalsCount",
                                    color = MaterialTheme.colorScheme.primary,
                                    textSizeMultiplier = responsiveValues.bodyTextSize
                                )

                                StatItem(
                                    label = "Completed Goals",
                                    value = "$completedGoalsCount",
                                    color = Color(0xFF4CAF50),
                                    textSizeMultiplier = responsiveValues.bodyTextSize
                                )

                                StatItem(
                                    label = "Completion Rate",
                                    value = "${if (totalGoalsCount > 0) (completedGoalsCount * 100 / totalGoalsCount) else 0}%",
                                    color = RusticGold,
                                    textSizeMultiplier = responsiveValues.bodyTextSize
                                )
                            }
                        }

                        // Divider
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Right side - Goals grid
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .padding(start = responsiveValues.contentPadding)
                        ) {
                            // Goals grid
                            GoalsGrid(
                                goals = goals,
                                onGoalClick = { goal ->
                                    if (goal.status == GoalStatus.ACTIVE) {
                                        // Mark goal as complete
                                        viewModel.updateGoalStatus(goal.id, GoalStatus.COMPLETED)

                                        // Show celebration
                                        celebrateGoalCompletion(goal)
                                    }
                                },
                                onGoalLongPress = { goal, position ->
                                    // Show bubble editor
                                    selectedGoalForEdit = goal
                                    
                                    // Calculate offset for the BubbleEditor to position it next to the goal
                                    // Position the bubble to the right of the goal with a slight offset
                                    val offsetX = position.first + 60f  // Move right from the goal
                                    val offsetY = position.second - 30f  // Move slightly up
                                    
                                    bubbleEditorPosition = Pair(offsetX, offsetY)
                                    showBubbleEditor = true
                                },
                                onEmptyCellClick = {
                                    showGoalCreationDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Goal completion celebration
            if (showCelebration && completedGoal != null) {
                GoalCompletionCelebration(
                    goalText = completedGoal!!.text,
                    imageResId = completedGoal!!.imageResId,
                    customImage = completedGoal!!.customImage,
                    targetPosition = confettiTargetPosition,
                    onAnimationEnd = {
                        showCelebration = false
                        completedGoal = null
                    }
                )
            }

            // Goal creation dialog
            if (showGoalCreationDialog) {
                GoalCreationDialog(
                    onDismiss = { showGoalCreationDialog = false },
                    onGoalCreated = { text, imageResId ->
                        viewModel.createGoal(text, imageResId)
                        showGoalCreationDialog = false
                    }
                )
            }

            // Bubble editor
            if (showBubbleEditor && selectedGoalForEdit != null) {
                BubbleComponent(
                    id = selectedGoalForEdit!!.id.hashCode(), // Convert string ID to int
                    text = selectedGoalForEdit!!.text,
                    imageResId = selectedGoalForEdit!!.imageResId,
                    customImage = selectedGoalForEdit!!.customImage,
                    createdAt = selectedGoalForEdit!!.createdAt,
                    position = bubbleEditorPosition,
                    onDismiss = { showBubbleEditor = false },
                    onTextChange = { newText ->
                        viewModel.updateGoalText(selectedGoalForEdit!!.id, newText)
                        // Don't dismiss the editor immediately to allow for multiple edits
                    },
                    onMediaUpload = { uri, mediaType ->
                        val savedImagePath = saveCustomImage(uri)
                        viewModel.updateGoalImage(selectedGoalForEdit!!.id, savedImagePath)

                        // Show update message based on media type
                        coroutineScope.launch {
                            val mediaTypeText = when (mediaType) {
                                MediaType.IMAGE -> "Image"
                                MediaType.GIF -> "GIF"
                                MediaType.STICKER -> "Sticker"
                            }
                            // Snackbar message is handled by the view model
                        }
                    },
                    onArchive = {
                        val goalId = selectedGoalForEdit!!.id
                        viewModel.updateGoalStatus(goalId, GoalStatus.ARCHIVED)
                        selectedGoalForEdit = null
                        showBubbleEditor = false
                    },
                    onDelete = {
                        val goalId = selectedGoalForEdit!!.id
                        viewModel.deleteGoal(goalId)
                        selectedGoalForEdit = null
                        showBubbleEditor = false
                    }
                )
            }

            // Settings dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    onSoundToggled = {
                        viewModel.toggleSound()
                    },
                    onVibrationToggled = {
                        viewModel.toggleVibration()
                    },
                    onDismiss = {
                        showSettingsDialog = false
                    }
                )
            }

            // Weekly wrap-up dialog
            if (viewModel.showWeeklyWrapUp.value) {
                WeeklyWrapUp(
                    completedGoals = completedGoals,
                    totalGoals = viewModel.totalWeeklyGoals.value,
                    onDismiss = { viewModel.dismissWeeklyWrapUp() },
                    onShare = {
                        // Create a share intent
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, viewModel.getWeeklySummaryText())
                            type = "text/plain"
                        }
                        
                        // Start the share activity
                        context.startActivity(Intent.createChooser(sendIntent, "Share Weekly Summary"))
                    }
                )
            }
        }
    }
}

@Composable
fun WeeklyOverviewHeader(textSizeMultiplier: Float = 1.0f) {
    // State to track the week offset (0 = current week, -1 = previous week, etc.)
    var weekOffset by remember { mutableStateOf(0) }
    
    // Calculate current week and days dynamically using Calendar
    val calendar = remember(weekOffset) { 
        Calendar.getInstance().apply {
            // Add weekOffset weeks to the current date
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
    }

    // 1 (Sunday) to 7 (Saturday) in Calendar, but we want 1 (Monday) to 7 (Sunday)
    val calendarDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    // Convert to 1 (Monday) to 7 (Sunday) format
    val dayOfWeek = if (calendarDayOfWeek == Calendar.SUNDAY) 7 else calendarDayOfWeek - 1

    val daysLeft = 8 - dayOfWeek // Days left in the week (including today)

    // Calculate week of month
    val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

    // Get month name
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())

    val year = calendar.get(Calendar.YEAR)

    val weekProgress = dayOfWeek.toFloat() / 7f
    
    // Detect horizontal swipe gestures
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset to current week on double tap
                        weekOffset = 0
                    }
                )
            }
            .pointerInput(Unit) {
                var startX = 0f
                var change = 0f
                detectTapGestures(
                    onPress = { offset ->
                        startX = offset.x
                    },
                    onLongPress = { offset ->
                        change = offset.x - startX
                        if (change > screenWidthPx / 4) {
                            // Swipe right - go to next week (if not already at current week)
                            if (weekOffset < 0) {
                                weekOffset++
                            }
                        } else if (change < -screenWidthPx / 4) {
                            // Swipe left - go to previous week
                            weekOffset--
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show week navigation indicator
        Text(
            text = when {
                weekOffset == 0 -> "Current Week"
                weekOffset == -1 -> "Last Week"
                weekOffset < -1 -> "${abs(weekOffset)} Weeks Ago"
                else -> "Future Week" // Should not happen with current logic
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = "${weekOfMonth}${getOrdinalSuffix(weekOfMonth)} Week of $monthName $year",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = MaterialTheme.typography.headlineSmall.fontSize * textSizeMultiplier
            ),
            fontWeight = FontWeight.Bold,
            color = RusticGold
        )

        // Show different text based on whether we're viewing current week or past weeks
        if (weekOffset == 0) {
            Text(
                text = "($daysLeft day${if (daysLeft != 1) "s" else ""} left!)",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSizeMultiplier
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Progress bar for current week
            LinearProgressIndicator(
                progress = weekProgress,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = RusticGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            // For past weeks, show completed status instead of progress
            Text(
                text = "Week completed",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSizeMultiplier
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Show swipe hint
            Text(
                text = "Swipe right to see more recent weeks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Show swipe left hint only for current week
        if (weekOffset == 0) {
            Text(
                text = "Swipe left to see past weeks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Helper function to get ordinal suffix (1st, 2nd, 3rd, 4th)
private fun getOrdinalSuffix(n: Int): String {
    return when {
        n in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
}

@Composable
fun GoalsGrid(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onEmptyCellClick: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val columns = GridCells.Fixed(3)
    val itemsVisible = true // For animations
    
    // Always show a grid of 12 cells (4 rows x 3 columns)
    val totalCells = 12
    val emptySlots = maxOf(0, totalCells - goals.size)

    LazyVerticalGrid(
        columns = columns,
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Display existing goals
        items(goals, key = { it.id }) { goal ->
            // Goal cell with animations
            GoalCell(
                goal = goal,
                modifier = Modifier
                    .padding(4.dp)
                    .popInAnimation(visible = itemsVisible, index = goals.indexOf(goal))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onGoalClick(goal) },
                            onLongPress = { offset ->
                                // Pass the position for context menu placement
                                onGoalLongPress(goal, Pair(offset.x, offset.y))
                            }
                        )
                    }
            )
        }
        
        // Add empty cells to fill the grid
        items(emptySlots) { index ->
            EmptyGoalCell(
                onClick = onEmptyCellClick,
                modifier = Modifier
                    .padding(4.dp)
                    .popInAnimation(visible = itemsVisible, index = goals.size + index)
            )
        }
    }
}

@Composable
fun GoalCell(
    goal: Goal,
    isDragged: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when (goal.status) {
                GoalStatus.COMPLETED -> Color(0xFFE8F5E9) // Light green for completed
                GoalStatus.FAILED -> Color(0xFFFFEBEE) // Light red for failed
                GoalStatus.ARCHIVED -> Color(0xFFF5F5F5) // Light gray for archived
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Image at the top
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (goal.customImage != null) {
                        AsyncImage(
                            model = goal.customImage,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .padding(4.dp)
                        )
                    } else if (goal.imageResId != null) {
                        // Use null-safe approach to avoid smart cast issue
                        val resId = goal.imageResId ?: R.drawable.ic_goal_notes
                        Icon(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            tint = Color.Unspecified, // Use original colors for doodle art style
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Text below the image
                Text(
                    text = goal.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (goal.status) {
                        GoalStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Dimmed text for archived
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
            
            // Status overlays
            when (goal.status) {
                GoalStatus.COMPLETED -> {
                    // Overlay for completed goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x33FFFFFF)), // Semi-transparent white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        // Stylized completion mark overlay
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // Outer circle
                            drawCircle(
                                color = Color(0xCC4CAF50), // Semi-transparent green
                                radius = size.minDimension / 2,
                                style = Stroke(width = 4f)
                            )
                            
                            // Inner circle
                            drawCircle(
                                color = Color(0x334CAF50), // More transparent green
                                radius = size.minDimension / 2 - 8f
                            )
                            
                            // Decorative lines
                            for (i in 0 until 8) {
                                rotate(degrees = i * 45f) {
                                    drawLine(
                                        color = Color(0xCC4CAF50),
                                        start = center + Offset(0f, -size.minDimension / 4),
                                        end = center + Offset(0f, -size.minDimension / 2 + 4f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }
                        
                        // "DONE" text in the center of the stamp
                        Text(
                            text = "DONE",
                            color = Color(0xFF4CAF50), // Green color
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        )
                    }
                }
                GoalStatus.FAILED -> {
                    // Overlay for failed goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x33FFFFFF)), // Semi-transparent white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // X mark
                            drawLine(
                                color = Color(0xCCFF5252), // Semi-transparent red
                                start = Offset(size.width * 0.3f, size.height * 0.3f),
                                end = Offset(size.width * 0.7f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = Color(0xCCFF5252), // Semi-transparent red
                                start = Offset(size.width * 0.7f, size.height * 0.3f),
                                end = Offset(size.width * 0.3f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                        }
                        
                        Text(
                            text = "FAILED",
                            color = Color(0xFFFF5252), // Red color
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                                .padding(top = 40.dp)
                        )
                    }
                }
                GoalStatus.ARCHIVED -> {
                    // Overlay for archived goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x33FFFFFF)), // Semi-transparent white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // Outer rectangle with rounded corners
                            drawRoundRect(
                                color = Color(0xCC9E9E9E), // Semi-transparent gray
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 3f)
                            )
                        }
                        
                        Text(
                            text = "ARCHIVED",
                            color = Color(0xFF9E9E9E), // Gray color
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        )
                    }
                }
                else -> {
                    // No overlay for active goals
                }
            }
        }
    }
}

@Composable
fun EmptyGoalCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Goal",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun GoalCreationDialog(
    onDismiss: () -> Unit,
    onGoalCreated: (text: String, imageResId: Int?) -> Unit
) {
    var goalText by remember { mutableStateOf("") }
    var selectedImageResId by remember { mutableStateOf<Int?>(R.drawable.ic_goal_notes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create New Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Goal text input
                androidx.compose.material3.OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Goal Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon selection grid
                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Icon grid
                val iconOptions = listOf(
                    R.drawable.ic_goal_learn,
                    R.drawable.ic_goal_book,
                    R.drawable.ic_goal_debt,
                    R.drawable.ic_goal_save,
                    R.drawable.ic_goal_travel,
                    R.drawable.ic_goal_walk,
                    R.drawable.ic_goal_steps,
                    R.drawable.ic_goal_notes,
                    R.drawable.ic_goal_organize
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    items(iconOptions.size) { index ->
                        val iconResId = iconOptions[index]
                        IconSelectionItem(
                            iconResId = iconResId,
                            isSelected = selectedImageResId == iconResId,
                            onClick = { selectedImageResId = iconResId }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    androidx.compose.material3.Button(
                        onClick = {
                            if (goalText.isNotBlank()) {
                                onGoalCreated(goalText, selectedImageResId)
                            }
                        },
                        enabled = goalText.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun IconSelectionItem(
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(32.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.6f
            )
        )
    }
}

@Composable
fun GoalContextMenu(
    position: Pair<Float, Float>,
    goalStatus: GoalStatus,
    onDismiss: () -> Unit,
    onStatusChange: (GoalStatus) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Calculate menu dimensions and constraints
    val menuWidth = 200.dp
    val menuHeight = 300.dp
    val menuWidthPx = with(density) { menuWidth.roundToPx() }
    val menuHeightPx = with(density) { menuHeight.roundToPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    // Calculate position, ensuring menu stays within screen bounds
    val xPos = position.first.coerceIn(0f, (screenWidthPx - menuWidthPx).toFloat())
    val yPos = position.second.coerceIn(0f, (screenHeightPx - menuHeightPx).toFloat())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .width(menuWidth)
                .padding(8.dp)
                .align(Alignment.TopStart)
                .offset(
                    x = with(density) { xPos.toDp() },
                    y = with(density) { yPos.toDp() }
                ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Goal Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )

                Divider()

                // Status options
                StatusMenuItem(
                    text = "Mark as Active",
                    icon = R.drawable.ic_goal_steps,
                    enabled = goalStatus != GoalStatus.ACTIVE,
                    onClick = { onStatusChange(GoalStatus.ACTIVE) }
                )

                StatusMenuItem(
                    text = "Mark as Completed",
                    icon = R.drawable.ic_goal_completed,
                    enabled = goalStatus != GoalStatus.COMPLETED,
                    onClick = { onStatusChange(GoalStatus.COMPLETED) }
                )

                StatusMenuItem(
                    text = "Mark as Failed",
                    icon = R.drawable.ic_goal_walk, // Using walk icon as placeholder
                    enabled = goalStatus != GoalStatus.FAILED,
                    onClick = { onStatusChange(GoalStatus.FAILED) }
                )

                StatusMenuItem(
                    text = "Archive Goal",
                    icon = R.drawable.ic_goal_organize,
                    enabled = goalStatus != GoalStatus.ARCHIVED,
                    onClick = { onStatusChange(GoalStatus.ARCHIVED) }
                )

                Divider()

                // Delete option
                StatusMenuItem(
                    text = "Delete Goal",
                    icon = R.drawable.ic_delete_goal,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun StatusMenuItem(
    text: String,
    icon: Int,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSoundToggled: () -> Unit,
    onVibrationToggled: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sound setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sound),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Sound Effects",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { onSoundToggled() }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Vibration setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vibrate),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Vibration Feedback",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { onVibrationToggled() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MountainSunriseTheme {
        Surface {
            HomeScreen(onSignOut = {})
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    textSizeMultiplier: Float = 1f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize * textSizeMultiplier
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize * textSizeMultiplier,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
