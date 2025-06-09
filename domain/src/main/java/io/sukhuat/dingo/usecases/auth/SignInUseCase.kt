package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Flow<AuthResult<Boolean>> {
        if (email.isBlank()) {
            return flow { emit(AuthResult.Error("Email cannot be empty")) }
        }
        if (password.isBlank()) {
            return flow { emit(AuthResult.Error("Password cannot be empty")) }
        }
        return repository.signInWithEmailPassword(email, password)
    }

    suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<Boolean>> {
        if (idToken.isBlank()) {
            return flow { emit(AuthResult.Error("Invalid Google ID token")) }
        }
        return repository.signInWithGoogle(idToken)
    }

    fun isUserAuthenticated(): Boolean = repository.isUserAuthenticated()

    fun getCurrentUserId(): String? = repository.getCurrentUserId()
}
