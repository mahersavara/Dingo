package io.sukhuat.dingo.ui.screens.yearplanner

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.ui.components.DingoAppScaffold
import io.sukhuat.dingo.ui.screens.yearplanner.components.MonthCard
import java.util.Calendar

/**
 * Year Planner Screen - Main screen for year planning with 12 month cards
 * Implements PRD requirements: multi-year navigation, always visible content, vintage theme
 */
@Composable
fun YearPlannerScreen(
    year: Int = Calendar.getInstance().get(Calendar.YEAR),
    onNavigateBack: () -> Unit = {},
    viewModel: YearPlannerViewModel = hiltViewModel()
) {
    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()

    // Update year when parameter changes
    LaunchedEffect(year) {
        viewModel.loadYear(year)
    }

    DingoAppScaffold(
        title = currentYear.toString(),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        useGradientBackground = true,
        isLoading = uiState is YearPlannerUiState.Loading
    ) { paddingValues ->

        when (uiState) {
            is YearPlannerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            is YearPlannerUiState.Error -> {
                val errorState = uiState as YearPlannerUiState.Error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading year planner",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            is YearPlannerUiState.Success -> {
                val successState = uiState as YearPlannerUiState.Success
                YearPlannerContent(
                    yearPlan = successState.yearPlan,
                    onMonthContentChanged = { monthIndex, content ->
                        viewModel.updateMonthContent(monthIndex, content)
                    },
                    onSwipeLeft = { viewModel.navigateToNextYear() },
                    onSwipeRight = { viewModel.navigateToPreviousYear() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Year Planner Content - Displays all 12 months in a scrollable list
 * PRD requirement: Always show full content, no expand/collapse
 */
@Composable
private fun YearPlannerContent(
    yearPlan: io.sukhuat.dingo.domain.model.yearplanner.YearPlan,
    onMonthContentChanged: (Int, String) -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Swipe gesture state - same as HomeScreen
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(yearPlan.year) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > swipeThreshold -> {
                                // Swipe right - go to previous year
                                onSwipeRight()
                            }
                            offsetX < -swipeThreshold -> {
                                // Swipe left - go to next year
                                onSwipeLeft()
                            }
                        }
                        offsetX = 0f
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount.x
                    // Limit offset to prevent excessive dragging - same as HomeScreen
                    offsetX = offsetX.coerceIn(-swipeThreshold * 2, swipeThreshold * 2)
                }
            }
    ) {
        // Main content with swipe offset animation - same as HomeScreen
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .graphicsLayer {
                    // Add visual feedback for swipe - same as HomeScreen
                    alpha = 1f - (kotlin.math.abs(offsetX) / (swipeThreshold * 2)).coerceIn(0f, 0.3f)
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Year header with statistics
                item {
                    YearHeader(
                        year = yearPlan.year,
                        statistics = yearPlan.getStatistics(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                // Month cards - always show all 12 months
                items(yearPlan.months) { month ->
                    MonthCard(
                        month = month,
                        year = yearPlan.year,
                        onContentChange = { content ->
                            onMonthContentChanged(month.index, content)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Bottom padding for better scrolling
                item {
                    Box(modifier = Modifier.padding(bottom = 32.dp))
                }
            }
        }
        
        // Left swipe indicator (previous year) - same as HomeScreen
        if (offsetX > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = (offsetX / swipeThreshold).coerceIn(0f, 0.8f)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous year",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        // Right swipe indicator (next year) - same as HomeScreen
        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = (kotlin.math.abs(offsetX) / swipeThreshold).coerceIn(0f, 0.8f)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next year",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Year Header - Shows year and completion statistics
 */
@Composable
private fun YearHeader(
    year: Int,
    statistics: io.sukhuat.dingo.domain.model.yearplanner.YearPlanStatistics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${statistics.monthsWithContent} of 12 months planned",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (statistics.totalWords > 0) {
            Text(
                text = "${statistics.totalWords} words total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
