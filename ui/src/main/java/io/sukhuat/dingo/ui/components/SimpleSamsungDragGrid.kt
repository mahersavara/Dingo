package io.sukhuat.dingo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.sukhuat.dingo.domain.model.Goal

/**
 * Simplified Samsung/One UI launcher-style drag and drop grid
 * For now, provides basic drag functionality while maintaining visual consistency
 */
@Composable
fun SimpleSamsungDragGrid(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onGoalReorder: (Goal, Int) -> Unit,
    onEmptyPositionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Basic drag state
    var draggedGoal by remember { mutableStateOf<Goal?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }
    var previewGridPositions by remember { mutableStateOf<Array<Goal?>>(arrayOfNulls(12)) }

    // Grid position calculator
    val calculateDropPosition = rememberSimpleGridPositionCalculator(
        gridBounds = gridBounds,
        columns = 3,
        rows = 4
    )

    // Create smart grid with reflow preview
    val displayGridPositions = remember(goals, draggedGoal, dragPosition) {
        val baseGrid = Array<Goal?>(12) { null }

        // Populate base grid based on position field
        goals.forEach { goal ->
            val position = goal.position.coerceIn(0, 11)
            if (baseGrid[position] == null) {
                baseGrid[position] = goal
            } else {
                // Handle position conflict - find next available slot
                val nextAvailable = baseGrid.indexOfFirst { it == null }
                if (nextAvailable != -1) {
                    baseGrid[nextAvailable] = goal
                }
            }
        }

        // If dragging, create preview with smart reflow
        if (draggedGoal != null && dragPosition != Offset.Zero) {
            val previewGrid = Array<Goal?>(12) { null }
            val targetPosition = calculateDropPosition(dragPosition)

            // Place non-dragged items with smart shifting
            val otherGoals = goals.filter { it.id != draggedGoal!!.id }

            otherGoals.forEach { goal ->
                val originalPos = goal.position.coerceIn(0, 11)

                // If target position conflicts, shift items intelligently
                val finalPos = if (targetPosition in 0..11) {
                    when {
                        originalPos < targetPosition -> originalPos // Items before target stay in place
                        originalPos >= targetPosition -> {
                            // Shift items at or after target position to the right
                            val shiftedPos = originalPos + 1
                            if (shiftedPos <= 11) shiftedPos else originalPos
                        }
                        else -> originalPos
                    }
                } else {
                    originalPos
                }

                if (finalPos in 0..11 && previewGrid[finalPos] == null) {
                    previewGrid[finalPos] = goal
                }
            }

            previewGrid
        } else {
            baseGrid
        }
    }

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
            itemsIndexed(displayGridPositions.toList()) { index, goal ->
                if (goal != null) {
                    SimpleDraggableGoalItem(
                        goal = goal,
                        isDraggedAway = draggedGoal?.id == goal.id,
                        isDropTarget = draggedGoal != null && calculateDropPosition(dragPosition) == index,
                        onGoalClick = onGoalClick,
                        onGoalLongPress = onGoalLongPress,
                        onDragStart = {
                            draggedGoal = goal
                            dragPosition = it
                        },
                        onDragUpdate = {
                            dragPosition = it
                        },
                        onDragEnd = { finalPos ->
                            draggedGoal?.let { draggedGoalRef ->
                                val dropPosition = calculateDropPosition(finalPos)
                                if (dropPosition != draggedGoalRef.position && dropPosition in 0..11) {
                                    // Perform smart reordering
                                    onGoalReorder(draggedGoalRef, dropPosition)
                                }
                            }
                            draggedGoal = null
                            dragPosition = Offset.Zero
                        },
                        modifier = Modifier.aspectRatio(1f)
                    )
                } else {
                    SimpleEmptyGridPosition(
                        position = index,
                        onTap = { onEmptyPositionClick(index) },
                        modifier = Modifier.aspectRatio(1f),
                        isHighlighted = draggedGoal != null && calculateDropPosition(dragPosition) == index
                    )
                }
            }
        }

        // Floating dragged item overlay
        draggedGoal?.let { goal ->
            SimpleFloatingDraggedItem(
                goal = goal,
                position = dragPosition,
                modifier = Modifier.zIndex(10f)
            )
        }
    }
}

/**
 * Samsung-style draggable goal item with proper touch detection
 */
@Composable
fun SimpleDraggableGoalItem(
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
    var touchState by remember { mutableStateOf<SimpleTouchState>(SimpleTouchState.Idle) }
    var showEditPopup by remember { mutableStateOf(false) }
    var itemGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    val hapticFeedback = LocalHapticFeedback.current

    // Touch state is now handled directly by gesture detectors

    // Smooth animations
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
            touchState is SimpleTouchState.LongPressDetected -> 0.95f
            else -> 1f
        },
        animationSpec = animationSpec,
        label = "scale_animation"
    )

    Box(
        modifier = modifier
            .alpha(alphaAnimation)
            .scale(scaleAnimation)
            .onGloballyPositioned { coordinates ->
                itemGlobalPosition = coordinates.positionInWindow()
            }
            .pointerInput(goal.id) {
                // Handle taps and long press
                detectTapGestures(
                    onTap = {
                        // Simple tap - always works
                        onGoalClick(goal)
                    },
                    onLongPress = { offset ->
                        // Long press - show popup with absolute screen coordinates
                        touchState = SimpleTouchState.LongPressDetected(offset)
                        showEditPopup = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Convert to absolute screen coordinates
                        val absoluteX = itemGlobalPosition.x + offset.x
                        val absoluteY = itemGlobalPosition.y + offset.y
                        onGoalLongPress(goal, Pair(absoluteX, absoluteY))
                    }
                )
            }
            .pointerInput(goal.id) {
                // Handle drag operations (separate pointer input for drag)
                detectDragGestures(
                    onDragStart = { startOffset ->
                        // Only start drag if we're in long press mode
                        if (touchState is SimpleTouchState.LongPressDetected) {
                            showEditPopup = false
                            touchState = SimpleTouchState.Dragging(startOffset)
                            onDragStart(startOffset)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDrag = { change, _ ->
                        if (touchState is SimpleTouchState.Dragging) {
                            touchState = SimpleTouchState.Dragging(change.position)
                            onDragUpdate(change.position)
                        }
                    },
                    onDragEnd = {
                        if (touchState is SimpleTouchState.Dragging) {
                            val dragState = touchState as SimpleTouchState.Dragging
                            onDragEnd(dragState.currentPosition)
                        }
                        // Reset state
                        touchState = SimpleTouchState.Idle
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

        // Show edit popup overlay if needed
        if (showEditPopup && touchState is SimpleTouchState.LongPressDetected) {
            // ?Add a simple visual indicator for edit mode =))) khong co cai nao xoa ah
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
//                        RoundedCornerShape(16.dp)
//                    )
//            )
        }
    }
}

/**
 * Simplified empty grid position
 */
@Composable
fun SimpleEmptyGridPosition(
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
 * Simplified floating dragged item
 */
@Composable
fun SimpleFloatingDraggedItem(
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
 * Touch states for Samsung-style interaction
 */
sealed class SimpleTouchState {
    data object Idle : SimpleTouchState()
    data class LongPressDetected(val position: Offset) : SimpleTouchState()
    data class Dragging(val currentPosition: Offset) : SimpleTouchState()
}

/**
 * Simplified grid position calculator
 */
@Composable
fun rememberSimpleGridPositionCalculator(
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
