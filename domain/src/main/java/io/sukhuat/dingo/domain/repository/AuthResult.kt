package io.sukhuat.dingo.domain.repository

/**
 * Represents the result of an authentication operation
 * @param T The type of data contained in the result
 */
sealed class AuthResult<out T> {
    /**
     * Represents a successful authentication operation
     * @param data The data returned from the operation
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Represents a failed authentication operation
     * @param message Error message
     * @param exception Optional exception that caused the failure
     */
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()

    /**
     * Represents an authentication operation in progress
     */
    object Loading : AuthResult<Nothing>()
}
