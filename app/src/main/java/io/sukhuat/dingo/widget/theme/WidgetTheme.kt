package io.sukhuat.dingo.widget.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized Mountain Sunrise theme for all widget components
 * Ensures consistent theming across widget sizes and states
 */
object WidgetTheme {

    // Primary Mountain Sunrise Colors
    object Colors {
        // Background Colors
        val WidgetBackground = Color(0xFFFDF2E9) // Warm cream - main background
        val CardBackgroundActive = Color(0xFFFFFBEB) // Warm cream - active goals
        val CardBackgroundCompleted = Color(0xFFF0F9FF) // Light blue - completed goals
        val CardBackgroundFailed = Color(0xFFFEF2F2) // Light red - failed goals
        val CardBackgroundArchived = Color(0xFFF9FAFB) // Light gray - archived goals
        val EmptySlotBackground = Color(0xFFF9FAFB) // Light gray - empty slots

        // Border and Accent Colors
        val AccentOrange = Color(0xFFD97706) // Warm orange - primary accent
        val AccentBrown = Color(0xFF92400E) // Warm brown - secondary accent
        val SuccessGreen = Color(0xFF059669) // Green - success/completed
        val SuccessGreenDark = Color(0xFF047857) // Dark green - success text
        val ErrorRed = Color(0xFFDC2626) // Red - error/failed
        val ErrorRedDark = Color(0xFF991B1B) // Dark red - error text
        val NeutralGray = Color(0xFF6B7280) // Gray - neutral elements
        val NeutralGrayDark = Color(0xFF374151) // Dark gray - neutral text

        // Text Colors
        val PrimaryText = AccentBrown // Main text color
        val SecondaryText = NeutralGray // Secondary text color
        val DisabledText = Color(0xFF9CA3AF) // Disabled text

        // Status-specific colors
        val ActiveIndicator = AccentOrange
        val CompletedIndicator = SuccessGreen
        val FailedIndicator = ErrorRed
        val ArchivedIndicator = NeutralGray
    }

    // Typography sizes for different widget sizes
    object Typography {
        // 2x2 Widget (Compact)
        object Compact {
            val HeaderSize = 12f
            val StatusIndicatorSize = 12f
            val GoalTextSize = 9f
            val ButtonSize = 10f
        }

        // 2x3 Widget (Vertical)
        object Vertical {
            val HeaderSize = 14f
            val StatusIndicatorSize = 14f
            val GoalTextSize = 10f
            val ButtonSize = 11f
        }

        // 3x2 Widget (Horizontal)
        object Horizontal {
            val HeaderSize = 12f
            val StatusIndicatorSize = 10f
            val GoalTextSize = 8f
            val ButtonSize = 9f
        }

        // Error and Loading states
        object States {
            val ErrorIconSize = 20f
            val ErrorTextSize = 11f
            val LoadingTextSize = 11f
        }
    }

    // Spacing and dimensions
    object Dimensions {
        // Padding and margins
        val WidgetPadding = 8f
        val CardPadding = 4f
        val ContentPadding = 6f
        val ButtonPadding = 4f

        // Sizes
        val GoalCardHeight2x2 = 64f
        val GoalCardHeight2x3 = 56f
        val GoalCardHeight3x2 = 50f

        // Spacing
        val SmallSpacing = 3f
        val MediumSpacing = 4f
        val LargeSpacing = 8f
    }

    // Status indicators with consistent symbols
    object StatusIndicators {
        const val ACTIVE = "○"
        const val COMPLETED = "✓"
        const val FAILED = "✗"
        const val ARCHIVED = "◐"
        const val EMPTY_SLOT = "+"
        const val LOADING = "⏳"
        const val ERROR = "⚠️"
        const val NETWORK_ERROR = "📶"
        const val AUTH_ERROR = "🔐"
        const val RETRY = "↻"
    }

    // Navigation symbols
    object Navigation {
        const val PREVIOUS = "◀"
        const val NEXT = "▶"
        const val OPEN_APP = "↗"
    }

    /**
     * Get status-specific theme data
     */
    fun getStatusTheme(status: io.sukhuat.dingo.domain.model.GoalStatus): StatusTheme {
        return when (status) {
            io.sukhuat.dingo.domain.model.GoalStatus.ACTIVE -> StatusTheme(
                backgroundColor = Colors.CardBackgroundActive,
                borderColor = Colors.AccentOrange,
                textColor = Colors.AccentBrown,
                indicator = StatusIndicators.ACTIVE
            )
            io.sukhuat.dingo.domain.model.GoalStatus.COMPLETED -> StatusTheme(
                backgroundColor = Colors.CardBackgroundCompleted,
                borderColor = Colors.SuccessGreen,
                textColor = Colors.SuccessGreenDark,
                indicator = StatusIndicators.COMPLETED
            )
            io.sukhuat.dingo.domain.model.GoalStatus.FAILED -> StatusTheme(
                backgroundColor = Colors.CardBackgroundFailed,
                borderColor = Colors.ErrorRed,
                textColor = Colors.ErrorRedDark,
                indicator = StatusIndicators.FAILED
            )
            io.sukhuat.dingo.domain.model.GoalStatus.ARCHIVED -> StatusTheme(
                backgroundColor = Colors.CardBackgroundArchived,
                borderColor = Colors.NeutralGray,
                textColor = Colors.NeutralGrayDark,
                indicator = StatusIndicators.ARCHIVED
            )
        }
    }

    /**
     * Get error-specific theme data
     */
    fun getErrorTheme(error: io.sukhuat.dingo.widget.WidgetErrorHandler.WidgetError): ErrorTheme {
        return when (error) {
            is io.sukhuat.dingo.widget.WidgetErrorHandler.WidgetError.NetworkUnavailable -> ErrorTheme(
                icon = StatusIndicators.NETWORK_ERROR,
                textColor = Colors.SecondaryText,
                canRetry = true
            )
            is io.sukhuat.dingo.widget.WidgetErrorHandler.WidgetError.AuthenticationError -> ErrorTheme(
                icon = StatusIndicators.AUTH_ERROR,
                textColor = Colors.SecondaryText,
                canRetry = false
            )
            else -> ErrorTheme(
                icon = StatusIndicators.ERROR,
                textColor = Colors.SecondaryText,
                canRetry = true
            )
        }
    }
}

/**
 * Theme data for goal status
 */
data class StatusTheme(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val indicator: String
)

/**
 * Theme data for error states
 */
data class ErrorTheme(
    val icon: String,
    val textColor: Color,
    val canRetry: Boolean
)
