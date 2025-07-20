package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.ui.screens.profile.LazyLoadingManager
import io.sukhuat.dingo.ui.screens.profile.ProfilePerformanceMonitor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Optimized achievements list with lazy loading and memory efficiency
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptimizedAchievementsList(
    achievements: List<Achievement>,
    onShareAchievement: (String) -> Unit,
    onLoadMore: suspend (page: Int, pageSize: Int) -> List<Achievement>,
    modifier: Modifier = Modifier,
    lazyLoadingManager: LazyLoadingManager = remember { LazyLoadingManager() },
    performanceMonitor: ProfilePerformanceMonitor = remember { ProfilePerformanceMonitor() }
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Use paginated state for lazy loading
    val paginatedState = lazyLoadingManager.rememberPaginatedAchievements(
        loadPage = onLoadMore,
        lazyListState = lazyListState
    )

    // Memory-efficient renderer
    val memoryEfficientRenderer = lazyLoadingManager.rememberMemoryEfficientRenderer()

    // Monitor performance
    LaunchedEffect(paginatedState.items.size) {
        if (paginatedState.items.isNotEmpty()) {
            performanceMonitor.endOperation(
                performanceMonitor.startOperation("achievements_load"),
                success = true,
                itemCount = paginatedState.items.size
            )
        }
    }

    // Virtualized rendering for very large lists
    val shouldUseVirtualization by remember {
        derivedStateOf { paginatedState.items.size > 100 }
    }

    Column(modifier = modifier) {
        if (paginatedState.isLoading && paginatedState.items.isEmpty()) {
            // Initial loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = RusticGold,
                    modifier = Modifier.semantics {
                        contentDescription = "Loading achievements"
                    }
                )
            }
        } else if (paginatedState.items.isEmpty() && paginatedState.error == null) {
            // Empty state
            EmptyAchievementsState(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        } else {
            // Achievements list
            if (shouldUseVirtualization) {
                VirtualizedAchievementsList(
                    achievements = paginatedState.items,
                    lazyListState = lazyListState,
                    memoryEfficientRenderer = memoryEfficientRenderer,
                    onShareAchievement = onShareAchievement,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                StandardAchievementsList(
                    achievements = paginatedState.items,
                    lazyListState = lazyListState,
                    onShareAchievement = onShareAchievement,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Loading more indicator
            if (paginatedState.isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = RusticGold,
                        modifier = Modifier
                            .size(24.dp)
                            .semantics {
                                contentDescription = "Loading more achievements"
                            }
                    )
                }
            }

            // Error state for loading more
            paginatedState.error?.let { error ->
                ErrorRetryButton(
                    errorMessage = "Failed to load achievements",
                    onRetry = {
                        coroutineScope.launch {
                            onLoadMore(0, 20) // Simple retry by reloading first page
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * Standard achievements list for smaller datasets
 */
@Composable
private fun StandardAchievementsList(
    achievements: List<Achievement>,
    lazyListState: LazyListState,
    onShareAchievement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = achievements,
            key = { _, achievement -> achievement.id }
        ) { index, achievement ->
            AchievementItem(
                achievement = achievement,
                onShare = { onShareAchievement(achievement.id) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Virtualized achievements list for very large datasets
 */
@Composable
private fun VirtualizedAchievementsList(
    achievements: List<Achievement>,
    lazyListState: LazyListState,
    memoryEfficientRenderer: io.sukhuat.dingo.ui.screens.profile.MemoryEfficientRenderer,
    onShareAchievement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleItemsInfo by remember {
        derivedStateOf { lazyListState.layoutInfo.visibleItemsInfo }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = achievements.size,
            key = { index -> achievements[index].id }
        ) { index ->
            // Only render items that are visible or in buffer
            val shouldRender = visibleItemsInfo.any {
                kotlin.math.abs(it.index - index) <= 10 // Buffer of 10 items
            }

            if (shouldRender) {
                val achievement = memoryEfficientRenderer.getOrCreateItem(index) {
                    achievements[it]
                }

                AchievementItem(
                    achievement = achievement,
                    onShare = { onShareAchievement(achievement.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            } else {
                // Placeholder for non-visible items
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Approximate height of achievement item
                )
            }
        }
    }
}

/**
 * Individual achievement item with optimized rendering
 */
@Composable
private fun AchievementItem(
    achievement: Achievement,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics {
            contentDescription = if (achievement.isUnlocked) {
                "Achievement ${achievement.title} unlocked"
            } else {
                "Achievement ${achievement.title} locked"
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = if (achievement.isUnlocked) RusticGold else Color.Gray.copy(alpha = 0.3f)
            ) {
                Icon(
                    imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .alpha(if (achievement.isUnlocked) 1f else 0.5f),
                    tint = if (achievement.isUnlocked) Color.White else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Achievement details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                achievement.unlockedDate?.let { timestamp ->
                    Text(
                        text = "Unlocked ${formatDate(timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RusticGold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Share button (only for unlocked achievements)
            if (achievement.isUnlocked) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.semantics {
                        contentDescription = "Share ${achievement.title} achievement"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = RusticGold
                    )
                }
            }
        }
    }
}

/**
 * Empty state for when no achievements are available
 */
@Composable
private fun EmptyAchievementsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = "No achievements yet"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .alpha(0.5f),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No achievements yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = "Complete goals to unlock achievements",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Format timestamp to readable date
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Error retry button component
 */
@Composable
private fun ErrorRetryButton(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.TextButton(
                onClick = onRetry,
                modifier = Modifier.semantics {
                    contentDescription = "Retry loading achievements"
                }
            ) {
                Text(
                    text = "Retry",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
