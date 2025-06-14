package io.sukhuat.dingo.ui.screens.home

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.changeAppLanguage
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

// Goal status enum
enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    ARCHIVED
}

// Goal data class
data class Goal(
    val id: Int,
    val text: String,
    val imageResId: Int? = null,
    var status: GoalStatus = GoalStatus.ACTIVE
)

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage = LocalAppLanguage.current

    // Track settings dialog visibility
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Track sound and vibration settings
    var soundEnabled by remember { mutableStateOf(viewModel.isSoundEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(viewModel.isVibrationEnabled()) }

    // Create MediaPlayer for sound effects with error handling and fallback
    var successSoundPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var soundPlaybackError by remember { mutableStateOf(false) }

    // Initialize MediaPlayer
    DisposableEffect(Unit) {
        try {
            successSoundPlayer = MediaPlayer.create(context, R.raw.success_sound)

            // Set error listener
            successSoundPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("HomeScreen", "MediaPlayer error: what=$what, extra=$extra")
                soundPlaybackError = true
                true // We handled the error
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error creating MediaPlayer: ${e.message}")
            soundPlaybackError = true
        }

        // Release the MediaPlayer when no longer needed
        onDispose {
            try {
                successSoundPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                successSoundPlayer = null
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error releasing MediaPlayer: ${e.message}")
            }
        }
    }

    // Function to play success sound with fallback to vibration
    fun playSuccessSound() {
        if (!viewModel.isSoundEnabled()) return

        try {
            if (soundPlaybackError || successSoundPlayer == null) {
                // Fallback to vibration if sound fails
                viewModel.vibrateOnGoalCompleted()
                return
            }

            successSoundPlayer?.apply {
                if (isPlaying) {
                    seekTo(0)
                } else {
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error playing sound: ${e.message}")
            soundPlaybackError = true
            // Fallback to vibration
            viewModel.vibrateOnGoalCompleted()
        }
    }

    // Function to provide haptic feedback
    fun provideHapticFeedback() {
        if (!viewModel.isVibrationEnabled()) return
        viewModel.vibrateOnGoalCompleted()
    }

    // Function to celebrate goal completion with sound and vibration
    fun celebrateGoalCompletion() {
        playSuccessSound()
        provideHapticFeedback()
    }

    // Function to show status change message
    fun showStatusChangeMessage(status: GoalStatus, goalText: String) {
        coroutineScope.launch {
            val message = when (status) {
                GoalStatus.COMPLETED -> "Goal completed: $goalText"
                GoalStatus.FAILED -> "Goal marked as failed: $goalText"
                GoalStatus.ARCHIVED -> "Goal archived: $goalText"
                GoalStatus.ACTIVE -> "Goal marked as active: $goalText"
            }
            snackbarHostState.showSnackbar(message = message)
        }
    }

    // Sample goals for demonstration
    val goals = remember {
        mutableStateListOf(
            Goal(1, "Learn new skillset", R.drawable.ic_goal_learn),
            Goal(2, "Read 1 book a month", R.drawable.ic_goal_book, GoalStatus.COMPLETED),
            Goal(3, "Debt Free", R.drawable.ic_goal_debt),
            Goal(4, "Save $8,000", R.drawable.ic_goal_save),
            Goal(5, "Visit somewhere new", R.drawable.ic_goal_travel),
            Goal(6, "Take daily walk", R.drawable.ic_goal_walk, GoalStatus.FAILED),
            Goal(7, "10,000 steps a day", R.drawable.ic_goal_steps),
            Goal(8, "Take notes", R.drawable.ic_goal_notes),
            Goal(9, "Keep home organized", R.drawable.ic_goal_organize, GoalStatus.ARCHIVED),
            // Empty cells for new goals
            null,
            null,
            null
        )
    }

    // Track if goal creation dialog is shown
    var showGoalCreationDialog by remember { mutableStateOf(false) }
    var selectedEmptyIndex by remember { mutableStateOf(-1) }

    // Track confetti animation
    var showConfetti by remember { mutableStateOf(false) }
    var confettiTargetPosition by remember { mutableStateOf(Pair(0f, 0f)) }

    // Track goal context menu
    var showGoalContextMenu by remember { mutableStateOf(false) }
    var selectedGoalIndex by remember { mutableStateOf(-1) }
    var contextMenuPosition by remember { mutableStateOf(Pair(0f, 0f)) }

    // Show error messages in snackbar
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Error) {
            snackbarHostState.showSnackbar(message = (uiState as HomeUiState.Error).message)
        }
    }

    DingoScaffold(
        title = stringResource(R.string.app_name),
        useGradientBackground = true,
        snackbarHostState = snackbarHostState,
        isLoading = uiState is HomeUiState.Loading,
        // Add user dropdown menu
        showUserMenu = true,
        isAuthenticated = true, // User is authenticated on the home screen
        currentLanguage = currentLanguage,
        onProfileClick = {
            // TODO: Navigate to profile screen
        },
        onLanguageChange = { languageCode ->
            coroutineScope.launch {
                changeAppLanguage(context, languageCode)
            }
        },
        onSettingsClick = {
            // Show settings dialog
            showSettingsDialog = true
        },
        onLogoutClick = {
            viewModel.signOut(onSignOut)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Weekly overview header
                WeeklyOverviewHeader()

                Spacer(modifier = Modifier.height(24.dp))

                // Goals grid
                GoalsGrid(
                    goals = goals,
                    onGoalClick = { index ->
                        if (goals[index] == null) {
                            // Show goal creation dialog for empty cell
                            selectedEmptyIndex = index
                            showGoalCreationDialog = true
                        } else {
                            // Mark goal as complete and show confetti
                            if (goals[index]?.status == GoalStatus.ACTIVE) {
                                val goalText = goals[index]?.text ?: ""
                                goals[index] = goals[index]?.copy(status = GoalStatus.COMPLETED)
                                // Set confetti target position to the center of the screen
                                confettiTargetPosition = Pair(0.5f, 0.3f)
                                showConfetti = true
                                // Celebrate with sound and vibration
                                celebrateGoalCompletion()
                                // Show snackbar message
                                showStatusChangeMessage(GoalStatus.COMPLETED, goalText)
                            }
                        }
                    },
                    onGoalLongPress = { index, position ->
                        if (goals[index] != null) {
                            selectedGoalIndex = index
                            contextMenuPosition = position
                            showGoalContextMenu = true
                        }
                    }
                )
            }

            // Confetti animation
            if (showConfetti) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Simple confetti animation using Canvas
                    ConfettiAnimation(
                        targetPosition = confettiTargetPosition,
                        onAnimationEnd = { showConfetti = false }
                    )
                }

                LaunchedEffect(showConfetti) {
                    kotlinx.coroutines.delay(1500)
                    showConfetti = false
                }
            }

            // Goal creation dialog
            if (showGoalCreationDialog) {
                GoalCreationDialog(
                    onDismiss = { showGoalCreationDialog = false },
                    onGoalCreated = { text, imageResId ->
                        if (selectedEmptyIndex >= 0 && selectedEmptyIndex < goals.size) {
                            goals[selectedEmptyIndex] = Goal(
                                id = goals.filterNotNull().maxOfOrNull { it.id }?.plus(1) ?: 1,
                                text = text,
                                imageResId = imageResId ?: R.drawable.ic_goal_notes,
                                status = GoalStatus.ACTIVE
                            )
                        }
                        showGoalCreationDialog = false
                    }
                )
            }

            // Goal context menu
            if (showGoalContextMenu && selectedGoalIndex >= 0 && selectedGoalIndex < goals.size && goals[selectedGoalIndex] != null) {
                val goal = goals[selectedGoalIndex]!!

                GoalContextMenu(
                    position = contextMenuPosition,
                    goalStatus = goal.status,
                    onDismiss = { showGoalContextMenu = false },
                    onStatusChange = { newStatus ->
                        val goalText = goal.text
                        goals[selectedGoalIndex] = goal.copy(status = newStatus)
                        showGoalContextMenu = false

                        // Show confetti and celebrate when marking as completed
                        if (newStatus == GoalStatus.COMPLETED) {
                            confettiTargetPosition = Pair(0.5f, 0.3f)
                            showConfetti = true
                            celebrateGoalCompletion()
                        }

                        // Show status change message
                        showStatusChangeMessage(newStatus, goalText)
                    },
                    onDelete = {
                        // Replace with null to create an empty cell
                        val goalText = goal.text
                        goals[selectedGoalIndex] = null
                        showGoalContextMenu = false

                        // Show deletion message
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message = "Goal deleted: $goalText")
                        }
                    }
                )
            }

            // Settings dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    onSoundToggled = {
                        soundEnabled = viewModel.toggleSound()
                    },
                    onVibrationToggled = {
                        vibrationEnabled = viewModel.toggleVibration()
                    },
                    onDismiss = {
                        showSettingsDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun WeeklyOverviewHeader() {
    // Calculate current week and days dynamically using Calendar instead of LocalDate
    val calendar = remember { Calendar.getInstance() }

    // 1 (Sunday) to 7 (Saturday) in Calendar, but we want 1 (Monday) to 7 (Sunday)
    val calendarDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    // Convert to 1 (Monday) to 7 (Sunday) format
    val dayOfWeek = remember { if (calendarDayOfWeek == Calendar.SUNDAY) 7 else calendarDayOfWeek - 1 }

    val daysLeft = remember { 8 - dayOfWeek } // Days left in the week (including today)

    // Calculate week of month
    val weekOfMonth = remember {
        calendar.get(Calendar.WEEK_OF_MONTH)
    }

    // Get month name
    val monthName = remember {
        calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
    }

    val year = remember { calendar.get(Calendar.YEAR) }

    val weekProgress = remember { dayOfWeek.toFloat() / 7f }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${weekOfMonth}${getOrdinalSuffix(weekOfMonth)} Week of $monthName $year",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = RusticGold
        )

        Text(
            text = "($daysLeft day${if (daysLeft != 1) "s" else ""} left!)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Progress bar for week
        LinearProgressIndicator(
            progress = weekProgress,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = RusticGold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
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

// Extension function to convert dp to pixels
@Composable
private fun Int.dpToPx(): Float {
    return with(LocalDensity.current) { this@dpToPx.dp.toPx() }
}

// Extension function to convert dp to pixels
@Composable
private fun androidx.compose.ui.unit.Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalsGrid(
    goals: List<Goal?>,
    onGoalClick: (Int) -> Unit,
    onGoalLongPress: (Int, Pair<Float, Float>) -> Unit
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Track drag and drop state
    var isDragging by remember { mutableStateOf(false) }
    var draggedItemIndex by remember { mutableStateOf(-1) }
    var draggedItemPosition by remember { mutableStateOf(Offset.Zero) }
    var dropTargetIndex by remember { mutableStateOf(-1) }

    // Function to handle goal reordering
    val onReorderGoals = { from: Int, to: Int ->
        if (from != to && from >= 0 && to >= 0 && from < goals.size && to < goals.size) {
            val mutableGoals = goals.toMutableList()
            val item = mutableGoals[from]
            mutableGoals.removeAt(from)
            mutableGoals.add(to, item)
            // Update the goals list (this would be handled by the ViewModel in a real app)
            // For now, we'll just log the reordering
            Log.d("GoalsGrid", "Reordered goal from $from to $to")
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(goals.size, key = { index -> goals[index]?.id ?: -index }) { index ->
            val goal = goals[index]

            if (goal == null) {
                // Empty cell
                EmptyGoalCell(onClick = { onGoalClick(index) })
            } else {
                // Goal cell
                GoalCell(
                    goal = goal,
                    isDragged = isDragging && draggedItemIndex == index,
                    modifier = Modifier
                        .animateItemPlacement()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onGoalClick(index) },
                                onLongPress = { offset ->
                                    // Pass the position for context menu placement
                                    onGoalLongPress(index, Pair(offset.x, offset.y))
                                }
                            )
                        }
                        .pointerInput(index) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    draggedItemIndex = index
                                    draggedItemPosition = it
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggedItemPosition += dragAmount

                                    // Calculate potential drop target based on position
                                    // This is a simplified implementation - in a real app,
                                    // you would calculate this based on the grid layout
                                    val row = (draggedItemPosition.y / 150f).toInt()
                                    val col = (draggedItemPosition.x / 150f).toInt()
                                    val potentialTarget = row * 3 + col

                                    if (potentialTarget >= 0 && potentialTarget < goals.size) {
                                        dropTargetIndex = potentialTarget
                                    }
                                },
                                onDragEnd = {
                                    if (dropTargetIndex != -1 && draggedItemIndex != dropTargetIndex) {
                                        onReorderGoals(draggedItemIndex, dropTargetIndex)
                                    }
                                    isDragging = false
                                    draggedItemIndex = -1
                                    dropTargetIndex = -1
                                },
                                onDragCancel = {
                                    isDragging = false
                                    draggedItemIndex = -1
                                    dropTargetIndex = -1
                                }
                            )
                        }
                )
            }
        }
    }
}

@Composable
fun GoalCell(
    goal: Goal,
    isDragged: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Determine background color based on status
    val backgroundColor = when (goal.status) {
        GoalStatus.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant
        GoalStatus.COMPLETED -> Color(0xFFD7F9DB) // Green pastel
        GoalStatus.FAILED -> Color(0xFFF9D7D7) // Red pastel
        GoalStatus.ARCHIVED -> Color(0xFFE0E0E0) // Gray
    }

    // Determine if cell is enabled
    val isEnabled = goal.status != GoalStatus.ARCHIVED

    // Apply elevation effect when dragging
    val elevation = if (isDragged) 8.dp else if (isEnabled) 4.dp else 1.dp
    val scale = if (isDragged) 1.05f else 1f

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                // Status indicator
                when (goal.status) {
                    GoalStatus.COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    GoalStatus.FAILED -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFF44336), CircleShape)
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Failed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    else -> { /* No indicator for other statuses */ }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Goal image/icon
                goal.imageResId?.let { resId ->
                    Icon(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Goal text
                Text(
                    text = goal.text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyGoalCell(onClick: () -> Unit) {
    Card(
        modifier = Modifier
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
                imageVector = Icons.Default.Add,
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
    var selectedImageResId by remember { mutableStateOf<Int?>(null) }
    var showIconSelector by remember { mutableStateOf(false) }

    // List of available goal icons
    val goalIcons = remember {
        listOf(
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
    }

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
                    text = "Add New Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { if (it.length <= 30) goalText = it },
                    label = { Text("Goal (max 30 chars)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Image selection with preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Icon:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Show selected icon or placeholder
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showIconSelector = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageResId != null) {
                            Icon(
                                painter = painterResource(id = selectedImageResId!!),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Select Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (selectedImageResId == null) "Tap to select" else "Tap to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Icon selector grid
                AnimatedVisibility(
                    visible = showIconSelector,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Select an icon:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            items(goalIcons.size) { index ->
                                val iconResId = goalIcons[index]
                                val isSelected = selectedImageResId == iconResId

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable {
                                            selectedImageResId = iconResId
                                            showIconSelector = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showIconSelector = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (goalText.isNotBlank()) {
                                onGoalCreated(goalText, selectedImageResId)
                            }
                        },
                        enabled = goalText.isNotBlank()
                    ) {
                        Text("Add Goal")
                    }
                }
            }
        }
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
fun ConfettiAnimation(
    targetPosition: Pair<Float, Float>,
    onAnimationEnd: () -> Unit
) {
    // Simple animation that shows colorful particles
    val colors = listOf(
        Color(0xFFFCE18A), Color(0xFFFF726D), Color(0xFFF4306D), Color(0xFFB48DEF), Color(0xFF42A5F5),
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFF03A9F4)
    )

    // Use built-in animation components instead of KonfettiView
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Create animated dots as confetti
        for (i in 0 until 30) {
            val color = colors[i % colors.size]
            val delay = (i * 50).toLong()

            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -it }
                ) + androidx.compose.animation.fadeIn(
                    initialAlpha = 0f
                ),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = ((-15..15).random()).dp,
                            y = ((-15..15).random()).dp
                        )
                        .size((5..12).random().dp)
                        .background(color, CircleShape)
                )
            }

            // Trigger end of animation
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1500)
                onAnimationEnd()
            }
        }
    }
}
