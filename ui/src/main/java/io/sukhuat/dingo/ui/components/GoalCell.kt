package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.theme.*
import io.sukhuat.dingo.common.utils.getSafeImageUri
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * Reusable Goal Cell component that can be used in grids
 */
@Composable
fun GoalCell(
    goal: Goal,
    isDragged: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
        ,
        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isDragged) 12.dp else 6.dp
//        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f) // Consistent glass effect for all states
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Image at the top
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (goal.customImage != null) {
                        // Use our utility function to get a safe URI
                        val context = LocalContext.current
                        val customImage = goal.customImage // Store in local variable to avoid smart cast issue
                        val safeImageUri = if (customImage != null) getSafeImageUri(context, customImage) else null

                        if (safeImageUri != null) {
                            // Use cached image loading for better performance
                            CachedAsyncImage(
                                model = safeImageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize(0.8f)
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )

                            // Show cloud indicator for Firebase Storage URLs
                            if (customImage != null && customImage.startsWith("https://firebasestorage.googleapis.com")) {
//                                Box(
//                                    modifier = Modifier
//                                        .align(Alignment.TopEnd)
//                                        .padding(4.dp)
//                                        .size(16.dp)
//                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.ic_upload),
//                                        contentDescription = "Stored in Firebase",
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(12.dp)
//                                    )
//                                }
                            }
                        } else {
                            // Fallback to default icon if URI is invalid
                            Icon(
                                painter = painterResource(id = R.drawable.ic_goal_notes),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    } else if (goal.imageResId != null) {
                        // Use null-safe approach to avoid smart cast issue
                        val resId = goal.imageResId ?: R.drawable.ic_goal_notes
                        Icon(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            tint = Color.Unspecified, // Use original colors for doodle art style
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Text below the image
                Text(
                    text = goal.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (goal.status) {
                        GoalStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Dimmed text for archived
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            // Status overlays
            when (goal.status) {
                GoalStatus.COMPLETED -> {
                    // Mountain sunrise theme completion glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        RusticGold.copy(alpha = 0.15f),
                                        AmberHorizon.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        RusticGold.copy(alpha = 0.6f), // Gold glow theo theme
                                        RusticGold.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Mountain sunrise theme completion stamp
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // Outer circle with theme gold
                            drawCircle(
                                color = RusticGold.copy(alpha = 0.8f),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 4f)
                            )

                            // Inner circle with warm amber
                            drawCircle(
                                color = AmberHorizon.copy(alpha = 0.4f),
                                radius = size.minDimension / 2 - 8f
                            )

                            // Decorative sunrise rays
                            for (i in 0 until 8) {
                                rotate(degrees = i * 45f) {
                                    drawLine(
                                        color = RusticGold.copy(alpha = 0.7f),
                                        start = center + Offset(0f, -size.minDimension / 4),
                                        end = center + Offset(0f, -size.minDimension / 2 + 4f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }

                        // "DONE" text with theme colors
                        Text(
                            text = "✨ DONE",
                            color = RusticGold,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        )
                    }
                }
                GoalStatus.FAILED -> {
                    // Mountain theme failed overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        DustyRose.copy(alpha = 0.12f),
                                        DustyRose.copy(alpha = 0.06f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // X mark with theme color
                            drawLine(
                                color = DustyRose.copy(alpha = 0.8f),
                                start = Offset(size.width * 0.3f, size.height * 0.3f),
                                end = Offset(size.width * 0.7f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = DustyRose.copy(alpha = 0.8f),
                                start = Offset(size.width * 0.7f, size.height * 0.3f),
                                end = Offset(size.width * 0.3f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                        }

                        Text(
                            text = "☒ FAILED",
                            color = DustyRose,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                                .padding(top = 40.dp)
                        )
                    }
                }
                GoalStatus.ARCHIVED -> {
                    // Elegant archived ribbon overlay
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Diagonal ribbon background
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = -12f
                                }
                        ) {
                            // Ribbon background with mountain theme gradient
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MountainShadow.copy(alpha = 0.85f),
                                        MountainShadow.copy(alpha = 0.65f)
                                    )
                                ),
                                size = Size(
                                    width = size.width * 1.4f,
                                    height = size.height * 0.25f
                                ),
                                topLeft = Offset(
                                    x = -size.width * 0.2f,
                                    y = size.height * 0.375f
                                )
                            )

                            // Ribbon shadow/depth
                            drawRect(
                                color = MountainShadow.copy(alpha = 0.3f),
                                size = Size(
                                    width = size.width * 1.4f,
                                    height = size.height * 0.25f
                                ),
                                topLeft = Offset(
                                    x = -size.width * 0.2f + 3f,
                                    y = size.height * 0.375f + 3f
                                )
                            )
                        }

                        // Archive text on ribbon
                        Text(
                            text = "ARCHIVED",
                            color = White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -12f
                                }
                        )
                    }
                }
                GoalStatus.ACTIVE -> {
                    //LinhKD Debug 1
                    // Mountain theme active indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(16.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MistyLavender.copy(alpha = 0.8f),
                                        MistyLavender.copy(alpha = 0.6f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center // Đây là key để căn giữa icon
                    ) {
                        Text(
                            text = "⏳",
                            fontSize = 10.sp,
                            maxLines = 1,
                            lineHeight = 10.sp, // = fontSize để không dư leading
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
                else -> {
                    // No overlay for other statuses
                }
            }
        }
    }
}
