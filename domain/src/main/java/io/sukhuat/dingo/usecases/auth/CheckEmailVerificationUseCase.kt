package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.AuthResult
import javax.inject.Inject

/**
 * Use case for checking email verification status
 */
class CheckEmailVerificationUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<Boolean> {
        return repository.checkEmailVerificationStatus()
    }
}
