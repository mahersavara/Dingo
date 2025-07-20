package io.sukhuat.dingo.ui.screens.profile

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ProfileErrorHandler
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.usecase.account.ChangePasswordUseCase
import io.sukhuat.dingo.domain.usecase.account.DeleteAccountUseCase
import io.sukhuat.dingo.domain.usecase.account.ExportUserDataUseCase
import io.sukhuat.dingo.domain.usecase.account.GetLoginHistoryUseCase
import io.sukhuat.dingo.domain.usecase.preferences.GetUserPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetAchievementsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetProfileStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageProfileImageUseCase
import io.sukhuat.dingo.domain.usecase.profile.RefreshStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareAchievementUseCase
import io.sukhuat.dingo.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Mock use cases
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var getProfileStatisticsUseCase: GetProfileStatisticsUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var manageProfileImageUseCase: ManageProfileImageUseCase
    private lateinit var refreshStatisticsUseCase: RefreshStatisticsUseCase
    private lateinit var getAchievementsUseCase: GetAchievementsUseCase
    private lateinit var shareAchievementUseCase: ShareAchievementUseCase
    private lateinit var exportUserDataUseCase: ExportUserDataUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private lateinit var getLoginHistoryUseCase: GetLoginHistoryUseCase
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase
    private lateinit var updatePreferencesUseCase: UpdatePreferencesUseCase
    private lateinit var profileErrorHandler: ProfileErrorHandler

    private lateinit var viewModel: ProfileViewModel

    // Test data
    private val testUserProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/image.jpg",
        joinDate = LocalDateTime.of(2024, 1, 1, 0, 0),
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = LocalDateTime.of(2024, 7, 18, 10, 0)
    )

    private val testAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = 1,
        unlockedDate = LocalDateTime.of(2024, 1, 15, 10, 0),
        isUnlocked = true
    )

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = mapOf("2024-01" to MonthlyStats("2024-01", 5, 3, 0.6f)),
        achievements = listOf(testAchievement)
    )

    private val testUserPreferences = UserPreferences(
        isDarkModeEnabled = false,
        isNotificationsEnabled = true,
        isSoundEnabled = true,
        isVibrationEnabled = true,
        languageCode = "en"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        getUserProfileUseCase = mockk()
        getProfileStatisticsUseCase = mockk()
        updateProfileUseCase = mockk(relaxed = true)
        manageProfileImageUseCase = mockk(relaxed = true)
        refreshStatisticsUseCase = mockk(relaxed = true)
        getAchievementsUseCase = mockk()
        shareAchievementUseCase = mockk(relaxed = true)
        exportUserDataUseCase = mockk()
        deleteAccountUseCase = mockk(relaxed = true)
        changePasswordUseCase = mockk(relaxed = true)
        getLoginHistoryUseCase = mockk()
        getUserPreferencesUseCase = mockk()
        updatePreferencesUseCase = mockk(relaxed = true)
        profileErrorHandler = mockk(relaxed = true)

        // Setup default mock behaviors
        coEvery { getUserProfileUseCase() } returns flowOf(testUserProfile)
        coEvery { getProfileStatisticsUseCase() } returns flowOf(testProfileStatistics)
        every { getUserPreferencesUseCase() } returns flowOf(testUserPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = profileErrorHandler
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // When
        viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertIs<ProfileUiState.Loading>(initialState)
        }
    }

    @Test
    fun `should load profile data successfully on initialization`() = runTest {
        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<ProfileUiState.Success>(state)
            assertEquals(testUserProfile, state.profile)
            assertEquals(testProfileStatistics, state.statistics)
            assertEquals(false, state.isRefreshing)
        }
    }

    @Test
    fun `should handle profile loading error`() = runTest {
        // Given
        val exception = RuntimeException("Profile loading failed")
        coEvery { getUserProfileUseCase() } throws exception

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<ProfileUiState.Error>(state)
            assertEquals("An unexpected error occurred", state.message)
        }
    }

    @Test
    fun `updateDisplayName should update profile successfully`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val newDisplayName = "Updated Name"

        // When
        viewModel.updateDisplayName(newDisplayName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updateProfileUseCase.updateDisplayName(newDisplayName) }

        viewModel.editState.test {
            val state = awaitItem()
            assertEquals(false, state.isEditing)
            assertEquals(false, state.isValidating)
            assertEquals(null, state.editingField)
            assertEquals("", state.tempDisplayName)
        }
    }

    @Test
    fun `updateDisplayName should handle validation error`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val invalidName = ""
        val validationError = ProfileError.ValidationError("displayName", "Display name cannot be empty")
        coEvery { updateProfileUseCase.updateDisplayName(invalidName) } throws validationError

        // When
        viewModel.updateDisplayName(invalidName)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.editState.test {
            val state = awaitItem()
            assertEquals(false, state.isValidating)
            assertEquals("Display name cannot be empty", state.validationError)
        }
    }

    @Test
    fun `uploadProfileImage should update image successfully`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val imageUri = mockk<Uri>()
        val expectedUrl = "https://example.com/new-image.jpg"
        coEvery { manageProfileImageUseCase.uploadProfileImage(imageUri) } returns expectedUrl

        // When
        viewModel.uploadProfileImage(imageUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { manageProfileImageUseCase.uploadProfileImage(imageUri) }

        viewModel.imageUploadState.test {
            val state = awaitItem()
            assertEquals(false, state.isUploading)
            assertEquals(0f, state.progress)
            assertEquals(null, state.error)
        }
    }

    @Test
    fun `uploadProfileImage should handle upload error`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val imageUri = mockk<Uri>()
        val uploadError = ProfileError.StorageError("upload", RuntimeException("Upload failed"))
        coEvery { manageProfileImageUseCase.uploadProfileImage(imageUri) } throws uploadError

        // When
        viewModel.uploadProfileImage(imageUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.imageUploadState.test {
            val state = awaitItem()
            assertEquals(false, state.isUploading)
            assertEquals("Failed to upload image", state.error)
        }
    }

    @Test
    fun `refreshStatistics should update statistics`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refreshStatistics()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { refreshStatisticsUseCase() }
    }

    @Test
    fun `shareAchievement should share achievement successfully`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val achievementId = "achievement-1"
        coEvery { getAchievementsUseCase() } returns listOf(testAchievement)
        coEvery { shareAchievementUseCase(testAchievement) } returns "Shared content"

        // When
        viewModel.shareAchievement(achievementId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { getAchievementsUseCase() }
        coVerify { shareAchievementUseCase(testAchievement) }
    }

    @Test
    fun `shareAchievement should handle achievement not found`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val achievementId = "non-existent-achievement"
        coEvery { getAchievementsUseCase() } returns listOf(testAchievement)

        // When
        viewModel.shareAchievement(achievementId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<ProfileUiState.Error>(state)
        }
    }

    @Test
    fun `selectTab should update tab state`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectTab(ProfileTab.STATISTICS)

        // Then
        viewModel.tabState.test {
            val state = awaitItem()
            assertEquals(ProfileTab.STATISTICS, state.selectedTab)
        }
    }

    @Test
    fun `startEditing should update edit state`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startEditing(ProfileField.DISPLAY_NAME)

        // Then
        viewModel.editState.test {
            val state = awaitItem()
            assertEquals(true, state.isEditing)
            assertEquals(ProfileField.DISPLAY_NAME, state.editingField)
            assertEquals(testUserProfile.displayName, state.tempDisplayName)
        }
    }

    @Test
    fun `cancelEditing should reset edit state`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startEditing(ProfileField.DISPLAY_NAME)

        // When
        viewModel.cancelEditing()

        // Then
        viewModel.editState.test {
            val state = awaitItem()
            assertEquals(false, state.isEditing)
            assertEquals(null, state.editingField)
            assertEquals("", state.tempDisplayName)
            assertEquals(null, state.validationError)
        }
    }

    @Test
    fun `updateTempDisplayName should update temporary name`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val newTempName = "Temporary Name"

        // When
        viewModel.updateTempDisplayName(newTempName)

        // Then
        viewModel.editState.test {
            val state = awaitItem()
            assertEquals(newTempName, state.tempDisplayName)
            assertEquals(null, state.validationError)
        }
    }

    @Test
    fun `retry should reload profile data`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<ProfileUiState.Success>(state)
        }
    }

    @Test
    fun `toggleDarkMode should update preferences`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleDarkMode(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePreferencesUseCase.updateDarkModeEnabled(true) }
    }

    @Test
    fun `toggleNotifications should update preferences`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleNotifications(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePreferencesUseCase.updateNotificationsEnabled(false) }
    }

    @Test
    fun `exportUserData should call export use case`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { exportUserDataUseCase() } returns "exported-data"

        // When
        viewModel.exportUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { exportUserDataUseCase() }
    }

    @Test
    fun `deleteAccount should call delete use case`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val confirmationText = "DELETE"

        // When
        viewModel.deleteAccount(confirmationText)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { deleteAccountUseCase(confirmationText) }
    }

    @Test
    fun `getProfileActions should return correct actions`() = runTest {
        // Given
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val actions = viewModel.getProfileActions()

        // Then
        assertTrue(actions.onEditProfile != null)
        assertTrue(actions.onUpdateDisplayName != null)
        assertTrue(actions.onDeleteProfileImage != null)
        assertTrue(actions.onShareAchievement != null)
        assertTrue(actions.onExportData != null)
        assertTrue(actions.onDeleteAccount != null)
        assertTrue(actions.onRefreshStats != null)
        assertTrue(actions.onRetry != null)
    }
}
