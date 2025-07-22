package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.domain.model.Goal

/**
 * States for goal interaction
 */
sealed class GoalInteractionState {
    data object Idle : GoalInteractionState()
    data class LongPressDetected(
        val goal: Goal,
        val startTime: Long,
        val startPosition: Offset
    ) : GoalInteractionState()
    data class EditPopupShown(val goal: Goal) : GoalInteractionState()
    data class DragModeActive(
        val goal: Goal,
        val currentPosition: Offset,
        val startGridPosition: Int
    ) : GoalInteractionState()
}

/**
 * Composable that handles complex touch interactions for goal items
 * Implements the corrected logic: Long press → popup, drag → cancel popup and start drag mode
 */
@Composable
fun TouchInteractionManager(
    goal: Goal,
    onEdit: (Goal) -> Unit,
    onReorder: (Goal, Int) -> Unit,
    onPopupDismiss: () -> Unit,
    isPopupShown: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (
        interactionState: GoalInteractionState,
        showPopup: Boolean,
        onPopupShow: () -> Unit,
        onPopupHide: () -> Unit
    ) -> Unit
) {
    var interactionState by remember { mutableStateOf<GoalInteractionState>(GoalInteractionState.Idle) }
    var showEditPopup by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Configuration
    val dragThreshold = with(density) { 10.dp.toPx() }
    val longPressTimeout = 500L

    LaunchedEffect(isPopupShown) {
        showEditPopup = isPopupShown
    }

    content(
        interactionState = interactionState,
        showPopup = showEditPopup,
        onPopupShow = { showEditPopup = true },
        onPopupHide = {
            showEditPopup = false
            onPopupDismiss()
        }
    )
}

/**
 * Modifier that adds drag and long press detection for goal reordering
 */
@Composable
fun Modifier.goalDragAndDrop(
    goal: Goal,
    onEdit: (Goal) -> Unit,
    onReorder: (Goal, Int) -> Unit,
    onPopupShow: () -> Unit,
    onPopupHide: () -> Unit,
    calculateDropPosition: (Offset) -> Int
): Modifier {
    var interactionState by remember { mutableStateOf<GoalInteractionState>(GoalInteractionState.Idle) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val dragThreshold = with(density) { 10.dp.toPx() }

    return this
        .pointerInput(goal.id) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    // Long press detected - show edit popup immediately
                    interactionState = GoalInteractionState.LongPressDetected(
                        goal,
                        System.currentTimeMillis(),
                        startOffset
                    )
                    onPopupShow()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { change, _ ->
                    when (val state = interactionState) {
                        is GoalInteractionState.LongPressDetected -> {
                            val distance = calculateDistance(state.startPosition, change.position)
                            if (distance > dragThreshold) {
                                // User moved finger - switch to drag mode
                                onPopupHide()
                                interactionState = GoalInteractionState.DragModeActive(
                                    goal,
                                    change.position,
                                    goal.position
                                )
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        is GoalInteractionState.EditPopupShown -> {
                            val startPos = Offset.Zero // Fallback for EditPopupShown
                            val distance = calculateDistance(startPos, change.position)
                            if (distance > dragThreshold) {
                                // Switch to drag mode
                                onPopupHide()
                                interactionState = GoalInteractionState.DragModeActive(
                                    goal,
                                    change.position,
                                    goal.position
                                )
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        is GoalInteractionState.DragModeActive -> {
                            // Continue dragging
                            interactionState = state.copy(currentPosition = change.position)
                        }
                        else -> { /* Idle - no action */ }
                    }
                },
                onDragEnd = {
                    when (val state = interactionState) {
                        is GoalInteractionState.LongPressDetected -> {
                            // Long press without drag - switch to edit popup mode
                            interactionState = GoalInteractionState.EditPopupShown(goal)
                            // Popup stays visible for editing
                        }
                        is GoalInteractionState.DragModeActive -> {
                            // Complete drag operation
                            val dropPosition = calculateDropPosition(state.currentPosition)
                            if (dropPosition != state.startGridPosition && dropPosition in 0..11) {
                                onReorder(goal, dropPosition)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            interactionState = GoalInteractionState.Idle
                        }
                        else -> {
                            interactionState = GoalInteractionState.Idle
                        }
                    }
                }
            )
        }
        .then(
            // Add visual feedback for drag state
            when (val dragState = interactionState) {
                is GoalInteractionState.DragModeActive -> {
                    Modifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                        alpha = 0.9f
                        shadowElevation = with(density) { 8.dp.toPx() }
                    }
                }
                is GoalInteractionState.EditPopupShown -> {
                    Modifier.graphicsLayer {
                        // Subtle highlight for edit mode
                        shadowElevation = with(density) { 2.dp.toPx() }
                    }
                }
                else -> Modifier
            }
        )
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
 * Converts screen coordinates to grid position (0-11)
 */
@Composable
fun rememberGridPositionCalculator(
    gridBounds: androidx.compose.ui.geometry.Rect = androidx.compose.ui.geometry.Rect.Zero,
    columns: Int = 3,
    rows: Int = 4
): (Offset) -> Int {
    return remember(gridBounds, columns, rows) {
        { position ->
            if (gridBounds == androidx.compose.ui.geometry.Rect.Zero) {
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
