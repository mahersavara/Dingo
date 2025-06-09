package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, confirmPassword: String): Flow<AuthResult<Boolean>> {
        if (email.isBlank()) {
            return flow { emit(AuthResult.Error("Email cannot be empty")) }
        }
        if (password.isBlank()) {
            return flow { emit(AuthResult.Error("Password cannot be empty")) }
        }
        if (password != confirmPassword) {
            return flow { emit(AuthResult.Error("Passwords don't match")) }
        }
        if (password.length < 6) {
            return flow { emit(AuthResult.Error("Password must be at least 6 characters")) }
        }

        return repository.signUpWithEmailPassword(email, password)
    }
}
