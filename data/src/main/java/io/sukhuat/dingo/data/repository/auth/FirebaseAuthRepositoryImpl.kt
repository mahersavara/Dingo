package io.sukhuat.dingo.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.sukhuat.dingo.common.Constants
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.data.model.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleAuthService: GoogleAuthService
) : AuthRepository {

    init {
        googleAuthService.initialize(Constants.GOOGLE_WEB_CLIENT_ID)
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading())
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            emit(AuthResult.Success(true))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading())
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(AuthResult.Success(true))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<Boolean>> = flow {
        try {
            emit(AuthResult.Loading())
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            emit(AuthResult.Success(true))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Google sign in failed"))
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
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
