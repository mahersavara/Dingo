package io.sukhuat.dingo.data.auth

import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import io.sukhuat.dingo.domain.repository.AuthResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages password reset functionality using Firebase Auth default approach
 * Implements the Firebase default link approach from the PRD
 */
@Singleton
class PasswordResetManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val TAG = "PasswordResetManager"
        private const val RATE_LIMIT_REQUESTS = 3
        private const val RATE_LIMIT_WINDOW_HOURS = 1
    }

    // Track reset requests for rate limiting (per email)
    private val resetRequestTracker = mutableMapOf<String, MutableList<Long>>()

    /**
     * Sends password reset email using Firebase default approach
     * The user will receive an email with a link to Firebase's web UI for password reset
     * @param email User's email address
     * @return AuthResult indicating success or failure
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            Log.d(TAG, "Starting password reset for email: $email")

            // Validate email format
            if (!isValidEmail(email)) {
                Log.w(TAG, "Invalid email format: $email")
                return AuthResult.Error("Please enter a valid email address")
            }

            // Check rate limiting
            val rateLimitResult = checkRateLimit(email)
            if (rateLimitResult is AuthResult.Error) {
                Log.w(TAG, "Rate limit exceeded for email: $email")
                return rateLimitResult
            }

            Log.d(TAG, "Attempting to send Firebase password reset email to: $email")

            // Send reset email using Firebase default (no custom ActionCodeSettings)
            // This uses Firebase's default web UI for password reset
            firebaseAuth.sendPasswordResetEmail(email).await()

            // Track the request for rate limiting
            trackResetRequest(email)

            Log.d(TAG, "Password reset email sent successfully to: $email")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email to: $email", e)

            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> {
                    "No account found with this email address"
                }
                else -> when {
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection and try again."
                    e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                        "Too many reset requests. Please try again later."
                    else -> "Failed to send password reset email. Please try again."
                }
            }

            AuthResult.Error(errorMessage, e)
        }
    }

    /**
     * Validates email format using Android's Patterns utility
     * @param email Email address to validate
     * @return true if email is valid, false otherwise
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the email has exceeded rate limits
     * @param email Email to check
     * @return AuthResult indicating if request can proceed
     */
    private fun checkRateLimit(email: String): AuthResult<Unit> {
        val currentTime = System.currentTimeMillis()
        val windowStart = currentTime - (RATE_LIMIT_WINDOW_HOURS * 60 * 60 * 1000)

        // Get or create request list for this email
        val requests = resetRequestTracker.getOrPut(email) { mutableListOf() }

        // Remove expired requests
        requests.removeAll { it < windowStart }

        // Check if limit exceeded
        if (requests.size >= RATE_LIMIT_REQUESTS) {
            val oldestRequest = requests.minOrNull() ?: currentTime
            val remainingMinutes = ((oldestRequest + RATE_LIMIT_WINDOW_HOURS * 60 * 60 * 1000 - currentTime) / (60 * 1000)).toInt()
            return AuthResult.Error("Too many password reset requests. Please try again in $remainingMinutes minutes.")
        }

        return AuthResult.Success(Unit)
    }

    /**
     * Tracks a password reset request for rate limiting
     * @param email Email that made the request
     */
    private fun trackResetRequest(email: String) {
        val requests = resetRequestTracker.getOrPut(email) { mutableListOf() }
        requests.add(System.currentTimeMillis())

        // Clean up old entries to prevent memory leaks
        cleanupOldRequests()
    }

    /**
     * Cleans up old request tracking data to prevent memory leaks
     */
    private fun cleanupOldRequests() {
        val currentTime = System.currentTimeMillis()
        val windowStart = currentTime - (RATE_LIMIT_WINDOW_HOURS * 60 * 60 * 1000)

        resetRequestTracker.forEach { (email, requests) ->
            requests.removeAll { it < windowStart }
        }

        // Remove empty entries
        resetRequestTracker.entries.removeAll { it.value.isEmpty() }
    }

    /**
     * Gets remaining reset attempts for an email
     * @param email Email to check
     * @return Number of remaining attempts
     */
    fun getRemainingAttempts(email: String): Int {
        val currentTime = System.currentTimeMillis()
        val windowStart = currentTime - (RATE_LIMIT_WINDOW_HOURS * 60 * 60 * 1000)

        val requests = resetRequestTracker[email] ?: return RATE_LIMIT_REQUESTS
        val recentRequests = requests.count { it >= windowStart }

        return maxOf(0, RATE_LIMIT_REQUESTS - recentRequests)
    }

    /**
     * Checks if an email can make a reset request
     * @param email Email to check
     * @return true if request can be made, false if rate limited
     */
    fun canMakeResetRequest(email: String): Boolean {
        return getRemainingAttempts(email) > 0
    }

    /**
     * Resets rate limiting for testing purposes
     */
    internal fun resetRateLimit() {
        resetRequestTracker.clear()
    }
}
