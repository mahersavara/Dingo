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

class DeleteAccountUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        deleteAccountUseCase = DeleteAccountUseCase(userProfileRepository)
    }

    @Test
    fun `invoke should call repository with correct confirmation`() = runTest {
        // Given
        val confirmationText = "DELETE"
        coEvery { userProfileRepository.deleteUserAccount() } returns Unit

        // When
        deleteAccountUseCase(confirmationText)

        // Then
        coVerify { userProfileRepository.deleteUserAccount() }
    }

    @Test
    fun `invoke should throw ValidationError for incorrect confirmation`() = runTest {
        // Given
        val incorrectConfirmation = "delete"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            deleteAccountUseCase(incorrectConfirmation)
        }
        assertEquals("confirmation", exception.field)
        assertEquals("Please type 'DELETE' to confirm account deletion", exception.message)
    }

    @Test
    fun `invoke should throw ValidationError for empty confirmation`() = runTest {
        // Given
        val emptyConfirmation = ""

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            deleteAccountUseCase(emptyConfirmation)
        }
        assertEquals("confirmation", exception.field)
        assertEquals("Please type 'DELETE' to confirm account deletion", exception.message)
    }

    @Test
    fun `invoke should throw ValidationError for blank confirmation`() = runTest {
        // Given
        val blankConfirmation = "   "

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            deleteAccountUseCase(blankConfirmation)
        }
        assertEquals("confirmation", exception.field)
        assertEquals("Please type 'DELETE' to confirm account deletion", exception.message)
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val confirmationText = "DELETE"
        val repositoryException = ProfileError.AuthenticationExpired
        coEvery { userProfileRepository.deleteUserAccount() } throws repositoryException

        // When & Then
        assertFailsWith<ProfileError.AuthenticationExpired> {
            deleteAccountUseCase(confirmationText)
        }
    }
}
