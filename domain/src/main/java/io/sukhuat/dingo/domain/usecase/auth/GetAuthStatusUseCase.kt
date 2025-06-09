package io.sukhuat.dingo.domain.usecase.auth

import io.sukhuat.dingo.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case that checks if a user is currently authenticated
 */
class GetAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Checks if the user is currently authenticated
     * @return true if user is authenticated, false otherwise
     */
    operator fun invoke(): Boolean {
        return authRepository.isUserAuthenticated()
    }
}
