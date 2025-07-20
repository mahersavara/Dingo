package io.sukhuat.dingo.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Data class representing a confetti piece
 */
data class ConfettiPiece(
    val color: Color,
    val size: Float,
    val shape: ConfettiShape,
    var x: Float,
    var y: Float,
    var rotation: Float,
    val xVelocity: Float,
    var yVelocity: Float,
    val rotationVelocity: Float
)

/**
 * Enum representing the shape of confetti
 */
enum class ConfettiShape {
    CIRCLE, SQUARE, TRIANGLE
}

/**
 * Enhanced confetti animation that simulates realistic physics
 * @param particleCount Number of confetti particles
 * @param colors List of colors for the confetti
 * @param targetPosition Target position for the confetti (normalized 0-1)
 * @param duration Duration of the animation in milliseconds
 * @param fadeOutDuration Duration of the fade out in milliseconds
 * @param onAnimationEnd Callback when the animation ends
 */
@Composable
fun EnhancedConfettiAnimation(
    particleCount: Int = 100,
    colors: List<Color> = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFF9C27B0) // Purple
    ),
    targetPosition: Pair<Float, Float> = Pair(0.5f, 0.5f),
    duration: Int = 3000,
    fadeOutDuration: Int = 1000,
    onAnimationEnd: () -> Unit = {}
) {
    // Animation state
    var isAnimating by remember { mutableStateOf(true) }
    val infiniteTransition = rememberInfiniteTransition(label = "confetti_transition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    // Generate confetti pieces
    val confettiPieces = remember {
        List(particleCount) {
            val shape = when (Random.nextInt(3)) {
                0 -> ConfettiShape.CIRCLE
                1 -> ConfettiShape.SQUARE
                else -> ConfettiShape.TRIANGLE
            }

            ConfettiPiece(
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 20f + 5f,
                shape = shape,
                x = targetPosition.first,
                y = targetPosition.second,
                rotation = Random.nextFloat() * 360f,
                xVelocity = (Random.nextFloat() - 0.5f) * 15f,
                yVelocity = Random.nextFloat() * -10f - 5f,
                rotationVelocity = (Random.nextFloat() - 0.5f) * 20f
            )
        }
    }

    // Handle animation lifecycle
    LaunchedEffect(Unit) {
        delay(duration.toLong())
        isAnimating = false
        delay(fadeOutDuration.toLong())
        onAnimationEnd()
    }

    // Draw confetti
    Box(modifier = Modifier.fillMaxSize()) {
        if (isAnimating) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gravity = 0.5f // Gravity acceleration

                confettiPieces.forEach { piece ->
                    // Apply physics
                    piece.y += piece.yVelocity
                    piece.x += piece.xVelocity
                    piece.yVelocity += gravity
                    piece.rotation += piece.rotationVelocity

                    // Draw the confetti piece
                    translate(piece.x * size.width, piece.y * size.height) {
                        rotate(piece.rotation) {
                            when (piece.shape) {
                                ConfettiShape.CIRCLE -> {
                                    drawCircle(
                                        color = piece.color,
                                        radius = piece.size
                                    )
                                }
                                ConfettiShape.SQUARE -> {
                                    drawRect(
                                        color = piece.color,
                                        topLeft = Offset(-piece.size / 2, -piece.size / 2),
                                        size = androidx.compose.ui.geometry.Size(piece.size, piece.size)
                                    )
                                }
                                ConfettiShape.TRIANGLE -> {
                                    val path = Path().apply {
                                        moveTo(0f, -piece.size / 2)
                                        lineTo(piece.size / 2, piece.size / 2)
                                        lineTo(-piece.size / 2, piece.size / 2)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = piece.color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
