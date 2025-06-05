package io.sukhuat.dingo.data.repository.auth

import io.sukhuat.dingo.data.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUpWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>>
    suspend fun signInWithEmailPassword(email: String, password: String): Flow<AuthResult<Boolean>>
    suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<Boolean>>
    suspend fun signOut(): AuthResult<Boolean>
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}
