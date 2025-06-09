package io.sukhuat.dingo.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
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
