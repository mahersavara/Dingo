package io.sukhuat.dingo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.Goal

/**
 * Enhanced floating drag item with smooth animations and visual feedback
 */
@Composable
fun EnhancedFloatingDragItem(
    goal: Goal,
    globalPosition: Offset,
    targetGridPosition: Int,
    isValidDropTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    // Trigger visibility animation
    LaunchedEffect(globalPosition) {
        if (globalPosition != Offset.Zero) {
            isVisible = true
        }
    }

    // Smooth animations for scale and alpha
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isVisible) {
            if (isValidDropTarget) 1.15f else 1.1f
        } else {
            1f
        },
        animationSpec = animationSpec,
        label = "floating_scale"
    )

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisible) 0.95f else 0f,
        animationSpec = animationSpec,
        label = "floating_alpha"
    )

    val shadowAnimation by animateFloatAsState(
        targetValue = if (isVisible) {
            if (isValidDropTarget) 20f else 16f
        } else {
            8f
        },
        animationSpec = animationSpec,
        label = "floating_shadow"
    )

    // Pulsing animation for valid drop zones
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isValidDropTarget) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )

    Box(
        modifier = modifier
            .offset(
                x = with(density) { (globalPosition.x - 50.dp.toPx()).toDp() },
                y = with(density) { (globalPosition.y - 50.dp.toPx()).toDp() }
            )
            .size(100.dp)
            .graphicsLayer {
                scaleX = scaleAnimation * pulseAnimation
                scaleY = scaleAnimation * pulseAnimation
                shadowElevation = shadowAnimation
                alpha = alphaAnimation
                rotationZ = if (isValidDropTarget) 2f else 0f // Slight tilt for valid drops
            }
    ) {
        // Enhanced goal cell with visual feedback
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = with(density) { shadowAnimation.toDp() }
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isValidDropTarget -> RusticGold.copy(alpha = 0.9f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                }
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = if (isValidDropTarget) 3.dp else 2.dp,
                        color = if (isValidDropTarget) Color.Green else RusticGold.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                GoalCell(
                    goal = goal,
                    isDragged = true,
                    modifier = Modifier.fillMaxSize()
                )

                // Valid drop indicator
                if (isValidDropTarget) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(12.dp)
                            .background(
                                Color.Green,
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Green dot indicator for valid drop
                    }
                }
            }
        }
    }
}

/**
 * Enhanced drop zone highlight with smooth animations
 */
@Composable
fun EnhancedDropZoneHighlight(
    isHighlighted: Boolean,
    isValidTarget: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isHighlighted) 1.08f else 1f,
        animationSpec = animationSpec,
        label = "dropzone_scale"
    )

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isHighlighted) 0.8f else 0f,
        animationSpec = animationSpec,
        label = "dropzone_alpha"
    )

    val borderWidthAnimation by animateFloatAsState(
        targetValue = if (isHighlighted) 4f else 0f,
        animationSpec = animationSpec,
        label = "dropzone_border"
    )

    // Pulsing effect for highlighted zones
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isHighlighted) 1.02f else 1f,
        animationSpec = if (isHighlighted) {
            infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        },
        label = "dropzone_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scaleAnimation * pulseAnimation)
            .alpha(alphaAnimation)
            .background(
                color = if (isValidTarget) {
                    Color.Green.copy(alpha = 0.2f)
                } else {
                    Color.Red.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = with(LocalDensity.current) { borderWidthAnimation.toDp() },
                color = if (isValidTarget) {
                    Color.Green.copy(alpha = 0.8f)
                } else {
                    Color.Red.copy(alpha = 0.8f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Ripple effect for drop zones
        if (isHighlighted) {
            RippleEffect(
                isValidTarget = isValidTarget,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Ripple effect for drop zones
 */
@Composable
private fun RippleEffect(
    isValidTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )

    Box(
        modifier = modifier
            .scale(rippleScale)
            .alpha(rippleAlpha)
            .background(
                color = if (isValidTarget) {
                    Color.Green.copy(alpha = 0.3f)
                } else {
                    Color.Red.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    )
}

/**
 * Enhanced drag mode visual indicator
 */
@Composable
fun EnhancedDragModeIndicator(
    isDragModeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isDragModeActive) 1.03f else 1f,
        animationSpec = animationSpec,
        label = "drag_mode_scale"
    )

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isDragModeActive) 1f else 0f,
        animationSpec = animationSpec,
        label = "drag_mode_alpha"
    )

    // Subtle glow effect for drag mode
    val glowAnimation by animateFloatAsState(
        targetValue = if (isDragModeActive) 8f else 0f,
        animationSpec = animationSpec,
        label = "drag_mode_glow"
    )

    Box(
        modifier = modifier
            .scale(scaleAnimation)
            .alpha(alphaAnimation)
            .shadow(
                elevation = with(LocalDensity.current) { glowAnimation.toDp() },
                shape = RoundedCornerShape(16.dp),
                ambientColor = RusticGold,
                spotColor = RusticGold
            )
            .background(
                color = RusticGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = RusticGold.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
    )
}

/**
 * Drag start animation effect
 */
@Composable
fun DragStartEffect(
    isTriggered: Boolean,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            isVisible = true
        }
    }

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isVisible) 1.5f else 1f,
        animationSpec = tween(300),
        finishedListener = { onAnimationEnd() },
        label = "drag_start_scale"
    )

    val alphaAnimation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 0.8f,
        animationSpec = tween(300),
        label = "drag_start_alpha"
    )

    if (isVisible) {
        Box(
            modifier = modifier
                .scale(scaleAnimation)
                .alpha(alphaAnimation)
                .background(
                    color = RusticGold.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}
