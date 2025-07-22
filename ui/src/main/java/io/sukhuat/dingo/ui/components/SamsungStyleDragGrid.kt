package io.sukhuat.dingo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.sukhuat.dingo.domain.model.Goal
import kotlinx.coroutines.delay

/**
 * Samsung/One UI launcher-style drag and drop grid
 * Touch behavior:
 * - Single tap: Execute action
 * - Long press (500ms, no movement): Show edit popup
 * - Long press + drag: Hide popup, start drag mode with floating icon
 */
@Composable
fun SamsungStyleDragGrid(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onGoalReorder: (Goal, Int) -> Unit,
    onEmptyPositionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Create a fixed 12-position grid
    val gridPositions = Array<Goal?>(12) { null }

    // Populate grid based on position field, not creation order
    goals.forEach { goal ->
        val position = goal.position.coerceIn(0, 11)
        if (gridPositions[position] == null) {
            gridPositions[position] = goal
        } else {
            // Handle position conflict - find next available slot
            val nextAvailable = gridPositions.indexOfFirst { it == null }
            if (nextAvailable != -1) {
                gridPositions[nextAvailable] = goal
            }
        }
    }

    // Samsung-style drag state
    var dragState by remember { mutableStateOf<SamsungDragState>(SamsungDragState.Idle) }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }

    // Grid position calculator
    val calculateDropPosition = rememberSamsungGridPositionCalculator(
        gridBounds = gridBounds,
        columns = 3,
        rows = 4
    )

    Box(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    gridBounds = Rect(
                        offset = coordinates.positionInParent(),
                        size = androidx.compose.ui.geometry.Size(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    )
                }
        ) {
            itemsIndexed(gridPositions.toList()) { index, goal ->
                if (goal != null) {
                    SamsungStyleGoalItem(
                        goal = goal,
                        isDraggedAway = (dragState as? SamsungDragState.Dragging)?.goal?.id == goal.id,
                        isDropTarget = (dragState as? SamsungDragState.Dragging)?.let { calculateDropPosition(it.currentPosition) == index } ?: false,
                        onGoalClick = onGoalClick,
                        onGoalLongPress = onGoalLongPress,
                        onDragStart = { startPos ->
                            dragState = SamsungDragState.Dragging(goal, startPos, goal.position)
                        },
                        onDragUpdate = { newPos ->
                            if (dragState is SamsungDragState.Dragging) {
                                dragState = (dragState as SamsungDragState.Dragging).copy(currentPosition = newPos)
                            }
                        },
                        onDragEnd = { finalPos ->
                            if (dragState is SamsungDragState.Dragging) {
                                val dropPosition = calculateDropPosition(finalPos)
                                if (dropPosition != goal.position && dropPosition in 0..11) {
                                    onGoalReorder(goal, dropPosition)
                                }
                                dragState = SamsungDragState.Idle
                            }
                        },
                        modifier = Modifier.aspectRatio(1f)
                    )
                } else {
                    SamsungEmptyGridPosition(
                        position = index,
                        onTap = { onEmptyPositionClick(index) },
                        modifier = Modifier.aspectRatio(1f),
                        isHighlighted = (dragState as? SamsungDragState.Dragging)?.let { calculateDropPosition(it.currentPosition) == index } ?: false
                    )
                }
            }
        }

        // Floating dragged item overlay
        (dragState as? SamsungDragState.Dragging)?.let { state ->
            FloatingDraggedItem(
                goal = state.goal,
                position = state.currentPosition,
                modifier = Modifier.zIndex(10f)
            )
        }
    }
}

/**
 * Samsung-style drag states
 */
sealed class SamsungDragState {
    data object Idle : SamsungDragState()
    data class Dragging(
        val goal: Goal,
        val currentPosition: Offset,
        val startGridPosition: Int
    ) : SamsungDragState()
}

/**
 * Samsung-style goal item with proper touch detection
 */
@Composable
fun SamsungStyleGoalItem(
    goal: Goal,
    isDraggedAway: Boolean,
    isDropTarget: Boolean,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var touchState by remember { mutableStateOf<TouchState>(TouchState.Idle) }
    var showEditPopup by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Touch configuration
    val longPressTimeoutMs = 500L
    val dragThreshold = with(density) { 10.dp.toPx() }

    // Smooth animations for Samsung-style feel
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isDraggedAway) 0.3f else 1f,
        animationSpec = animationSpec,
        label = "alpha_animation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = when {
            isDropTarget -> 1.1f
            touchState is TouchState.PressDown || touchState is TouchState.LongPressDetected -> 0.95f
            else -> 1f
        },
        animationSpec = animationSpec,
        label = "scale_animation"
    )

    // Handle long press detection with LaunchedEffect
    LaunchedEffect(touchState) {
        if (touchState is TouchState.PressDown) {
            val pressState = touchState as TouchState.PressDown
            delay(longPressTimeoutMs)
            if (touchState is TouchState.PressDown && touchState == pressState) {
                // Long press detected - show popup
                showEditPopup = true
                val newState = TouchState.LongPressDetected(pressState.startPosition)
                touchState = newState
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onGoalLongPress(goal, Pair(pressState.startPosition.x, pressState.startPosition.y))
            }
        }
    }

    Box(
        modifier = modifier
            .alpha(alphaAnimation)
            .scale(scaleAnimation)
            .pointerInput(goal.id) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        // Drag started - this means user is moving finger
                        // Check if we're in long press mode to start drag
                        if (touchState is TouchState.LongPressDetected) {
                            // Long press + movement = start drag mode
                            showEditPopup = false
                            touchState = TouchState.Dragging(startOffset)
                            onDragStart(startOffset)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            // Set press state for quick detection
                            touchState = TouchState.PressDown(startOffset, System.currentTimeMillis())
                        }
                    },
                    onDrag = { change, _ ->
                        when (val state = touchState) {
                            is TouchState.PressDown -> {
                                // Check if enough time passed for long press during drag
                                val elapsedTime = System.currentTimeMillis() - state.startTime
                                val distance = calculateDistance(state.startPosition, change.position)

                                if (distance > dragThreshold) {
                                    if (elapsedTime >= longPressTimeoutMs) {
                                        // Long press + drag = start drag mode
                                        showEditPopup = false
                                        touchState = TouchState.Dragging(change.position)
                                        onDragStart(change.position)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        // Quick movement - cancel and do nothing
                                        touchState = TouchState.Idle
                                    }
                                }
                            }
                            is TouchState.Dragging -> {
                                // Continue dragging
                                touchState = TouchState.Dragging(change.position)
                                onDragUpdate(change.position)
                            }
                            else -> { /* Other states */ }
                        }
                    },
                    onDragEnd = {
                        when (val state = touchState) {
                            is TouchState.Dragging -> {
                                // Complete drag operation
                                onDragEnd(state.currentPosition)
                            }
                            else -> {
                                // Handle as tap if not dragging
                                onGoalClick(goal)
                            }
                        }
                        touchState = TouchState.Idle
                        showEditPopup = false
                    }
                )
            }
    ) {
        // Use the existing GoalCell component
        GoalCell(
            goal = goal,
            isDragged = isDraggedAway,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Touch states for Samsung-style interaction
 */
sealed class TouchState {
    data object Idle : TouchState()
    data class PressDown(val startPosition: Offset, val startTime: Long) : TouchState()
    data class LongPressDetected(val position: Offset) : TouchState()
    data class Dragging(val currentPosition: Offset) : TouchState()
}

/**
 * Floating dragged item that follows finger
 */
@Composable
fun FloatingDraggedItem(
    goal: Goal,
    position: Offset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(
                x = with(LocalDensity.current) { (position.x - 40.dp.toPx()).toDp() },
                y = with(LocalDensity.current) { (position.y - 40.dp.toPx()).toDp() }
            )
            .size(80.dp)
            .graphicsLayer {
                scaleX = 1.2f
                scaleY = 1.2f
                shadowElevation = 16.dp.toPx()
                alpha = 0.9f
            }
    ) {
        GoalCell(
            goal = goal,
            isDragged = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Empty grid position component for Samsung style
 */
@Composable
fun SamsungEmptyGridPosition(
    position: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isHighlighted) 1.05f else 1f,
        animationSpec = animationSpec,
        label = "empty_scale_animation"
    )

    Card(
        modifier = modifier
            .scale(scaleAnimation)
            .clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = if (isHighlighted) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add goal",
                    tint = if (isHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${position + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Calculate distance between two points
 */
private fun calculateDistance(start: Offset, end: Offset): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/**
 * Grid position calculator
 */
@Composable
fun rememberSamsungGridPositionCalculator(
    gridBounds: Rect = Rect.Zero,
    columns: Int = 3,
    rows: Int = 4
): (Offset) -> Int {
    return remember(gridBounds, columns, rows) {
        { position ->
            if (gridBounds == Rect.Zero) {
                -1 // Invalid position
            } else {
                val cellWidth = gridBounds.width / columns
                val cellHeight = gridBounds.height / rows

                val column = ((position.x - gridBounds.left) / cellWidth).toInt().coerceIn(0, columns - 1)
                val row = ((position.y - gridBounds.top) / cellHeight).toInt().coerceIn(0, rows - 1)

                (row * columns + column).coerceIn(0, 11)
            }
        }
    }
}
