package io.sukhuat.dingo.common.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * Adds a bubble-like animation effect to items when scrolling in a LazyGrid
 */
fun Modifier.bubbleScrollAnimation(
    gridState: LazyGridState,
    itemInfo: LazyGridItemInfo
): Modifier = composed {
    val density = LocalDensity.current.density
    
    // Calculate the center position of the item
    val itemCenter = itemInfo.offset.y + itemInfo.size.height / 2
    
    // Calculate the center of the visible viewport
    val viewportHeight = gridState.layoutInfo.viewportSize.height
    val viewportCenter = viewportHeight / 2
    
    // Calculate how far the item is from the center of the screen
    val distanceFromCenter = (itemCenter - viewportCenter).absoluteValue.toFloat()
    
    // The maximum distance we want to consider for the effect
    val maxDistance = 300f * density
    
    // Calculate the scale and rotation factor based on distance from center
    val scrollFactor = (1f - (distanceFromCenter / maxDistance).coerceIn(0f, 1f))
    
    // Remember if this item was previously in view
    var wasInView by remember { mutableStateOf(false) }
    val isInView = distanceFromCenter < maxDistance
    
    // Animate entry when item comes into view
    if (!wasInView && isInView) {
        wasInView = true
    }
    
    // Calculate animation values
    val scale by animateFloatAsState(
        targetValue = 0.8f + (scrollFactor * 0.2f),
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )
    
    val rotationZ by animateFloatAsState(
        targetValue = if (itemCenter < viewportCenter) -2f * scrollFactor else 2f * scrollFactor,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 0.6f + (scrollFactor * 0.4f),
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    // Apply the transformations
    this
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.rotationZ = rotationZ
            this.alpha = alpha
            
            // Add a subtle 3D effect
            val cameraDistance = 8f
            this.cameraDistance = cameraDistance
            
            // Add a subtle translation based on position
            translationY = if (itemCenter < viewportCenter) -2f * scrollFactor else 2f * scrollFactor
        }
}

/**
 * Adds a popping animation effect when items first appear in a LazyGrid
 */
fun Modifier.popInAnimation(
    visible: Boolean,
    index: Int
): Modifier = composed {
    // Staggered delay based on item index
    val delayMillis = index * 50
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "pop_scale"
    )
    
    this.scale(scale)
} 