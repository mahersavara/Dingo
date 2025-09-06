package io.sukhuat.dingo.domain.usecase.account

import io.mockk.*
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import io.sukhuat.dingo.domain.usecase.profile.ChangePasswordUseCase
import io.sukhuat.dingo.domain.validation.ProfileValidator
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangePasswordUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var profileValidator: ProfileValidator
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        profileValidator = mockk(relaxed = true)
        changePasswordUseCase = ChangePasswordUseCase(userProfileRepository, profileValidator)
    }

    @Test
    fun `changePassword should return Success with valid passwords`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "NewPassword123!"
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Valid
        every { profileValidator.getPasswordStrength(newPassword) } returns 3
        coEvery { userProfileRepository.changePassword(currentPassword, newPassword) } returns Unit

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.Success)
        coVerify { userProfileRepository.changePassword(currentPassword, newPassword) }
    }

    @Test
    fun `changePassword should return ValidationError for empty current password`() = runTest {
        // Given
        val currentPassword = ""
        val newPassword = "NewPassword123!"

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.ValidationError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.ValidationError
        assertEquals("currentPassword", error.field)
        assertEquals("Current password is required", error.message)
    }

    @Test
    fun `changePassword should return ValidationError for invalid new password`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "weak"
        val validationError = ProfileError.ValidationError("newPassword", "Password must be at least 8 characters long")
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Invalid(validationError)

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.ValidationError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.ValidationError
        assertEquals("newPassword", error.field)
        assertEquals("Password must be at least 8 characters long", error.message)
    }

    @Test
    fun `changePassword should return ValidationError for same passwords`() = runTest {
        // Given
        val currentPassword = "SamePassword123!"
        val newPassword = "SamePassword123!"
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Valid

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.ValidationError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.ValidationError
        assertEquals("newPassword", error.field)
        assertEquals("New password must be different from current password", error.message)
    }

    @Test
    fun `changePassword should return ValidationError for weak password strength`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "WeakPass1!"
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Valid
        every { profileValidator.getPasswordStrength(newPassword) } returns 1 // Weak strength

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.ValidationError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.ValidationError
        assertEquals("newPassword", error.field)
        assertEquals("Password is too weak. Please choose a stronger password.", error.message)
    }

    @Test
    fun `changePassword should return AuthError for expired authentication`() = runTest {
        // Given
        val currentPassword = "currentPassword123"
        val newPassword = "NewPassword123!"
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Valid
        every { profileValidator.getPasswordStrength(newPassword) } returns 3
        coEvery { userProfileRepository.changePassword(currentPassword, newPassword) } throws Exception("requires-recent-login")

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.AuthError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.AuthError
        assertEquals("Please sign out and sign in again to change your password", error.message)
    }

    @Test
    fun `changePassword should return ValidationError for incorrect current password`() = runTest {
        // Given
        val currentPassword = "wrongPassword"
        val newPassword = "NewPassword123!"
        every { profileValidator.validatePassword(newPassword) } returns ProfileValidator.ValidationResult.Valid
        every { profileValidator.getPasswordStrength(newPassword) } returns 3
        coEvery { userProfileRepository.changePassword(currentPassword, newPassword) } throws Exception("wrong-password")

        // When
        val result = changePasswordUseCase.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is ChangePasswordUseCase.PasswordChangeResult.ValidationError)
        val error = result as ChangePasswordUseCase.PasswordChangeResult.ValidationError
        assertEquals("currentPassword", error.field)
        assertEquals("Current password is incorrect", error.message)
    }
}