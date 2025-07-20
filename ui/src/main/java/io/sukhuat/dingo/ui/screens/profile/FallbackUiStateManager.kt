package io.sukhuat.dingo.ui.screens.profile

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for creating fallback UI states when data loading fails
 */
@Singleton
class FallbackUiStateManager @Inject constructor() {

    /**
     * Create a fallback profile for offline mode or when profile loading fails
     */
    fun createFallbackProfile(userId: String? = null): UserProfile {
        return UserProfile(
            userId = userId ?: "offline_user",
            displayName = "User",
            email = "user@example.com",
            profileImageUrl = null,
            joinDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
            isEmailVerified = false,
            authProvider = io.sukhuat.dingo.domain.model.AuthProvider.EMAIL_PASSWORD,
            lastLoginDate = System.currentTimeMillis()
        )
    }

    /**
     * Create fallback statistics when statistics loading fails
     */
    fun createFallbackStatistics(): ProfileStatistics {
        return ProfileStatistics(
            totalGoalsCreated = 0,
            completedGoals = 0,
            completionRate = 0f,
            currentStreak = 0,
            longestStreak = 0,
            monthlyStats = emptyMap(),
            achievements = emptyList()
        )
    }

    /**
     * Create a fallback success state for offline mode
     */
    fun createOfflineFallbackState(
        cachedProfile: UserProfile? = null,
        cachedStatistics: ProfileStatistics? = null
    ): ProfileUiState.Success {
        return ProfileUiState.Success(
            profile = cachedProfile ?: createFallbackProfile(),
            statistics = cachedStatistics ?: createFallbackStatistics(),
            isRefreshing = false,
            isOfflineMode = true
        )
    }

    /**
     * Create error state with appropriate message and retry options
     */
    fun createErrorState(error: ProfileError): ProfileUiState.Error {
        val message = when (error) {
            is ProfileError.NetworkUnavailable -> "No internet connection. Please check your network and try again."
            is ProfileError.AuthenticationExpired -> "Your session has expired. Please sign in again."
            is ProfileError.ServerUnavailable -> "Server is temporarily unavailable. Please try again in a few minutes."
            is ProfileError.RateLimitExceeded -> "Too many requests. Please wait a moment before trying again."
            is ProfileError.QuotaExceeded -> "Storage limit reached. Please free up space or upgrade your account."
            is ProfileError.PermissionDenied -> "Access denied. Please check your account permissions."
            is ProfileError.DataCorruption -> "Data corruption detected. Please refresh or contact support."
            is ProfileError.OfflineMode -> "You're currently offline. Some features may be limited."
            is ProfileError.ValidationError -> error.message
            else -> "An unexpected error occurred. Please try again."
        }

        return ProfileUiState.Error(
            message = message,
            isRetryable = isRetryableError(error),
            errorType = error::class.simpleName ?: "UnknownError"
        )
    }

    /**
     * Check if an error is retryable
     */
    private fun isRetryableError(error: ProfileError): Boolean {
        return when (error) {
            is ProfileError.NetworkUnavailable,
            is ProfileError.ServerUnavailable,
            is ProfileError.RateLimitExceeded,
            is ProfileError.StorageError,
            is ProfileError.CacheError,
            is ProfileError.UnknownError -> true

            is ProfileError.AuthenticationExpired,
            is ProfileError.ValidationError,
            is ProfileError.QuotaExceeded,
            is ProfileError.PermissionDenied,
            is ProfileError.DataCorruption,
            is ProfileError.OfflineMode -> false

            else -> false
        }
    }

    /**
     * Create loading state with appropriate message
     */
    fun createLoadingState(message: String = "Loading profile..."): ProfileUiState.Loading {
        return ProfileUiState.Loading(message)
    }

    /**
     * Create partial loading state when some data is available
     */
    fun createPartialLoadingState(
        profile: UserProfile,
        statistics: ProfileStatistics? = null,
        loadingMessage: String = "Updating..."
    ): ProfileUiState.Success {
        return ProfileUiState.Success(
            profile = profile,
            statistics = statistics ?: createFallbackStatistics(),
            isRefreshing = true,
            isOfflineMode = statistics == null
        )
    }
}
