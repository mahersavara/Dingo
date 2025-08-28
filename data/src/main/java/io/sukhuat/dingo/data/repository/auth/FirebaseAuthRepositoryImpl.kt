package io.sukhuat.dingo.data.repository.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.sukhuat.dingo.common.Constants
import io.sukhuat.dingo.data.auth.EmailVerificationManager
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.data.auth.PasswordResetManager
import io.sukhuat.dingo.domain.repository.AuthAction
import io.sukhuat.dingo.domain.repository.AuthMethod
import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import io.sukhuat.dingo.domain.repository.AuthenticationState
import io.sukhuat.dingo.domain.repository.PasswordStrength
import io.sukhuat.dingo.domain.repository.User
import io.sukhuat.dingo.domain.util.PasswordStrengthValidator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleAuthService: GoogleAuthService,
    private val emailVerificationManager: EmailVerificationManager,
    private val passwordResetManager: PasswordResetManager
) : AuthRepository {

    init {
        googleAuthService.initialize(Constants.GOOGLE_WEB_CLIENT_ID)
    }

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading)
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            // Automatically send email verification after successful signup
            emailVerificationManager.sendEmailVerification()

            emit(AuthResult.Success(true))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An unknown error occurred", e))
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading)
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // Reload user to get the latest email verification status
                user.reload().await()

                if (user.isEmailVerified) {
                    emit(AuthResult.Success(true))
                } else {
                    // Sign out the user since email is not verified
                    firebaseAuth.signOut()
                    emit(AuthResult.Error("Please verify your email address before signing in. Check your inbox for a verification email."))
                }
            } else {
                emit(AuthResult.Error("Authentication failed"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An unknown error occurred", e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                // Reload user to get the latest email verification status
                user.reload().await()

                // Google accounts are typically verified by default, but let's check anyway
                if (user.isEmailVerified) {
                    emit(AuthResult.Success(true))
                } else {
                    // For Google accounts that are somehow not verified, sign them out
                    firebaseAuth.signOut()
                    emit(AuthResult.Error("Please verify your email address before signing in."))
                }
            } else {
                emit(AuthResult.Error("Google authentication failed"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Google sign in failed", e))
        }
    }

    override suspend fun signOut(): AuthResult<Boolean> {
        return try {
            if (googleAuthService.getLastSignedInAccount() != null) {
                googleAuthService.signOut()
            }
            firebaseAuth.signOut()
            AuthResult.Success(true)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed", e)
        }
    }

    // Email Verification Methods
    override suspend fun sendEmailVerification(): AuthResult<Unit> {
        return emailVerificationManager.sendEmailVerification()
    }

    override suspend fun checkEmailVerificationStatus(): AuthResult<Boolean> {
        return emailVerificationManager.checkEmailVerificationStatus()
    }

    override suspend fun resendVerificationEmail(): AuthResult<Unit> {
        return emailVerificationManager.resendVerificationEmail()
    }

    // Password Management Methods
    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return passwordResetManager.sendPasswordResetEmail(email)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.Error("No authenticated user found")

            val email = user.email ?: return AuthResult.Error("User email not available")

            // Re-authenticate user with current password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update to new password
            user.updatePassword(newPassword).await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("wrong-password", ignoreCase = true) == true ->
                    "Current password is incorrect"
                e.message?.contains("weak-password", ignoreCase = true) == true ->
                    "New password is too weak"
                e.message?.contains("requires-recent-login", ignoreCase = true) == true ->
                    "Please sign in again to change your password"
                else -> "Failed to change password. Please try again."
            }
            AuthResult.Error(errorMessage, e)
        }
    }

    // Password Validation
    override fun validatePasswordStrength(password: String): PasswordStrength {
        return PasswordStrengthValidator.validatePassword(password)
    }

    // User State Management
    override fun getAuthState(): Flow<AuthenticationState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            val authState = if (user != null) {
                AuthenticationState(
                    isAuthenticated = true,
                    isEmailVerified = user.isEmailVerified,
                    user = User(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl?.toString(),
                        isEmailVerified = user.isEmailVerified,
                        createdAt = user.metadata?.creationTimestamp ?: System.currentTimeMillis(),
                        lastSignInAt = user.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
                    ),
                    authMethod = when {
                        user.providerData.any { it.providerId == "google.com" } -> AuthMethod.GOOGLE
                        user.providerData.any { it.providerId == "password" } -> AuthMethod.EMAIL_PASSWORD
                        else -> AuthMethod.NONE
                    },
                    requiresAction = when {
                        !user.isEmailVerified && user.providerData.any { it.providerId == "password" } -> AuthAction.VERIFY_EMAIL
                        else -> null
                    }
                )
            } else {
                AuthenticationState()
            }
            trySend(authState)
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString(),
            isEmailVerified = firebaseUser.isEmailVerified,
            createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastSignInAt = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
