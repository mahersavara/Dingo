package io.sukhuat.dingo.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations with enhanced features
 */
interface AuthRepository {
    // Basic Authentication
    /**
     * Sign up a new user with email and password
     * @param email User's email
     * @param password User's password
     * @return Flow of AuthResult indicating success or failure
     */
    suspend fun signUpWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>>

    /**
     * Sign in a user with email and password
     * @param email User's email
     * @param password User's password
     * @return Flow of AuthResult indicating success or failure
     */
    suspend fun signInWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>>

    /**
     * Sign in a user with Google
     * @param idToken Google ID token
     * @return Flow of AuthResult indicating success or failure
     */
    suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<Boolean>>

    /**
     * Sign out the current user
     * @return AuthResult indicating success or failure
     */
    suspend fun signOut(): AuthResult<Boolean>

    // Email Verification
    /**
     * Send email verification to the current user
     * @return AuthResult indicating success or failure
     */
    suspend fun sendEmailVerification(): AuthResult<Unit>

    /**
     * Check if the current user's email is verified
     * @return AuthResult with verification status
     */
    suspend fun checkEmailVerificationStatus(): AuthResult<Boolean>

    /**
     * Resend email verification with rate limiting
     * @return AuthResult indicating success or failure
     */
    suspend fun resendVerificationEmail(): AuthResult<Unit>

    // Password Management
    /**
     * Send password reset email using Firebase default approach
     * @param email User's email address
     * @return AuthResult indicating success or failure
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>

    /**
     * Change password for authenticated user
     * @param currentPassword Current password for verification
     * @param newPassword New password
     * @return AuthResult indicating success or failure
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit>

    // Password Validation
    /**
     * Validate password strength in real-time
     * @param password Password to validate
     * @return PasswordStrength assessment
     */
    fun validatePasswordStrength(password: String): PasswordStrength

    // User State Management
    /**
     * Get current authentication state as a flow
     * @return Flow of AuthenticationState
     */
    fun getAuthState(): Flow<AuthenticationState>

    /**
     * Get current authenticated user
     * @return User object or null if not authenticated
     */
    fun getCurrentUser(): User?

    /**
     * Check if a user is currently authenticated
     * @return true if user is authenticated, false otherwise
     */
    fun isUserAuthenticated(): Boolean

    /**
     * Get the current user's ID
     * @return User ID or null if no user is authenticated
     */
    fun getCurrentUserId(): String?
}
