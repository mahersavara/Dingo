package io.sukhuat.dingo.ui.screens.yearplanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sukhuat.dingo.common.theme.LocalExtendedColors
import io.sukhuat.dingo.domain.model.yearplanner.MonthData

// Vintage theme colors from PRD
private val ParchmentBackground = Color(0xFFF4E8D0)
private val DarkBrown = Color(0xFF3D2B1F)
private val InkBrown = Color(0xFF8B6914)
private val SepiaShadow = Color(0x1A5D4B3F)

/**
 * Month Card Component - Individual month planning card with vintage styling
 * Implements PRD requirements: always visible content, vintage theme, inline editing
 */
@Composable
fun MonthCard(
    month: MonthData,
    year: Int,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    
    // Local state for immediate UI updates during typing
    var localContent by remember(month.content) { 
        mutableStateOf(month.content) 
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ParchmentBackground,
            contentColor = DarkBrown
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month header with statistics
            MonthHeader(
                monthName = month.name,
                wordCount = month.wordCount,
                isPending = month.isPendingSync
            )
            
            // Vintage divider
            VintageDivider()
            
            // Content editor - always visible, no expand/collapse (PRD requirement)
            MonthContentEditor(
                content = localContent,
                onContentChange = { newContent ->
                    localContent = newContent
                    onContentChange(newContent)
                },
                placeholder = "Tap to add your plans for ${month.name}...",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp) // Minimum height for empty content
            )
        }
    }
}

/**
 * Month Header - Shows month name, word count, and sync status
 */
@Composable
private fun MonthHeader(
    monthName: String,
    wordCount: Int,
    isPending: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month name with serif font (vintage style)
        Text(
            text = monthName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = DarkBrown
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Statistics and status
        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (wordCount > 0) {
                Text(
                    text = "$wordCount words",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = InkBrown
                )
            }
            
            if (isPending) {
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Vintage Divider - Decorative separator with vintage styling
 */
@Composable
private fun VintageDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = InkBrown.copy(alpha = 0.3f)
        )
        
        // Add vintage decorative elements
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    ParchmentBackground,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "✦",
                style = MaterialTheme.typography.labelSmall,
                color = InkBrown.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Month Content Editor - Rich text input with vintage styling
 * PRD requirement: Notion-style shortcuts will be added in Phase 4
 */
@Composable
private fun MonthContentEditor(
    content: String,
    onContentChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = DarkBrown.copy(alpha = 0.6f)
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Serif,
            color = DarkBrown,
            lineHeight = 24.sp
        ),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = InkBrown,
            unfocusedIndicatorColor = InkBrown.copy(alpha = 0.3f),
            cursorColor = InkBrown
        ),
        minLines = 3,
        maxLines = Int.MAX_VALUE, // Allow unlimited growth (PRD: always visible)
        shape = RoundedCornerShape(8.dp)
    )
}