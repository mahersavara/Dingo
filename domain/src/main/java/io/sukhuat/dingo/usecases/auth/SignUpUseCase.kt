package io.sukhuat.dingo.usecases.auth

import io.sukhuat.dingo.data.model.AuthResult
import io.sukhuat.dingo.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Flow<AuthResult<Boolean>> {
        return repository.signUpWithEmailPassword(email, password)
    }
}
