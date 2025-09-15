package io.sukhuat.dingo.widget

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized error handling for widget operations
 */
@Singleton
class WidgetErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Check if device is connected to the internet
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Categorize different types of errors for appropriate handling
     */
    sealed class WidgetError {
        object NetworkUnavailable : WidgetError()
        object DataLoadFailure : WidgetError()
        object AuthenticationError : WidgetError()
        object UnknownError : WidgetError()
        data class CustomError(val message: String) : WidgetError()
    }

    /**
     * Get appropriate error message for display
     */
    fun getErrorMessage(error: WidgetError): String {
        return when (error) {
            is WidgetError.NetworkUnavailable -> "No internet connection"
            is WidgetError.DataLoadFailure -> "Failed to load goals"
            is WidgetError.AuthenticationError -> "Please sign in to view goals"
            is WidgetError.UnknownError -> "Something went wrong"
            is WidgetError.CustomError -> error.message
        }
    }

    /**
     * Get appropriate retry action for error type
     */
    fun canRetry(error: WidgetError): Boolean {
        return when (error) {
            is WidgetError.NetworkUnavailable -> true
            is WidgetError.DataLoadFailure -> true
            is WidgetError.AuthenticationError -> false // Requires user intervention
            is WidgetError.UnknownError -> true
            is WidgetError.CustomError -> true
        }
    }
}

/**
 * Retry action callback for error handling
 */
class RetryWidgetUpdateAction : androidx.glance.appwidget.action.ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: androidx.glance.GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        val widgetSize = parameters[WIDGET_SIZE_KEY] ?: "2x2"

        // Force immediate update of the widget
        WeeklyGoalWidget.update(context, glanceId)
    }

    companion object {
        val WIDGET_SIZE_KEY = androidx.glance.action.ActionParameters.Key<String>("widget_size")
    }
}

@Composable
fun WidgetErrorContent(
    error: WidgetErrorHandler.WidgetError,
    errorHandler: WidgetErrorHandler? = null,
    onRetry: (() -> androidx.glance.action.Action)? = null,
    onOpenApp: () -> androidx.glance.action.Action = { actionStartActivity(MainActivity::class.java) }
) {
    val backgroundColor = Color(0xFFFDF2E9) // Mountain Sunrise background

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Error icon
            Text(
                text = when (error) {
                    is WidgetErrorHandler.WidgetError.NetworkUnavailable -> "📶"
                    is WidgetErrorHandler.WidgetError.AuthenticationError -> "🔐"
                    else -> "⚠️"
                },
                style = TextStyle(fontSize = 24.sp)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Error message
            Text(
                text = errorHandler?.getErrorMessage(error) ?: getDefaultErrorMessage(error),
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color(0xFF6B7280)),
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Action buttons
            Row(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Retry button (if applicable)
                if ((errorHandler?.canRetry(error) ?: canRetryDefault(error)) && onRetry != null) {
                    Text(
                        text = "Retry",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(Color(0xFFD97706))
                        ),
                        modifier = GlanceModifier
                            .clickable(onRetry())
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Open app button
                Text(
                    text = "Open App",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFF92400E))
                    ),
                    modifier = GlanceModifier
                        .clickable(onOpenApp())
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun WidgetLoadingContent() {
    val backgroundColor = Color(0xFFFDF2E9) // Mountain Sunrise background

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(backgroundColor))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏳",
                style = TextStyle(fontSize = 24.sp)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "🚨 WIDGET FIXED! 🚨",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color(0xFF6B7280)),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * Default error message when errorHandler is not available
 */
private fun getDefaultErrorMessage(error: WidgetErrorHandler.WidgetError): String {
    return when (error) {
        is WidgetErrorHandler.WidgetError.NetworkUnavailable -> "No internet connection"
        is WidgetErrorHandler.WidgetError.DataLoadFailure -> "Failed to load goals"
        is WidgetErrorHandler.WidgetError.AuthenticationError -> "Please sign in to view goals"
        is WidgetErrorHandler.WidgetError.UnknownError -> "Something went wrong"
        is WidgetErrorHandler.WidgetError.CustomError -> error.message
    }
}

/**
 * Default retry logic when errorHandler is not available
 */
private fun canRetryDefault(error: WidgetErrorHandler.WidgetError): Boolean {
    return when (error) {
        is WidgetErrorHandler.WidgetError.NetworkUnavailable -> true
        is WidgetErrorHandler.WidgetError.DataLoadFailure -> true
        is WidgetErrorHandler.WidgetError.AuthenticationError -> false
        is WidgetErrorHandler.WidgetError.UnknownError -> true
        is WidgetErrorHandler.WidgetError.CustomError -> true
    }
}
