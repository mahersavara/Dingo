package io.sukhuat.dingo.domain.usecase.preferences

import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user preferences
 */
class GetUserPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> {
        return userPreferencesRepository.getUserPreferences()
    }
}
