package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import io.sukhuat.dingo.domain.model.Goal

/**
 * Enhanced goals grid with drag-and-drop support and fixed position control
 */
@Composable
fun DragDropGoalsGrid(
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

    // State for drag and drop
    var dragState by remember { mutableStateOf<DragState>(DragState.Idle) }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }

    // Grid position calculator
    val calculateDropPosition = rememberGridPositionCalculator(
        gridBounds = gridBounds,
        columns = 3,
        rows = 4
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                gridBounds = Rect(
                    offset = coordinates.positionInParent(),
                    size = coordinates.size.let { androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
                )
            }
    ) {
        itemsIndexed(gridPositions.toList()) { index, goal ->
            if (goal != null) {
                DraggableGoalItem(
                    goal = goal,
                    isDragging = (dragState as? DragState.Dragging)?.goal?.id == goal.id,
                    onGoalClick = onGoalClick,
                    onGoalLongPress = onGoalLongPress,
                    onReorder = onGoalReorder,
                    calculateDropPosition = calculateDropPosition,
                    onDragStateChange = { newState -> dragState = newState },
                    modifier = Modifier.aspectRatio(1f)
                )
            } else {
                EmptyGridPosition(
                    position = index,
                    onTap = { onEmptyPositionClick(index) },
                    modifier = Modifier.aspectRatio(1f),
                    isHighlighted = (dragState as? DragState.Dragging)?.let { calculateDropPosition(it.currentPosition) == index } ?: false
                )
            }
        }
    }
}

/**
 * Draggable goal item with touch interaction management
 */
@Composable
fun DraggableGoalItem(
    goal: Goal,
    isDragging: Boolean,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onReorder: (Goal, Int) -> Unit,
    calculateDropPosition: (Offset) -> Int,
    onDragStateChange: (DragState) -> Unit,
    modifier: Modifier = Modifier
) {
    var interactionState by remember { mutableStateOf<GoalInteractionState>(GoalInteractionState.Idle) }
    var showEditPopup by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val dragThreshold = with(density) { 10.dp.toPx() }

    Box(
        modifier = modifier
            .pointerInput(goal.id) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        // Long press detected - show edit popup immediately
                        interactionState = GoalInteractionState.LongPressDetected(
                            goal,
                            System.currentTimeMillis(),
                            startOffset
                        )
                        showEditPopup = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { _, dragAmount ->
                        when (val state = interactionState) {
                            is GoalInteractionState.LongPressDetected,
                            is GoalInteractionState.EditPopupShown -> {
                                val newPosition = state.startPosition + dragAmount
                                val distance = calculateDistance(
                                    state.startPosition,
                                    newPosition
                                )
                                if (distance > dragThreshold) {
                                    // Switch to drag mode
                                    showEditPopup = false
                                    interactionState = GoalInteractionState.DragModeActive(
                                        goal,
                                        newPosition,
                                        goal.position
                                    )
                                    onDragStateChange(DragState.Dragging(goal, newPosition))
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                            is GoalInteractionState.DragModeActive -> {
                                // Continue dragging
                                val newPosition = state.currentPosition + dragAmount
                                interactionState = state.copy(currentPosition = newPosition)
                                onDragStateChange(DragState.Dragging(goal, newPosition))
                            }
                            else -> { /* Idle - no action */ }
                        }
                    },
                    onDragEnd = {
                        when (val state = interactionState) {
                            is GoalInteractionState.LongPressDetected,
                            is GoalInteractionState.EditPopupShown -> {
                                // Long press without drag - show popup for editing
                                interactionState = GoalInteractionState.EditPopupShown(goal)
                                // Trigger long press callback
                                onGoalLongPress(goal, Pair(state.startPosition.x, state.startPosition.y))
                            }
                            is GoalInteractionState.DragModeActive -> {
                                // Complete drag operation
                                val dropPosition = calculateDropPosition(state.currentPosition)
                                if (dropPosition != state.startGridPosition && dropPosition in 0..11) {
                                    onReorder(goal, dropPosition)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                interactionState = GoalInteractionState.Idle
                                onDragStateChange(DragState.Idle)
                                showEditPopup = false
                            }
                            else -> {
                                interactionState = GoalInteractionState.Idle
                                onDragStateChange(DragState.Idle)
                            }
                        }
                    }
                )
            }
            .clickable(
                enabled = interactionState == GoalInteractionState.Idle
            ) {
                onGoalClick(goal)
            }
            .then(
                // Visual feedback during drag
                when (interactionState) {
                    is GoalInteractionState.DragModeActive -> {
                        Modifier
                            .scale(1.05f)
                            .alpha(0.9f)
                            .graphicsLayer {
                                shadowElevation = with(density) { 8.dp.toPx() }
                            }
                    }
                    is GoalInteractionState.EditPopupShown -> {
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(16.dp)
                        )
                    }
                    else -> Modifier
                }
            )
    ) {
        // Use the GoalCell component
        GoalCell(
            goal = goal,
            isDragged = isDragging || interactionState is GoalInteractionState.DragModeActive,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Empty grid position component
 */
@Composable
fun EmptyGridPosition(
    position: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier
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
 * Drag state for managing drag and drop operations
 */
sealed class DragState {
    data object Idle : DragState()
    data class Dragging(
        val goal: Goal,
        val currentPosition: Offset
    ) : DragState()
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
 * Get the start position from interaction state
 */
private val GoalInteractionState.startPosition: Offset
    get() = when (this) {
        is GoalInteractionState.LongPressDetected -> startPosition
        is GoalInteractionState.EditPopupShown -> Offset.Zero // Fallback
        is GoalInteractionState.DragModeActive -> Offset.Zero // Not used in drag mode
        else -> Offset.Zero
    }
