package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * Get localized motivational quotes
 */
@Composable
private fun getMotivationalQuotes(): List<String> = listOf(
    stringResource(R.string.quote_1),
    stringResource(R.string.quote_2), stringResource(R.string.quote_3),
    stringResource(R.string.quote_4),
    stringResource(R.string.quote_5),
    stringResource(R.string.quote_6),
    stringResource(R.string.quote_7),
    stringResource(R.string.quote_8),
    stringResource(R.string.quote_9),
    stringResource(R.string.quote_10)
)

/**
 * Weekly Wrap-Up component that shows a summary of completed goals at the end of the week
 * * @param completedGoals List of completed goals
 * @param totalGoals Total number of goals
 * @param onDismiss Called when the user dismisses the dialog
 * @param onShare Called when the user clicks the share button
 */
@Composable
fun WeeklyWrapUp(
    completedGoals: List<Goal>,
    totalGoals: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {}
) {
    // Get localized motivational quotes and pick random one
    val quotes = getMotivationalQuotes()
    val randomQuote = quotes.random()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = stringResource(R.string.weekly_wrapup_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress summary with a friendly tone
                Text(
                    text = stringResource(
                        R.string.goals_completed_this_week
                    ) + " " + stringResource(
                        R.string.out_of,
                        completedGoals.size,
                        totalGoals
                    ) + "!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar with a softer, more encouraging look
                LinearProgressIndicator(
                    progress = { if (totalGoals > 0) completedGoals.size.toFloat() / totalGoals else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = RusticGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Missed goals section with a more supportive tone
                val missedGoals = totalGoals - completedGoals.size
                if (missedGoals > 0) {
                    Text(
                        text = stringResource(R.string.goals_for_next_week),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // List missed goals with a more encouraging message
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "❌ " + stringResource(
                                R.string.goals_to_carry_forward,
                                missedGoals
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.encouragement_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Congratulate on completing all goals
                    Text(
                        text = stringResource(R.string.all_goals_completed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Motivational quote with a softer presentation
                Text(
                    text = "\"$randomQuote\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Share button
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.share_progress))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.continue_journey))
                    }
                }
            }
        }
    }
}

/**
 * Preview for the WeeklyWrapUp component
 */
@Composable
@androidx.compose.ui.tooling.preview.Preview
fun WeeklyWrapUpPreview() {
    MaterialTheme {
        WeeklyWrapUp(
            completedGoals = listOf(
                Goal(
                    id = "1",
                    text = "Finish project",
                    imageResId = null,
                    status = GoalStatus.COMPLETED,
                    createdAt = System.currentTimeMillis(),
                    customImage = null,
                    imageUrl = null,
                    position = 0
                ),
                Goal(
                    id = "2",
                    text = "Go for a run",
                    imageResId = null,
                    status = GoalStatus.COMPLETED,
                    createdAt = System.currentTimeMillis(),
                    customImage = null,
                    imageUrl = null,
                    position = 1
                )
            ),
            totalGoals = 5,
            onDismiss = {},
            onShare = {}
        )
    }
}
