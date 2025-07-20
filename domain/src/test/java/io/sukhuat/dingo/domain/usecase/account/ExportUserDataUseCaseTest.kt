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

class ExportUserDataUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var exportUserDataUseCase: ExportUserDataUseCase

    @Before
    fun setUp() {
        userProfileRepository = mockk()
        exportUserDataUseCase = ExportUserDataUseCase(userProfileRepository)
    }

    @Test
    fun `invoke should return exported data from repository`() = runTest {
        // Given
        val expectedData = "exported-user-data-json"
        coEvery { userProfileRepository.exportUserData() } returns expectedData

        // When
        val result = exportUserDataUseCase()

        // Then
        assertEquals(expectedData, result)
        coVerify { userProfileRepository.exportUserData() }
    }

    @Test
    fun `invoke should propagate authentication errors`() = runTest {
        // Given
        val authError = ProfileError.AuthenticationExpired
        coEvery { userProfileRepository.exportUserData() } throws authError

        // When & Then
        assertFailsWith<ProfileError.AuthenticationExpired> {
            exportUserDataUseCase()
        }
    }

    @Test
    fun `invoke should propagate network errors`() = runTest {
        // Given
        val networkError = ProfileError.NetworkUnavailable
        coEvery { userProfileRepository.exportUserData() } throws networkError

        // When & Then
        assertFailsWith<ProfileError.NetworkUnavailable> {
            exportUserDataUseCase()
        }
    }

    @Test
    fun `invoke should wrap unknown errors in UnknownError`() = runTest {
        // Given
        val unknownError = RuntimeException("Export failed")
        coEvery { userProfileRepository.exportUserData() } throws unknownError

        // When & Then
        val wrappedException = assertFailsWith<ProfileError.UnknownError> {
            exportUserDataUseCase()
        }
        assertEquals(unknownError, wrappedException.cause)
    }
}
