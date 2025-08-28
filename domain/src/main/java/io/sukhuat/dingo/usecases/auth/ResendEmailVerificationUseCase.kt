package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import javax.inject.Inject

/**
 * Use case for resending email verification with rate limiting
 */
class ResendEmailVerificationUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<Unit> {
        return repository.resendVerificationEmail()
    }
}
