package io.sukhuat.dingo.domain.usecase.profile

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ManageProfileImageUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var manageProfileImageUseCase: ManageProfileImageUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk(relaxed = true)
        manageProfileImageUseCase = ManageProfileImageUseCase(userProfileRepository)
    }

    @Test
    fun `uploadProfileImage should return image URL from repository`() = runTest {
        // Given
        val imageUri = mockk<Uri>()
        every { imageUri.toString() } returns "content://media/image/123"
        every { imageUri.scheme } returns "content"
        val expectedUrl = "https://example.com/profile-image.jpg"
        coEvery { userProfileRepository.updateProfileImage(imageUri) } returns expectedUrl

        // When
        val result = manageProfileImageUseCase.uploadProfileImage(imageUri)

        // Then
        assertEquals(expectedUrl, result)
        coVerify { userProfileRepository.updateProfileImage(imageUri) }
    }

    @Test
    fun `uploadProfileImage should propagate repository exceptions`() = runTest {
        // Given
        val imageUri = mockk<Uri>()
        every { imageUri.toString() } returns "content://media/image/123"
        every { imageUri.scheme } returns "content"
        val exception = ProfileError.StorageError("upload", RuntimeException("Upload failed"))
        coEvery { userProfileRepository.updateProfileImage(imageUri) } throws exception

        // When & Then
        val thrownException = assertFailsWith<ProfileError.StorageError> {
            manageProfileImageUseCase.uploadProfileImage(imageUri)
        }
        assertEquals("upload", thrownException.operation)
    }

    @Test
    fun `deleteProfileImage should call repository delete method`() = runTest {
        // Given
        coEvery { userProfileRepository.deleteProfileImage() } returns Unit

        // When
        manageProfileImageUseCase.deleteProfileImage()

        // Then
        coVerify { userProfileRepository.deleteProfileImage() }
    }

    @Test
    fun `deleteProfileImage should propagate repository exceptions`() = runTest {
        // Given
        val exception = ProfileError.StorageError("delete", RuntimeException("Delete failed"))
        coEvery { userProfileRepository.deleteProfileImage() } throws exception

        // When & Then
        val thrownException = assertFailsWith<ProfileError.StorageError> {
            manageProfileImageUseCase.deleteProfileImage()
        }
        assertEquals("delete", thrownException.operation)
    }
}
