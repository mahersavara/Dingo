package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.domain.repository.PasswordStrength
import javax.inject.Inject

/**
 * Use case for real-time password strength validation
 */
class ValidatePasswordStrengthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(password: String): PasswordStrength {
        return repository.validatePasswordStrength(password)
    }
}
