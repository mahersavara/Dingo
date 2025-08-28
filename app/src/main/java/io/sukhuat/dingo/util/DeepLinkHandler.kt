package io.sukhuat.dingo.util

import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Handles deep link navigation for the Dingo app
 * Supports authentication callbacks and profile navigation
 */
object DeepLinkHandler {

    private const val TAG = "DeepLinkHandler"

    /**
     * Represents different deep link destinations
     */
    sealed class DeepLinkDestination {
        // Authentication deep links
        data class PasswordResetSuccess(val continueUrl: String? = null) : DeepLinkDestination()
        data class EmailVerificationSuccess(val continueUrl: String? = null) : DeepLinkDestination()
        data class AuthError(val error: String, val message: String? = null) : DeepLinkDestination()

        // Profile deep links
        data class ProfileSection(val section: String) : DeepLinkDestination()

        // Unknown or invalid deep link
        object Unknown : DeepLinkDestination()
    }

    /**
     * Parses an intent to determine the deep link destination
     * @param intent The intent containing the deep link data
     * @return The parsed deep link destination
     */
    fun parseDeepLink(intent: Intent?): DeepLinkDestination {
        val data = intent?.data ?: return DeepLinkDestination.Unknown

        Log.d(TAG, "Parsing deep link: $data")

        return when {
            // Password reset callback
            isPasswordResetCallback(data) -> parsePasswordResetCallback(data)

            // Email verification callback
            isEmailVerificationCallback(data) -> parseEmailVerificationCallback(data)

            // Profile deep link
            isProfileDeepLink(data) -> parseProfileDeepLink(data)

            else -> {
                Log.w(TAG, "Unknown deep link format: $data")
                DeepLinkDestination.Unknown
            }
        }
    }

    /**
     * Checks if the URI is a password reset callback
     */
    private fun isPasswordResetCallback(uri: Uri): Boolean {
        return uri.scheme == "https" && uri.host == "dingo-app.com" && uri.path?.startsWith("/auth/reset") == true
    }

    /**
     * Checks if the URI is an email verification callback
     */
    private fun isEmailVerificationCallback(uri: Uri): Boolean {
        return uri.scheme == "https" && uri.host == "dingo-app.com" && uri.path?.startsWith("/verify-email") == true
    }

    /**
     * Checks if the URI is a profile deep link
     */
    private fun isProfileDeepLink(uri: Uri): Boolean {
        return uri.scheme == "dingo" && uri.host == "profile"
    }

    /**
     * Parses password reset callback parameters
     */
    private fun parsePasswordResetCallback(uri: Uri): DeepLinkDestination {
        val mode = uri.getQueryParameter("mode")
        val oobCode = uri.getQueryParameter("oobCode")
        val continueUrl = uri.getQueryParameter("continueUrl")
        val error = uri.getQueryParameter("error")
        val errorMessage = uri.getQueryParameter("errorMessage")

        Log.d(TAG, "Password reset callback - mode: $mode, oobCode: $oobCode, error: $error")

        return when {
            error != null -> DeepLinkDestination.AuthError(error, errorMessage)
            mode == "resetPassword" && oobCode != null -> DeepLinkDestination.PasswordResetSuccess(continueUrl)
            else -> {
                Log.w(TAG, "Invalid password reset callback parameters")
                DeepLinkDestination.AuthError("invalid_request", "Invalid password reset link")
            }
        }
    }

    /**
     * Parses email verification callback parameters
     */
    private fun parseEmailVerificationCallback(uri: Uri): DeepLinkDestination {
        val mode = uri.getQueryParameter("mode")
        val oobCode = uri.getQueryParameter("oobCode")
        val continueUrl = uri.getQueryParameter("continueUrl")
        val error = uri.getQueryParameter("error")
        val errorMessage = uri.getQueryParameter("errorMessage")

        Log.d(TAG, "Email verification callback - mode: $mode, oobCode: $oobCode, error: $error")

        return when {
            error != null -> DeepLinkDestination.AuthError(error, errorMessage)
            mode == "verifyEmail" && oobCode != null -> DeepLinkDestination.EmailVerificationSuccess(continueUrl)
            else -> {
                Log.w(TAG, "Invalid email verification callback parameters")
                DeepLinkDestination.AuthError("invalid_request", "Invalid verification link")
            }
        }
    }

    /**
     * Parses profile deep link parameters
     */
    private fun parseProfileDeepLink(uri: Uri): DeepLinkDestination {
        val section = uri.pathSegments.firstOrNull() ?: "overview"
        Log.d(TAG, "Profile deep link - section: $section")
        return DeepLinkDestination.ProfileSection(section)
    }

    /**
     * Gets a user-friendly error message for auth errors
     */
    fun getAuthErrorMessage(error: String): String {
        return when (error) {
            "expired-action-code" -> "This link has expired. Please request a new one."
            "invalid-action-code" -> "This link is invalid or has already been used."
            "user-disabled" -> "Your account has been disabled. Please contact support."
            "user-not-found" -> "No account found with this email address."
            "invalid_request" -> "Invalid request. Please try again."
            "network_error" -> "Network error. Please check your connection and try again."
            else -> "An error occurred. Please try again."
        }
    }

    /**
     * Checks if the deep link requires authentication
     */
    fun requiresAuthentication(destination: DeepLinkDestination): Boolean {
        return when (destination) {
            is DeepLinkDestination.ProfileSection -> true
            is DeepLinkDestination.PasswordResetSuccess,
            is DeepLinkDestination.EmailVerificationSuccess,
            is DeepLinkDestination.AuthError,
            is DeepLinkDestination.Unknown -> false
        }
    }
}
