package io.sukhuat.dingo.domain.usecase.account

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChangePasswordUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        changePasswordUseCase = ChangePasswordUseCase(userProfileRepository)
    }

    @Test
    fun `changePassword should call repository with valid passwords`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "NewPassword123!"
        val confirmPassword = "NewPassword123!"
        coEvery { userProfileRepository.changePassword(currentPassword, newPassword) } returns Unit

        // When
        changePasswordUseCase(currentPassword, newPassword, confirmPassword)

        // Then
        coVerify { userProfileRepository.changePassword(currentPassword, newPassword) }
    }

    @Test
    fun `changePassword should throw ValidationError for empty current password`() = runTest {
        // Given
        val currentPassword = ""
        val newPassword = "NewPassword123!"
        val confirmPassword = "NewPassword123!"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals("currentPassword", exception.field)
        assertEquals("Current password is required", exception.message)
    }

    @Test
    fun `changePassword should throw ValidationError for empty new password`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = ""
        val confirmPassword = ""

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals("newPassword", exception.field)
        assertEquals("New password is required", exception.message)
    }

    @Test
    fun `changePassword should throw ValidationError for weak new password`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "weak"
        val confirmPassword = "weak"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals("newPassword", exception.field)
        assertEquals("Password must be at least 8 characters long", exception.message)
    }

    @Test
    fun `changePassword should throw ValidationError for same passwords`() = runTest {
        // Given
        val currentPassword = "SamePassword123!"
        val newPassword = "SamePassword123!"
        val confirmPassword = "SamePassword123!"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals("newPassword", exception.field)
        assertEquals("New password must be different from current password", exception.message)
    }

    @Test
    fun `changePassword should throw ValidationError for mismatched passwords`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "NewPassword123!"
        val confirmPassword = "DifferentPassword123!"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals("confirmPassword", exception.field)
        assertEquals("Passwords do not match", exception.message)
    }

    @Test
    fun `changePassword should propagate repository exceptions`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "NewPassword123!"
        val confirmPassword = "NewPassword123!"
        val repositoryException = ProfileError.AuthenticationExpired
        coEvery { userProfileRepository.changePassword(currentPassword, newPassword) } throws repositoryException

        // When & Then
        val exception = assertFailsWith<ProfileError.AuthenticationExpired> {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword)
        }
        assertEquals(ProfileError.AuthenticationExpired, exception)
    }
}
