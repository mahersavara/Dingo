package io.sukhuat.dingo.domain.usecase.auth

import javax.inject.Inject

/**
 * Use case for signing out the user
 */
class SignOutUseCase @Inject constructor() {
    suspend operator fun invoke() {
        // In a real app, this would handle authentication logout
        // For now, it's just a placeholder
    }
}