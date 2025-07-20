package io.sukhuat.dingo.domain.model

/**
 * Sealed class representing different types of profile-related errors
 */
sealed class ProfileError : Exception() {
    object NetworkUnavailable : ProfileError()
    object AuthenticationExpired : ProfileError()
    data class ValidationError(val field: String, override val message: String) : ProfileError()
    data class StorageError(val operation: String, override val cause: Throwable) : ProfileError()
    data class UnknownError(override val cause: Throwable) : ProfileError()

    // Enhanced error types for comprehensive error handling
    object ServerUnavailable : ProfileError()
    object RateLimitExceeded : ProfileError()
    data class QuotaExceeded(val quotaType: String) : ProfileError()
    data class PermissionDenied(val resource: String) : ProfileError()
    data class DataCorruption(val dataType: String) : ProfileError()
    data class CacheError(val operation: String) : ProfileError()
    object OfflineMode : ProfileError()
    data class SyncConflict(val conflictType: String) : ProfileError()
    data class ImageProcessingError(val reason: String) : ProfileError()
    data class ExportError(val stage: String) : ProfileError()
}

/**
 * Error actions for handling profile errors
 */
sealed class ErrorAction {
    object ShowRetry : ErrorAction()
    object RequireReauth : ErrorAction()
    data class ShowValidation(val message: String) : ErrorAction()
    object ShowStorageError : ErrorAction()
    object ShowGenericError : ErrorAction()

    // Enhanced error actions
    data class ShowRetryWithDelay(val delaySeconds: Int) : ErrorAction()
    object ShowOfflineMode : ErrorAction()
    data class ShowQuotaExceeded(val quotaType: String) : ErrorAction()
    object ShowPermissionError : ErrorAction()
    data class ShowSyncConflict(val conflictType: String) : ErrorAction()
    object ShowImageProcessingError : ErrorAction()
    data class ShowExportError(val stage: String) : ErrorAction()
    object ShowCacheError : ErrorAction()
}

/**
 * Error recovery strategies
 */
sealed class ErrorRecoveryStrategy {
    object Retry : ErrorRecoveryStrategy()
    data class RetryWithBackoff(val maxRetries: Int = 3, val baseDelayMs: Long = 1000) : ErrorRecoveryStrategy()
    object RequireReauth : ErrorRecoveryStrategy()
    object FallbackToCache : ErrorRecoveryStrategy()
    object ShowOfflineUI : ErrorRecoveryStrategy()
    object NoRecovery : ErrorRecoveryStrategy()
}

/**
 * Profile error handler for consistent error management with recovery strategies
 */
class ProfileErrorHandler {

    fun handleError(error: ProfileError): ErrorAction {
        return when (error) {
            is ProfileError.NetworkUnavailable -> ErrorAction.ShowRetry
            is ProfileError.AuthenticationExpired -> ErrorAction.RequireReauth
            is ProfileError.ValidationError -> ErrorAction.ShowValidation(error.message)
            is ProfileError.StorageError -> ErrorAction.ShowStorageError
            is ProfileError.UnknownError -> ErrorAction.ShowGenericError
            is ProfileError.ServerUnavailable -> ErrorAction.ShowRetryWithDelay(30)
            is ProfileError.RateLimitExceeded -> ErrorAction.ShowRetryWithDelay(60)
            is ProfileError.QuotaExceeded -> ErrorAction.ShowQuotaExceeded(error.quotaType)
            is ProfileError.PermissionDenied -> ErrorAction.ShowPermissionError
            is ProfileError.DataCorruption -> ErrorAction.ShowGenericError
            is ProfileError.CacheError -> ErrorAction.ShowCacheError
            is ProfileError.OfflineMode -> ErrorAction.ShowOfflineMode
            is ProfileError.SyncConflict -> ErrorAction.ShowSyncConflict(error.conflictType)
            is ProfileError.ImageProcessingError -> ErrorAction.ShowImageProcessingError
            is ProfileError.ExportError -> ErrorAction.ShowExportError(error.stage)
        }
    }

    fun getRecoveryStrategy(error: ProfileError): ErrorRecoveryStrategy {
        return when (error) {
            is ProfileError.NetworkUnavailable -> ErrorRecoveryStrategy.RetryWithBackoff()
            is ProfileError.AuthenticationExpired -> ErrorRecoveryStrategy.RequireReauth
            is ProfileError.ValidationError -> ErrorRecoveryStrategy.NoRecovery
            is ProfileError.StorageError -> ErrorRecoveryStrategy.RetryWithBackoff(maxRetries = 2)
            is ProfileError.ServerUnavailable -> ErrorRecoveryStrategy.RetryWithBackoff(maxRetries = 5, baseDelayMs = 5000)
            is ProfileError.RateLimitExceeded -> ErrorRecoveryStrategy.RetryWithBackoff(maxRetries = 3, baseDelayMs = 10000)
            is ProfileError.QuotaExceeded -> ErrorRecoveryStrategy.NoRecovery
            is ProfileError.PermissionDenied -> ErrorRecoveryStrategy.RequireReauth
            is ProfileError.DataCorruption -> ErrorRecoveryStrategy.FallbackToCache
            is ProfileError.CacheError -> ErrorRecoveryStrategy.Retry
            is ProfileError.OfflineMode -> ErrorRecoveryStrategy.ShowOfflineUI
            is ProfileError.SyncConflict -> ErrorRecoveryStrategy.FallbackToCache
            is ProfileError.ImageProcessingError -> ErrorRecoveryStrategy.Retry
            is ProfileError.ExportError -> ErrorRecoveryStrategy.RetryWithBackoff()
            is ProfileError.UnknownError -> ErrorRecoveryStrategy.RetryWithBackoff()
        }
    }

    fun getErrorMessage(error: ProfileError): String {
        return when (error) {
            is ProfileError.NetworkUnavailable -> "No internet connection. Please check your network and try again."
            is ProfileError.AuthenticationExpired -> "Your session has expired. Please sign in again."
            is ProfileError.ValidationError -> error.message
            is ProfileError.StorageError -> "Failed to ${error.operation} file. Please try again."
            is ProfileError.ServerUnavailable -> "Server is temporarily unavailable. Please try again later."
            is ProfileError.RateLimitExceeded -> "Too many requests. Please wait a moment and try again."
            is ProfileError.QuotaExceeded -> "Storage quota exceeded for ${error.quotaType}. Please free up space."
            is ProfileError.PermissionDenied -> "Permission denied for ${error.resource}. Please check your account permissions."
            is ProfileError.DataCorruption -> "Data corruption detected in ${error.dataType}. Using cached version."
            is ProfileError.CacheError -> "Cache operation failed: ${error.operation}. Data may not be up to date."
            is ProfileError.OfflineMode -> "You're currently offline. Some features may be limited."
            is ProfileError.SyncConflict -> "Data sync conflict detected: ${error.conflictType}. Please refresh and try again."
            is ProfileError.ImageProcessingError -> "Image processing failed: ${error.reason}. Please try a different image."
            is ProfileError.ExportError -> "Export failed at ${error.stage}. Please try again."
            is ProfileError.UnknownError -> "An unexpected error occurred. Please try again."
        }
    }

    fun isRetryable(error: ProfileError): Boolean {
        return when (error) {
            is ProfileError.NetworkUnavailable,
            is ProfileError.StorageError,
            is ProfileError.ServerUnavailable,
            is ProfileError.RateLimitExceeded,
            is ProfileError.CacheError,
            is ProfileError.ImageProcessingError,
            is ProfileError.ExportError,
            is ProfileError.UnknownError -> true

            is ProfileError.AuthenticationExpired,
            is ProfileError.ValidationError,
            is ProfileError.QuotaExceeded,
            is ProfileError.PermissionDenied,
            is ProfileError.DataCorruption,
            is ProfileError.OfflineMode,
            is ProfileError.SyncConflict -> false
        }
    }
}
