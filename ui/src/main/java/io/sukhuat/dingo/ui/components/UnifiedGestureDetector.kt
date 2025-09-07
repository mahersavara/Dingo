package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.math.sqrt

/**
 * Enhanced unified gesture detection system for goal items with smooth animations
 * Fixes the dual pointerInput conflict by combining all gestures into a single detector
 * Includes smooth drag start animations and haptic feedback
 */
@Composable
fun Modifier.unifiedGoalGestures(
    isDragModeActive: Boolean,
    onTap: () -> Unit = {},
    onLongPress: (globalPosition: Offset) -> Unit = {},
    onDragStart: (globalPosition: Offset) -> Unit = {},
    onDragUpdate: (globalPosition: Offset) -> Unit = {},
    onDragEnd: (globalPosition: Offset) -> Unit = {},
    onDragStartAnimation: () -> Unit = {} // New callback for drag start animation
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    // Track component's global bounds for coordinate transformation
    var globalBounds by remember { mutableStateOf(Rect.Zero) }
    
    // Configuration constants
    val longPressThreshold = 500L // 500ms for long press
    val dragThreshold = with(density) { 10.dp.toPx() } // 10dp movement to start drag
    
    this
        .onGloballyPositioned { coordinates ->
            globalBounds = coordinates.boundsInWindow()
        }
        .pointerInput(isDragModeActive) {
            awaitEachGesture {
                // Wait for initial touch
                val down = awaitFirstDown(requireUnconsumed = false)
                val downPosition = down.position
                
                var isDragging = false
                var hasTriggeredLongPress = false
                
                // Start monitoring the gesture
                var currentPosition = downPosition
                
                try {
                    // Wait for either long press timeout or significant movement
                    val gestureResult = withTimeout(longPressThreshold) {
                        // Monitor for movement while waiting for long press
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull { it.id == down.id }
                            
                            if (pointer == null || !pointer.pressed) {
                                // Pointer released - this is a tap
                                return@withTimeout GestureType.TAP
                            }
                            
                            currentPosition = pointer.position
                            val distance = sqrt(
                                (currentPosition.x - downPosition.x) * (currentPosition.x - downPosition.x) +
                                (currentPosition.y - downPosition.y) * (currentPosition.y - downPosition.y)
                            )
                            
                            if (distance > dragThreshold) {
                                // Movement detected - check if we should start dragging
                                return@withTimeout if (isDragModeActive || hasTriggeredLongPress) {
                                    GestureType.DRAG_START
                                } else {
                                    GestureType.TAP // Movement without long press in non-drag mode = cancel
                                }
                            }
                        }
                    }
                    
                    // Handle gesture based on result
                    when (gestureResult) {
                        GestureType.TAP -> {
                            onTap()
                        }
                        GestureType.DRAG_START -> {
                            // Start drag immediately
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val dragGlobalPosition = globalBounds.topLeft + currentPosition
                            onDragStart(dragGlobalPosition)
                            isDragging = true
                            
                            // Continue with drag loop
                            while (true) {
                                val dragEvent = awaitPointerEvent()
                                val dragPointer = dragEvent.changes.firstOrNull { it.id == down.id }
                                
                                if (dragPointer == null || !dragPointer.pressed) {
                                    // Drag ended
                                    val finalPos = globalBounds.topLeft + (dragPointer?.position ?: currentPosition)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDragEnd(finalPos)
                                    break
                                } else {
                                    // Continue dragging
                                    val dragPos = globalBounds.topLeft + dragPointer.position
                                    onDragUpdate(dragPos)
                                    currentPosition = dragPointer.position
                                    dragPointer.consume()
                                }
                            }
                        }
                    }
                    
                } catch (timeout: TimeoutCancellationException) {
                    // Long press timeout reached with enhanced feedback
                    hasTriggeredLongPress = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    
                    val longPressGlobalPosition = globalBounds.topLeft + currentPosition
                    
                    // Trigger drag start animation if in drag mode
                    if (isDragModeActive) {
                        onDragStartAnimation()
                    }
                    
                    onLongPress(longPressGlobalPosition)
                    
                    // After long press, continue monitoring for drag
                    if (isDragModeActive) {
                        // In drag mode, long press enables dragging
                        try {
                            // Continue monitoring for drag after long press
                            while (true) {
                                val event = awaitPointerEvent()
                                val pointer = event.changes.firstOrNull { it.id == down.id }
                                
                                if (pointer == null || !pointer.pressed) {
                                    break
                                }
                                
                                currentPosition = pointer.position
                                val distance = sqrt(
                                    (currentPosition.x - downPosition.x) * (currentPosition.x - downPosition.x) +
                                    (currentPosition.y - downPosition.y) * (currentPosition.y - downPosition.y)
                                )
                                
                                if (distance > dragThreshold && !isDragging) {
                                    // Start dragging after long press
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val dragStartPos = globalBounds.topLeft + currentPosition
                                    onDragStart(dragStartPos)
                                    isDragging = true
                                    
                                    // Continue with drag loop
                                    while (true) {
                                        val dragEvent = awaitPointerEvent()
                                        val dragPointer = dragEvent.changes.firstOrNull { it.id == down.id }
                                        
                                        if (dragPointer == null || !dragPointer.pressed) {
                                            // Drag ended
                                            val finalPos = globalBounds.topLeft + (dragPointer?.position ?: currentPosition)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDragEnd(finalPos)
                                            break
                                        } else {
                                            // Continue dragging
                                            val dragPos = globalBounds.topLeft + dragPointer.position
                                            onDragUpdate(dragPos)
                                            currentPosition = dragPointer.position
                                            dragPointer.consume()
                                        }
                                    }
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            // Handle any errors during post-long-press monitoring
                            GestureDebugLogger.logGestureEnd("LONG_PRESS_ERROR", globalBounds.topLeft + currentPosition)
                        }
                    }
                }
            }
        }
}

/**
 * Types of gestures that can be detected
 */
private enum class GestureType {
    TAP,
    DRAG_START
}

/**
 * Debug logging for gesture events
 */
object GestureDebugLogger {
    fun logGestureStart(type: String, position: Offset, isDragMode: Boolean) {
        println("🎯 GESTURE_DEBUG: $type started at $position, dragMode=$isDragMode")
    }
    
    fun logGestureEnd(type: String, position: Offset) {
        println("🎯 GESTURE_DEBUG: $type ended at $position")
    }
}