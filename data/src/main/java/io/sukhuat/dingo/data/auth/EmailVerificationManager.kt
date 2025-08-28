package io.sukhuat.dingo.data.auth

import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import io.sukhuat.dingo.domain.repository.AuthResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages email verification functionality using Firebase Auth
 * Implements the email verification requirements from the PRD
 */
@Singleton
class EmailVerificationManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val TAG = "EmailVerificationManager"
        private const val RESEND_COOLDOWN_SECONDS = 60
        private const val VERIFICATION_URL = "https://dingo-app.com/verify-email"
        private const val ANDROID_PACKAGE_NAME = "io.sukhuat.dingo"
    }

    private var lastEmailSentTime: Long = 0

    /**
     * Sends email verification to the current user
     * @return AuthResult indicating success or failure
     */
    suspend fun sendEmailVerification(): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.Error("No authenticated user found")

            // Check if user is already verified
            if (user.isEmailVerified) {
                return AuthResult.Success(Unit)
            }

            // Check rate limiting
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmailSentTime < RESEND_COOLDOWN_SECONDS * 1000) {
                val remainingSeconds = RESEND_COOLDOWN_SECONDS - ((currentTime - lastEmailSentTime) / 1000)
                return AuthResult.Error("Please wait $remainingSeconds seconds before requesting another email")
            }

            // Configure action code settings for better UX
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(VERIFICATION_URL)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(ANDROID_PACKAGE_NAME, true, null)
                .build()

            // Send verification email
            user.sendEmailVerification(actionCodeSettings).await()
            lastEmailSentTime = currentTime

            Log.d(TAG, "Email verification sent successfully to: ${user.email}")
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email verification", e)
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection and try again."
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    "Too many requests. Please try again later."
                else -> "Failed to send verification email. Please try again."
            }
            AuthResult.Error(errorMessage, e)
        }
    }

    /**
     * Checks the current user's email verification status
     * Reloads user data from Firebase to get the latest status
     * @return AuthResult with verification status
     */
    suspend fun checkEmailVerificationStatus(): AuthResult<Boolean> {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.Error("No authenticated user found")

            // Reload user to get fresh data from Firebase
            user.reload().await()

            val isVerified = user.isEmailVerified
            Log.d(TAG, "Email verification status checked: $isVerified for user: ${user.email}")

            AuthResult.Success(isVerified)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check email verification status", e)
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection."
                else -> "Failed to check verification status. Please try again."
            }
            AuthResult.Error(errorMessage, e)
        }
    }

    /**
     * Resends email verification with rate limiting
     * @return AuthResult indicating success or failure
     */
    suspend fun resendVerificationEmail(): AuthResult<Unit> {
        return sendEmailVerification() // Uses the same logic with built-in rate limiting
    }

    /**
     * Gets the remaining cooldown time in seconds
     * @return Remaining seconds until next email can be sent, 0 if ready
     */
    fun getRemainingCooldownSeconds(): Int {
        val currentTime = System.currentTimeMillis()
        val elapsed = (currentTime - lastEmailSentTime) / 1000
        val remaining = RESEND_COOLDOWN_SECONDS - elapsed.toInt()
        return maxOf(0, remaining)
    }

    /**
     * Checks if email can be sent (no cooldown active)
     * @return true if email can be sent, false if in cooldown
     */
    fun canSendEmail(): Boolean {
        return getRemainingCooldownSeconds() == 0
    }

    /**
     * Resets the rate limiting (for testing purposes)
     */
    internal fun resetRateLimit() {
        lastEmailSentTime = 0
    }
}
