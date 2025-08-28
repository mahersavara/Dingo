package io.sukhuat.dingo.usecases.auth

import android.util.Patterns
import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for sending password reset email with validation
 */
class SendPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Flow<AuthResult<Unit>> = flow {
        // Validate email format
        if (email.isBlank()) {
            emit(AuthResult.Error("Email cannot be empty"))
            return@flow
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(AuthResult.Error("Please enter a valid email address"))
            return@flow
        }

        emit(AuthResult.Loading)

        val result = repository.sendPasswordResetEmail(email)
        emit(result)
    }
}
