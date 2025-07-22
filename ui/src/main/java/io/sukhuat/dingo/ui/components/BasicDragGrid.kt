package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sukhuat.dingo.domain.model.Goal

/**
 * Basic working goals grid with simple tap and long press
 */
@Composable
fun BasicDragGrid(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    onGoalReorder: (Goal, Int) -> Unit,
    onEmptyPositionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Create a fixed 12-position grid
    val gridPositions = Array<Goal?>(12) { null }

    // Populate grid based on position field
    goals.forEach { goal ->
        val position = goal.position.coerceIn(0, 11)
        if (gridPositions[position] == null) {
            gridPositions[position] = goal
        } else {
            // Handle position conflict - find next available slot
            val nextAvailable = gridPositions.indexOfFirst { it == null }
            if (nextAvailable != -1) {
                gridPositions[nextAvailable] = goal
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(gridPositions.toList()) { index, goal ->
            if (goal != null) {
                BasicGoalItem(
                    goal = goal,
                    onGoalClick = onGoalClick,
                    onGoalLongPress = onGoalLongPress,
                    modifier = Modifier.aspectRatio(1f)
                )
            } else {
                BasicEmptyGridPosition(
                    position = index,
                    onTap = { onEmptyPositionClick(index) },
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
    }
}

/**
 * Basic goal item with simple tap and long press
 */
@Composable
fun BasicGoalItem(
    goal: Goal,
    onGoalClick: (Goal) -> Unit,
    onGoalLongPress: (Goal, Pair<Float, Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .pointerInput(goal.id) {
                detectTapGestures(
                    onTap = { onGoalClick(goal) },
                    onLongPress = { offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onGoalLongPress(goal, Pair(offset.x, offset.y))
                    }
                )
            }
    ) {
        GoalCell(
            goal = goal,
            isDragged = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Basic empty grid position
 */
@Composable
fun BasicEmptyGridPosition(
    position: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                    contentDescription = "Add goal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${position + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                )
            }
        }
    }
}
