package io.sukhuat.dingo.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/**
 * A visual indicator that follows the cursor during drag operations
 */
@Composable
fun DragIndicator(
    isDragging: Boolean,
    dragPosition: Offset,
    imageResId: Int? = null,
    customImage: String? = null,
    tint: Color = Color.Unspecified
) {
    if (!isDragging) return
    
    val density = LocalDensity.current
    val size = 48.dp
    val halfSize = with(density) { (size / 2).toPx() }
    
    // Animation for scale effect
    val scale = remember { Animatable(0.8f) }
    
    // Animate scale when dragging starts
    LaunchedEffect(isDragging) {
        if (isDragging) {
            launch {
                scale.animateTo(
                    targetValue = 1.2f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                scale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
    
    // Position the indicator at the drag position
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (dragPosition.x - halfSize).toInt(),
                    y = (dragPosition.y - halfSize).toInt()
                )
            }
            .size(size)
            .shadow(8.dp, CircleShape)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = 0.9f
            }
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            customImage != null -> {
                // Show custom image
                AsyncImage(
                    model = customImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            imageResId != null -> {
                // Show icon
                Icon(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = tint
                )
            }
        }
    }
} 