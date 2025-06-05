package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.data.model.AuthResult
import io.sukhuat.dingo.data.repository.auth.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<Boolean> {
        return repository.signOut()
    }
}
