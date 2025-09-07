package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sukhuat.dingo.common.R
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
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragged) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when (goal.status) {
                GoalStatus.COMPLETED -> Color(0xFFE8F5E9) // Light green for completed
                GoalStatus.FAILED -> Color(0xFFFFEBEE) // Light red for failed
                GoalStatus.ARCHIVED -> Color(0xFFF5F5F5) // Light gray for archived
                else -> MaterialTheme.colorScheme.surface
            }
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
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(16.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_upload),
                                        contentDescription = "Stored in Firebase",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
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
                    // Overlay for completed goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55FFFFFF)), // Slightly more opaque white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        // Stylized completion mark overlay
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // Outer circle
                            drawCircle(
                                color = Color(0xDD4CAF50), // More opaque green
                                radius = size.minDimension / 2,
                                style = Stroke(width = 4f)
                            )

                            // Inner circle
                            drawCircle(
                                color = Color(0x554CAF50), // More visible green
                                radius = size.minDimension / 2 - 8f
                            )

                            // Decorative lines
                            for (i in 0 until 8) {
                                rotate(degrees = i * 45f) {
                                    drawLine(
                                        color = Color(0xDD4CAF50), // More opaque green
                                        start = center + Offset(0f, -size.minDimension / 4),
                                        end = center + Offset(0f, -size.minDimension / 2 + 4f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }

                        // "DONE" text in the center of the stamp
                        Text(
                            text = "✅ DONE",
                            color = Color(0xFF4CAF50), // Green color
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
                    // Overlay for failed goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55FFFFFF)), // Slightly more opaque white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // X mark
                            drawLine(
                                color = Color(0xDDFF5252), // More opaque red
                                start = Offset(size.width * 0.3f, size.height * 0.3f),
                                end = Offset(size.width * 0.7f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                            drawLine(
                                color = Color(0xDDFF5252), // More opaque red
                                start = Offset(size.width * 0.7f, size.height * 0.3f),
                                end = Offset(size.width * 0.3f, size.height * 0.7f),
                                strokeWidth = 5f
                            )
                        }

                        Text(
                            text = "❌ FAILED",
                            color = Color(0xFFFF5252), // Red color
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
                    // Overlay for archived goals
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55FFFFFF)), // Slightly more opaque white overlay to dim content
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        ) {
                            // Outer rectangle with rounded corners
                            drawRoundRect(
                                color = Color(0xDD9E9E9E), // More opaque gray
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 3f)
                            )
                        }

                        Text(
                            text = "⛔ ARCHIVED",
                            color = Color(0xFF9E9E9E), // Gray color
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -15f
                                }
                        )
                    }
                }
                GoalStatus.ACTIVE -> {
                    // Add a subtle indicator for active goals
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(16.dp)
                            .background(Color(0xFF64B5F6), CircleShape)
                    ) {
                        Text(
                            text = "⏳",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxSize()
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
