package io.sukhuat.dingo.domain.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * Manager for handling error recovery with various strategies
 */
@Singleton
class ErrorRecoveryManager @Inject constructor(
    private val profileErrorHandler: ProfileErrorHandler
) {

    /**
     * Execute an operation with automatic retry based on error type
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = 3,
        baseDelayMs: Long = 1000
    ): T {
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                val profileError = mapToProfileError(e)

                if (!profileErrorHandler.isRetryable(profileError) || attempt == maxRetries) {
                    throw profileError
                }

                // Calculate exponential backoff delay
                val delayMs = calculateBackoffDelay(attempt, baseDelayMs)
                delay(delayMs)
            }
        }

        throw lastException ?: ProfileError.UnknownError(RuntimeException("Unknown error"))
    }

    /**
     * Create a Flow with retry logic based on error recovery strategies
     */
    fun <T> flowWithRetry(
        flowFactory: () -> Flow<T>,
        maxRetries: Int = 3,
        baseDelayMs: Long = 1000
    ): Flow<T> = flow {
        flowFactory()
            .retryWhen { cause, attempt ->
                val profileError = mapToProfileError(cause)
                val strategy = profileErrorHandler.getRecoveryStrategy(profileError)

                when (strategy) {
                    is ErrorRecoveryStrategy.RetryWithBackoff -> {
                        if (attempt < strategy.maxRetries) {
                            val delayMs = calculateBackoffDelay(attempt.toInt(), strategy.baseDelayMs)
                            delay(delayMs)
                            true
                        } else {
                            false
                        }
                    }
                    is ErrorRecoveryStrategy.Retry -> {
                        if (attempt < maxRetries) {
                            delay(baseDelayMs)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
            .catch { cause ->
                throw mapToProfileError(cause)
            }
            .collect { emit(it) }
    }

    /**
     * Map generic exceptions to ProfileError types
     */
    fun mapToProfileError(throwable: Throwable): ProfileError {
        return when (throwable) {
            is ProfileError -> throwable
            is SocketTimeoutException -> ProfileError.ServerUnavailable
            is UnknownHostException, is IOException -> ProfileError.NetworkUnavailable
            is SecurityException -> ProfileError.PermissionDenied("system")
            is IllegalArgumentException -> ProfileError.ValidationError("input", throwable.message ?: "Invalid input")
            is OutOfMemoryError -> ProfileError.QuotaExceeded("memory")
            else -> {
                // Check for Firebase-specific errors
                when {
                    throwable.message?.contains("PERMISSION_DENIED") == true -> ProfileError.PermissionDenied("firebase")
                    throwable.message?.contains("QUOTA_EXCEEDED") == true -> ProfileError.QuotaExceeded("firebase_storage")
                    throwable.message?.contains("UNAUTHENTICATED") == true -> ProfileError.AuthenticationExpired
                    throwable.message?.contains("UNAVAILABLE") == true -> ProfileError.ServerUnavailable
                    throwable.message?.contains("RESOURCE_EXHAUSTED") == true -> ProfileError.RateLimitExceeded
                    throwable.message?.contains("DATA_LOSS") == true -> ProfileError.DataCorruption("firebase_data")
                    else -> ProfileError.UnknownError(throwable)
                }
            }
        }
    }

    /**
     * Calculate exponential backoff delay with jitter
     */
    private fun calculateBackoffDelay(attempt: Int, baseDelayMs: Long): Long {
        val exponentialDelay = baseDelayMs * (2.0.pow(attempt.toDouble())).toLong()
        val maxDelay = 30000L // 30 seconds max
        val delayWithCap = min(exponentialDelay, maxDelay)

        // Add jitter to prevent thundering herd
        val jitter = (delayWithCap * 0.1 * Math.random()).toLong()
        return delayWithCap + jitter
    }

    /**
     * Check if device is in offline mode
     */
    fun isOfflineMode(): Boolean {
        // In a real implementation, this would check network connectivity
        // For now, we'll return false as a placeholder
        return false
    }

    /**
     * Handle sync conflicts by providing resolution strategies
     */
    fun resolveSyncConflict(conflictType: String, localData: Any?, remoteData: Any?): Any? {
        return when (conflictType) {
            "profile_update" -> {
                // Use remote data as it's likely more recent
                remoteData
            }
            "preferences" -> {
                // Merge preferences, preferring local changes
                localData
            }
            else -> {
                // Default to remote data
                remoteData
            }
        }
    }
}

/**
 * Network connectivity checker interface
 */
interface NetworkConnectivityChecker {
    fun isConnected(): Boolean
    fun isWifiConnected(): Boolean
    fun isMobileConnected(): Boolean
}

/**
 * Cache manager for offline support
 */
interface ProfileCacheManager {
    suspend fun cacheProfile(userId: String, profile: UserProfile)
    suspend fun getCachedProfile(userId: String): UserProfile?
    suspend fun cacheStatistics(userId: String, statistics: ProfileStatistics)
    suspend fun getCachedStatistics(userId: String): ProfileStatistics?
    suspend fun invalidateCache(userId: String)
    suspend fun clearExpiredCache()
}
