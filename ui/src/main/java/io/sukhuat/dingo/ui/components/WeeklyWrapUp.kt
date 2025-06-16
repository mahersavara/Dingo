package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus

/**
 * A list of motivational quotes to display in the weekly wrap-up
 */
private val motivationalQuotes = listOf(
    "Every accomplishment starts with the decision to try.",
    "Small progress is still progress.",
    "The journey of a thousand miles begins with a single step.",
    "Success is not final, failure is not fatal: it is the courage to continue that counts.",
    "You don't have to be great to start, but you have to start to be great.",
    "The only way to do great work is to love what you do.",
    "Believe you can and you're halfway there.",
    "It always seems impossible until it's done.",
    "Don't watch the clock; do what it does. Keep going.",
    "The future depends on what you do today."
)

/**
 * Weekly Wrap-Up component that shows a summary of completed goals at the end of the week
 * 
 * @param completedGoals List of completed goals
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
    // Get a random motivational quote
    val randomQuote = motivationalQuotes.random()
    
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
                    text = "Weekly Wrap-Up",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = RusticGold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress summary with a friendly tone
                Text(
                    text = "You completed ${completedGoals.size}/$totalGoals goals this week!",
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
                        text = "Goals for Next Week",
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
                            text = "❌ $missedGoals ${if (missedGoals == 1) "goal" else "goals"} to carry forward",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Every step counts! You'll get there.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Congratulate on completing all goals
                    Text(
                        text = "Amazing work! You completed all your goals! 🎉",
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
                        Text("Share")
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OK")
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