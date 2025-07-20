package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.Success
import io.sukhuat.dingo.common.theme.WarmOrange
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Profile statistics component displaying user's goal achievement statistics,
 * progress indicators, achievement badges, and monthly/yearly breakdowns
 */
@Composable
fun ProfileStatistics(
    statistics: ProfileStatistics,
    onShareAchievement: (String) -> Unit,
    onRefreshStats: () -> Unit,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDetailedStats by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Statistics Cards
        StatisticsOverview(
            statistics = statistics,
            isRefreshing = isRefreshing,
            onRefresh = onRefreshStats
        )

        // Achievement Badges
        if (statistics.achievements.isNotEmpty()) {
            AchievementSection(
                achievements = statistics.achievements,
                onShareAchievement = onShareAchievement
            )
        }

        // Detailed Statistics Toggle
        DetailedStatsToggle(
            showDetailed = showDetailedStats,
            onToggle = { showDetailedStats = !showDetailedStats }
        )

        // Detailed Statistics (Expandable)
        AnimatedVisibility(
            visible = showDetailedStats,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            DetailedStatistics(statistics = statistics)
        }

        // Empty State for New Users
        if (statistics.totalGoalsCreated == 0) {
            EmptyStatsState()
        }
    }
}

/**
 * Main statistics overview cards
 */
@Composable
private fun StatisticsOverview(
    statistics: ProfileStatistics,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Goals Card
        StatCard(
            title = "Total Goals",
            value = statistics.totalGoalsCreated.toString(),
            icon = Icons.Default.TrendingUp,
            color = MaterialTheme.colorScheme.primary,
            contentDescription = "Total goals created: ${statistics.totalGoalsCreated}",
            modifier = Modifier.weight(1f)
        )

        // Completed Goals Card
        StatCard(
            title = "Completed",
            value = statistics.completedGoals.toString(),
            icon = Icons.Default.EmojiEvents,
            color = Success,
            contentDescription = "Goals completed: ${statistics.completedGoals}",
            modifier = Modifier.weight(1f)
        )

        // Current Streak Card
        StatCard(
            title = "Streak",
            value = "${statistics.currentStreak} days",
            icon = Icons.Default.LocalFireDepartment,
            color = WarmOrange,
            contentDescription = "Current streak: ${statistics.currentStreak} days",
            modifier = Modifier.weight(1f)
        )
    }

    // Completion Rate Progress
    if (statistics.totalGoalsCreated > 0) {
        CompletionRateCard(
            completionRate = statistics.completionRate,
            completedGoals = statistics.completedGoals,
            totalGoals = statistics.totalGoalsCreated
        )
    }
}

/**
 * Individual statistic card
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Completion rate progress card
 */
@Composable
private fun CompletionRateCard(
    completionRate: Float,
    completedGoals: Int,
    totalGoals: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Completion rate: ${(completionRate * 100).toInt()} percent"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completion Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = completionRate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = RusticGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$completedGoals of $totalGoals goals completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Achievement badges section
 */
@Composable
private fun AchievementSection(
    achievements: List<Achievement>,
    onShareAchievement: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${achievements.count { it.isUnlocked }}/${achievements.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                AchievementBadge(
                    achievement = achievement,
                    onShare = { onShareAchievement(achievement.id) }
                )
            }
        }
    }
}

/**
 * Individual achievement badge
 */
@Composable
private fun AchievementBadge(
    achievement: Achievement,
    onShare: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (achievement.isUnlocked) 1f else 0.3f,
        animationSpec = tween(300),
        label = "AchievementAlpha"
    )

    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(enabled = achievement.isUnlocked) { onShare() }
            .semantics {
                contentDescription = if (achievement.isUnlocked && achievement.unlockedDate != null) {
                    val unlockedDate = achievement.unlockedDate
                    val dateStr = unlockedDate?.let {
                        val date = java.util.Date(it)
                        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        formatter.format(date)
                    } ?: "Unknown date"
                    "Achievement: ${achievement.title}. ${achievement.description}. Unlocked on $dateStr"
                } else {
                    "Achievement: ${achievement.title}. ${achievement.description}. Not yet unlocked"
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                RusticGold.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                // Achievement icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (achievement.isUnlocked) {
                        RusticGold
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (achievement.iconResId != 0) {
                            Icon(
                                painter = painterResource(id = achievement.iconResId),
                                contentDescription = achievement.title,
                                tint = if (achievement.isUnlocked) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            // Fallback icon when iconResId is invalid
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = achievement.title,
                                tint = if (achievement.isUnlocked) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Share button for unlocked achievements
                if (achievement.isUnlocked) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share achievement",
                            tint = RusticGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha),
                maxLines = 2
            )

            if (achievement.isUnlocked && achievement.unlockedDate != null) {
                val unlockedDate = achievement.unlockedDate
                unlockedDate?.let { timestamp ->
                    val date = java.util.Date(timestamp)
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    Text(
                        text = formatter.format(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = RusticGold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Toggle for detailed statistics
 */
@Composable
private fun DetailedStatsToggle(
    showDetailed: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showDetailed) "Hide Detailed Stats" else "Show Detailed Stats",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = if (showDetailed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (showDetailed) "Hide" else "Show",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Detailed statistics breakdown
 */
@Composable
private fun DetailedStatistics(statistics: ProfileStatistics) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak Information
        StreakCard(
            currentStreak = statistics.currentStreak,
            longestStreak = statistics.longestStreak
        )

        // Monthly Statistics
        if (statistics.monthlyStats.isNotEmpty()) {
            MonthlyStatsSection(monthlyStats = statistics.monthlyStats)
        }
    }
}

/**
 * Streak information card
 */
@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Streak Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmOrange
                    )
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$longestStreak",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = RusticGold
                    )
                    Text(
                        text = "Best Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Monthly statistics section
 */
@Composable
private fun MonthlyStatsSection(monthlyStats: Map<String, MonthlyStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Monthly Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            monthlyStats.entries.take(6).forEach { (month, stats) ->
                MonthlyStatItem(
                    month = month,
                    stats = stats
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Individual monthly stat item
 */
@Composable
private fun MonthlyStatItem(
    month: String,
    stats: MonthlyStats
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stats.goalsCompleted}/${stats.goalsCreated}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = "${(stats.completionRate * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (stats.completionRate >= 0.8f) Success else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Empty state for users with no goals
 */
@Composable
private fun EmptyStatsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Start Your Journey!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your first goal to see your progress and achievements here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileStatisticsPreview() {
    MountainSunriseTheme {
        ProfileStatistics(
            statistics = ProfileStatistics(
                totalGoalsCreated = 15,
                completedGoals = 12,
                completionRate = 0.8f,
                currentStreak = 7,
                longestStreak = 14,
                monthlyStats = mapOf(
                    "January 2024" to MonthlyStats("January 2024", 5, 4, 0.8f),
                    "February 2024" to MonthlyStats("February 2024", 6, 5, 0.83f),
                    "March 2024" to MonthlyStats("March 2024", 4, 3, 0.75f)
                ),
                achievements = listOf(
                    Achievement(
                        id = "first_goal",
                        title = "First Goal",
                        description = "Created your first goal",
                        iconResId = android.R.drawable.star_on,
                        unlockedDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
                        isUnlocked = true
                    ),
                    Achievement(
                        id = "streak_7",
                        title = "Week Warrior",
                        description = "7-day streak",
                        iconResId = android.R.drawable.star_on,
                        unlockedDate = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L),
                        isUnlocked = true
                    ),
                    Achievement(
                        id = "goals_10",
                        title = "Goal Getter",
                        description = "Completed 10 goals",
                        iconResId = android.R.drawable.star_on,
                        isUnlocked = false
                    )
                )
            ),
            onShareAchievement = {},
            onRefreshStats = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileStatisticsEmptyPreview() {
    MountainSunriseTheme {
        ProfileStatistics(
            statistics = ProfileStatistics(),
            onShareAchievement = {},
            onRefreshStats = {}
        )
    }
}
