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
        // Basic validation
        if (email.isBlank()) {
            return flow { emit(AuthResult.Error("Email cannot be empty")) }
        }
        if (password.isBlank()) {
            return flow { emit(AuthResult.Error("Password cannot be empty")) }
        }
        if (confirmPassword.isBlank()) {
            return flow { emit(AuthResult.Error("Please confirm your password")) }
        }
        if (password != confirmPassword) {
            return flow { emit(AuthResult.Error("Passwords don't match")) }
        }

        // Enhanced password strength validation
        val passwordStrength = repository.validatePasswordStrength(password)
        if (!passwordStrength.isValid) {
            val feedback = passwordStrength.feedback.firstOrNull() ?: "Password is too weak"
            return flow { emit(AuthResult.Error("Password requirements not met: $feedback")) }
        }

        return repository.signUpWithEmailPassword(email, password)
    }
}
