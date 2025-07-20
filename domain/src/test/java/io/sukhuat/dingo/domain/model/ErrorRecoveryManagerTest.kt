package io.sukhuat.dingo.domain.model

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for ErrorRecoveryManager
 */
class ErrorRecoveryManagerTest {

    private lateinit var profileErrorHandler: ProfileErrorHandler
    private lateinit var errorRecoveryManager: ErrorRecoveryManager

    @Before
    fun setup() {
        profileErrorHandler = ProfileErrorHandler()
        errorRecoveryManager = ErrorRecoveryManager(profileErrorHandler)
    }

    @Test
    fun `mapToProfileError should map UnknownHostException to NetworkUnavailable`() {
        val exception = UnknownHostException("Host not found")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.NetworkUnavailable)
    }

    @Test
    fun `mapToProfileError should map SocketTimeoutException to ServerUnavailable`() {
        val exception = SocketTimeoutException("Connection timeout")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.ServerUnavailable)
    }

    @Test
    fun `mapToProfileError should map IOException to NetworkUnavailable`() {
        val exception = IOException("Network error")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.NetworkUnavailable)
    }

    @Test
    fun `mapToProfileError should map SecurityException to PermissionDenied`() {
        val exception = SecurityException("Access denied")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.PermissionDenied)
        assertEquals("system", (result as ProfileError.PermissionDenied).resource)
    }

    @Test
    fun `mapToProfileError should map IllegalArgumentException to ValidationError`() {
        val exception = IllegalArgumentException("Invalid input")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.ValidationError)
        assertEquals("input", (result as ProfileError.ValidationError).field)
        assertEquals("Invalid input", result.message)
    }

    @Test
    fun `mapToProfileError should map OutOfMemoryError to QuotaExceeded`() {
        val exception = OutOfMemoryError("Out of memory")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.QuotaExceeded)
        assertEquals("memory", (result as ProfileError.QuotaExceeded).quotaType)
    }

    @Test
    fun `mapToProfileError should handle Firebase PERMISSION_DENIED error`() {
        val exception = RuntimeException("PERMISSION_DENIED: Access denied")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.PermissionDenied)
        assertEquals("firebase", (result as ProfileError.PermissionDenied).resource)
    }

    @Test
    fun `mapToProfileError should handle Firebase QUOTA_EXCEEDED error`() {
        val exception = RuntimeException("QUOTA_EXCEEDED: Storage quota exceeded")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.QuotaExceeded)
        assertEquals("firebase_storage", (result as ProfileError.QuotaExceeded).quotaType)
    }

    @Test
    fun `mapToProfileError should handle Firebase UNAUTHENTICATED error`() {
        val exception = RuntimeException("UNAUTHENTICATED: User not authenticated")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.AuthenticationExpired)
    }

    @Test
    fun `mapToProfileError should handle Firebase UNAVAILABLE error`() {
        val exception = RuntimeException("UNAVAILABLE: Service unavailable")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.ServerUnavailable)
    }

    @Test
    fun `mapToProfileError should handle Firebase RESOURCE_EXHAUSTED error`() {
        val exception = RuntimeException("RESOURCE_EXHAUSTED: Rate limit exceeded")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.RateLimitExceeded)
    }

    @Test
    fun `mapToProfileError should handle Firebase DATA_LOSS error`() {
        val exception = RuntimeException("DATA_LOSS: Data corruption detected")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.DataCorruption)
        assertEquals("firebase_data", (result as ProfileError.DataCorruption).dataType)
    }

    @Test
    fun `mapToProfileError should return ProfileError as-is`() {
        val originalError = ProfileError.NetworkUnavailable
        val result = errorRecoveryManager.mapToProfileError(originalError)
        assertSame(originalError, result)
    }

    @Test
    fun `mapToProfileError should map unknown exceptions to UnknownError`() {
        val exception = RuntimeException("Some unknown error")
        val result = errorRecoveryManager.mapToProfileError(exception)
        assertTrue(result is ProfileError.UnknownError)
        assertSame(exception, (result as ProfileError.UnknownError).cause)
    }

    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            "success"
        }

        val result = errorRecoveryManager.executeWithRetry(operation)

        assertEquals("success", result)
        assertEquals(1, callCount)
    }

    @Test
    fun `executeWithRetry should retry on retryable errors`() = runTest {
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            if (callCount < 3) {
                throw IOException("Network error")
            }
            "success"
        }

        val result = errorRecoveryManager.executeWithRetry(operation, maxRetries = 3)

        assertEquals("success", result)
        assertEquals(3, callCount)
    }

    @Test
    fun `executeWithRetry should not retry on non-retryable errors`() = runTest {
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            throw IllegalArgumentException("Invalid input")
        }

        try {
            errorRecoveryManager.executeWithRetry(operation, maxRetries = 3)
            fail("Expected ProfileError.ValidationError to be thrown")
        } catch (e: ProfileError.ValidationError) {
            assertEquals(1, callCount)
            assertEquals("input", e.field)
        }
    }

    @Test
    fun `executeWithRetry should throw after max retries exceeded`() = runTest {
        var callCount = 0
        val operation: suspend () -> String = {
            callCount++
            throw IOException("Network error")
        }

        try {
            errorRecoveryManager.executeWithRetry(operation, maxRetries = 2)
            fail("Expected ProfileError.NetworkUnavailable to be thrown")
        } catch (e: ProfileError.NetworkUnavailable) {
            assertEquals(3, callCount) // Initial attempt + 2 retries
        }
    }

    @Test
    fun `resolveSyncConflict should prefer remote data for profile updates`() {
        val localData = "local_profile"
        val remoteData = "remote_profile"

        val result = errorRecoveryManager.resolveSyncConflict("profile_update", localData, remoteData)

        assertEquals(remoteData, result)
    }

    @Test
    fun `resolveSyncConflict should prefer local data for preferences`() {
        val localData = "local_preferences"
        val remoteData = "remote_preferences"

        val result = errorRecoveryManager.resolveSyncConflict("preferences", localData, remoteData)

        assertEquals(localData, result)
    }

    @Test
    fun `resolveSyncConflict should default to remote data for unknown conflict types`() {
        val localData = "local_data"
        val remoteData = "remote_data"

        val result = errorRecoveryManager.resolveSyncConflict("unknown_type", localData, remoteData)

        assertEquals(remoteData, result)
    }
}
