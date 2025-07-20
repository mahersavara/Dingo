package io.sukhuat.dingo.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.sukhuat.dingo.common.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A celebration animation that shows when a goal is completed
 */
@Composable
fun GoalCompletionCelebration(
    goalText: String,
    imageResId: Int? = null,
    customImage: String? = null,
    targetPosition: Pair<Float, Float> = Pair(0.5f, 0.3f),
    onAnimationEnd: () -> Unit = {}
) {
    var showOverlay by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(true) }

    // Scale animation for the celebration card
    val scale = remember { Animatable(0.6f) }

    // Rotation animation for the celebration card
    val rotation by animateFloatAsState(
        targetValue = if (showOverlay) 0f else -10f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    // Show the overlay after a short delay
    LaunchedEffect(Unit) {
        delay(500) // Let confetti start first
        showOverlay = true

        launch {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        // Hide the celebration after a delay
        delay(2500)
        showOverlay = false
        delay(300) // Wait for fade out
        showConfetti = false
        delay(200) // Wait for confetti to clean up
        onAnimationEnd()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Confetti animation
        if (showConfetti) {
            EnhancedConfettiAnimation(
                targetPosition = targetPosition,
                onAnimationEnd = {}
            )
        }

        // Celebration overlay
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(tween(300)) + scaleIn(tween(500)),
            exit = fadeOut(tween(300)) + scaleOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                modifier = Modifier
                    .padding(32.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        rotationZ = rotation
                    },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Trophy icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFFFC107).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trophy),
                            contentDescription = "Trophy",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Goal completed text
                    Text(
                        text = "Goal Completed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Goal text
                    Text(
                        text = goalText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Goal image if available
                    if (customImage != null) {
                        AsyncImage(
                            model = customImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageResId != null) {
                        Icon(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Congratulations text
                    Text(
                        text = "Congratulations!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFB300)
                    )
                }
            }
        }
    }
}
