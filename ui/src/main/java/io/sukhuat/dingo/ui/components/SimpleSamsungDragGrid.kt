package io.sukhuat.dingo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.Goal

/**
 * Simplified Samsung/One UI launcher-style drag and drop grid
 * Enhanced with drag mode toggle support
 */
@Composable
fun SimpleSamsungDragGrid(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onGoalReorder: (Goal, Int) -> Unit,
    onEmptyPositionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDragModeActive: Boolean = false // New parameter for drag mode
) {
    val gridState = rememberLazyGridState()

    // Basic drag state
    var draggedGoal by remember { mutableStateOf<Goal?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }

    // Reset drag state when exiting drag mode
    LaunchedEffect(isDragModeActive) {
        if (!isDragModeActive && draggedGoal != null) {
            // Clear drag state when exiting drag mode
            draggedGoal = null
            dragPosition = Offset.Zero
        }
    }

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
                        modifier = Modifier.aspectRatio(1f),
                        isDragModeActive = isDragModeActive // Pass drag mode state
                    )
                } else {
                    SimpleEmptyGridPosition(
                        position = index,
                        onTap = { onEmptyPositionClick(index) },
                        modifier = Modifier.aspectRatio(1f),
                        isHighlighted = draggedGoal != null && calculateDropPosition(dragPosition) == index,
                        isDragModeActive = isDragModeActive // Pass drag mode state
                    )
                }
            }
        }

        // Enhanced floating dragged item overlay
        draggedGoal?.let { goal ->
            val targetPosition = if (dragPosition != Offset.Zero) calculateDropPosition(dragPosition) else -1
            val isValidTarget = targetPosition in 0..11

            EnhancedFloatingDragItem(
                goal = goal,
                globalPosition = dragPosition,
                targetGridPosition = targetPosition,
                isValidDropTarget = isValidTarget,
                modifier = Modifier.zIndex(10f)
            )
        }
    }
}

/**
 * Samsung-style draggable goal item with drag mode support
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
    modifier: Modifier = Modifier,
    isDragModeActive: Boolean = false // New parameter
) {
    var touchState by remember { mutableStateOf<SimpleTouchState>(SimpleTouchState.Idle) }
    var showEditPopup by remember { mutableStateOf(false) }
    var itemGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var showDragStartAnimation by remember { mutableStateOf(false) }

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
            touchState is SimpleTouchState.Dragging -> 0.95f
            isDragModeActive -> 1.02f // Subtle scale in drag mode
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
            .unifiedGoalGestures(
                isDragModeActive = isDragModeActive,
                onTap = {
                    // Only allow goal completion if NOT in drag mode
                    if (!isDragModeActive) {
                        onGoalClick(goal)
                    }
                },
                onLongPress = { globalPosition ->
                    // DEBUG: Log drag mode state and actions
                    println("🎯 UNIFIED_DEBUG: Long press triggered")
                    println("🎯 UNIFIED_DEBUG: isDragModeActive = $isDragModeActive")
                    println("🎯 UNIFIED_DEBUG: Goal = ${goal.text}")
                    println("🎯 UNIFIED_DEBUG: globalPosition = $globalPosition")

                    if (isDragModeActive) {
                        // In drag mode: prepare for potential dragging
                        touchState = SimpleTouchState.Dragging(globalPosition)
                        println("🎯 UNIFIED_DEBUG: Set touchState to Dragging for drag mode")
                    } else {
                        // Not in drag mode: show edit popup
                        showEditPopup = true
                        println("🎯 UNIFIED_DEBUG: Setting showEditPopup = true")
                        onGoalLongPress(goal, Pair(globalPosition.x, globalPosition.y))
                    }
                },
                onDragStart = { globalPosition ->
                    println("🎯 UNIFIED_DEBUG: Drag started at $globalPosition")
                    showEditPopup = false
                    touchState = SimpleTouchState.Dragging(globalPosition)
                    onDragStart(globalPosition)
                },
                onDragUpdate = { globalPosition ->
                    if (touchState is SimpleTouchState.Dragging) {
                        touchState = SimpleTouchState.Dragging(globalPosition)
                        onDragUpdate(globalPosition)
                    }
                },
                onDragEnd = { globalPosition ->
                    println("🎯 UNIFIED_DEBUG: Drag ended at $globalPosition")
                    if (touchState is SimpleTouchState.Dragging) {
                        onDragEnd(globalPosition)
                    }
                    // Reset state
                    touchState = SimpleTouchState.Idle
                    showEditPopup = false
                    showDragStartAnimation = false
                },
                onDragStartAnimation = {
                    showDragStartAnimation = true
                }
            )
    ) {
        // Use the existing GoalCell component
        GoalCell(
            goal = goal,
            isDragged = isDraggedAway,
            modifier = Modifier.fillMaxSize()
        )

        // Enhanced drag mode visual indicators
        if (isDragModeActive && !isDraggedAway) {
            EnhancedDragModeIndicator(
                isDragModeActive = true,
                modifier = Modifier.fillMaxSize()
            )

            // Drag handle icon with enhanced positioning
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = RusticGold,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(16.dp)
                        .graphicsLayer {
                            shadowElevation = 4.dp.toPx()
                        }
                )
            }
        }

        // Show edit popup overlay if needed (only when NOT in drag mode)
        // DEBUG: Log popup conditions
        val shouldShowPopup = showEditPopup && touchState is SimpleTouchState.Idle && !isDragModeActive
        if (shouldShowPopup) {
            println("🎯 UNIFIED_DEBUG: 🚨 POPUP SHOWN!")
            println("🎯 UNIFIED_DEBUG: showEditPopup = $showEditPopup")
            println("🎯 UNIFIED_DEBUG: touchState = $touchState")
            println("🎯 UNIFIED_DEBUG: isDragModeActive = $isDragModeActive")
        }
        if (shouldShowPopup) {
            // Add a simple visual indicator for edit mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
            )
        }

        // Drag start animation effect
        if (showDragStartAnimation) {
            DragStartEffect(
                isTriggered = showDragStartAnimation,
                onAnimationEnd = { showDragStartAnimation = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Simplified empty grid position with drag mode support
 */
@Composable
fun SimpleEmptyGridPosition(
    position: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    isDragModeActive: Boolean = false // New parameter
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = when {
            isHighlighted -> 1.05f
            isDragModeActive -> 1.02f
            else -> 1f
        },
        animationSpec = animationSpec,
        label = "empty_scale_animation"
    )

    Card(
        modifier = modifier
            .scale(scaleAnimation)
            .clickable { // Only allow adding goals if NOT in drag mode
                if (!isDragModeActive) {
                    onTap()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isDragModeActive -> RusticGold.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        border = when {
            isHighlighted -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isDragModeActive -> BorderStroke(2.dp, RusticGold.copy(alpha = 0.5f))
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                    contentDescription = if (isDragModeActive) "Drop target" else "Add goal",
                    tint = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        isDragModeActive -> RusticGold.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${position + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isHighlighted -> MaterialTheme.colorScheme.primary
                        isDragModeActive -> RusticGold.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            }
        }

        // Enhanced drop zone highlighting overlay
        if (isHighlighted) {
            EnhancedDropZoneHighlight(
                isHighlighted = true,
                isValidTarget = true,
                modifier = Modifier.fillMaxSize()
            )
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
 * Simplified touch states for Samsung-style interaction
 * Reduced from 3 states to 2 states for better reliability
 */
sealed class SimpleTouchState {
    data object Idle : SimpleTouchState()
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
