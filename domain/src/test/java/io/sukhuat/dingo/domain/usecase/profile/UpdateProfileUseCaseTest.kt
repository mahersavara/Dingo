package io.sukhuat.dingo.domain.usecase.profile

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

class UpdateProfileUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var updateProfileUseCase: UpdateProfileUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        updateProfileUseCase = UpdateProfileUseCase(userProfileRepository)
    }

    @Test
    fun `updateDisplayName should call repository with valid name`() = runTest {
        // Given
        val validName = "Valid Name"
        coEvery { userProfileRepository.updateDisplayName(validName) } returns Unit

        // When
        updateProfileUseCase.updateDisplayName(validName)

        // Then
        coVerify { userProfileRepository.updateDisplayName(validName) }
    }

    @Test
    fun `updateDisplayName should throw ValidationError for empty name`() = runTest {
        // Given
        val emptyName = ""

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            updateProfileUseCase.updateDisplayName(emptyName)
        }
        assertEquals("displayName", exception.field)
        assertEquals("Display name cannot be empty", exception.message)
    }

    @Test
    fun `updateDisplayName should throw ValidationError for blank name`() = runTest {
        // Given
        val blankName = "   "

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            updateProfileUseCase.updateDisplayName(blankName)
        }
        assertEquals("displayName", exception.field)
        assertEquals("Display name cannot be empty", exception.message)
    }

    @Test
    fun `updateDisplayName should throw ValidationError for name too short`() = runTest {
        // Given
        val shortName = "A"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            updateProfileUseCase.updateDisplayName(shortName)
        }
        assertEquals("displayName", exception.field)
        assertEquals("Display name must be at least 2 characters", exception.message)
    }

    @Test
    fun `updateDisplayName should throw ValidationError for name too long`() = runTest {
        // Given
        val longName = "A".repeat(51)

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            updateProfileUseCase.updateDisplayName(longName)
        }
        assertEquals("displayName", exception.field)
        assertEquals("Display name cannot exceed 50 characters", exception.message)
    }

    @Test
    fun `updateDisplayName should throw ValidationError for invalid characters`() = runTest {
        // Given
        val invalidName = "Test@Name#"

        // When & Then
        val exception = assertFailsWith<ProfileError.ValidationError> {
            updateProfileUseCase.updateDisplayName(invalidName)
        }
        assertEquals("displayName", exception.field)
        assertEquals("Display name contains invalid characters", exception.message)
    }

    @Test
    fun `updateDisplayName should accept valid names with allowed characters`() = runTest {
        // Given
        val validNames = listOf(
            "John Doe",
            "User123",
            "Test_User",
            "User-Name",
            "User.Name"
        )

        // When & Then
        validNames.forEach { name ->
            updateProfileUseCase.updateDisplayName(name)
            coVerify { userProfileRepository.updateDisplayName(name) }
        }
    }

    @Test
    fun `updateDisplayName should propagate repository exceptions`() = runTest {
        // Given
        val validName = "Valid Name"
        val repositoryException = RuntimeException("Repository error")
        coEvery { userProfileRepository.updateDisplayName(validName) } throws repositoryException

        // When & Then
        val exception = assertFailsWith<RuntimeException> {
            updateProfileUseCase.updateDisplayName(validName)
        }
        assertEquals("Repository error", exception.message)
    }
}
