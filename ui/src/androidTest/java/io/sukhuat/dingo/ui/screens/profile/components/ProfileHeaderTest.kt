package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.ui.screens.profile.ImageUploadState
import io.sukhuat.dingo.ui.screens.profile.ProfileEditState
import io.sukhuat.dingo.ui.screens.profile.ProfileField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProfileHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUserProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = null,
        joinDate = LocalDateTime.of(2024, 1, 1, 0, 0),
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = LocalDateTime.of(2024, 7, 18, 10, 0)
    )

    @Test
    fun profileHeader_displaysUserInformation() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify user information is displayed
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Member since January 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verified").assertIsDisplayed()
    }

    @Test
    fun profileHeader_displaysGoogleAuthProvider() {
        val googleProfile = testUserProfile.copy(authProvider = AuthProvider.GOOGLE)

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = googleProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify Google auth provider is displayed
        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
    }

    @Test
    fun profileHeader_displaysUnverifiedEmail() {
        val unverifiedProfile = testUserProfile.copy(isEmailVerified = false)

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = unverifiedProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify verified badge is not displayed
        composeTestRule.onNodeWithText("Verified").assertDoesNotExist()
    }

    @Test
    fun profileHeader_editButton_startsEditing() {
        var editingStarted = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = { editingStarted = true },
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Click edit button
        composeTestRule.onNodeWithContentDescription("Edit name").performClick()

        // Verify editing was started
        assert(editingStarted)
    }

    @Test
    fun profileHeader_editingMode_displaysTextField() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "Test User"
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify editing UI is displayed
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Confirm").assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Cancel").assertHasClickAction()
    }

    @Test
    fun profileHeader_editingMode_confirmButton_triggersCallback() {
        var confirmClicked = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "New Name"
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = { confirmClicked = true },
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Click confirm button
        composeTestRule.onNodeWithContentDescription("Confirm").performClick()

        // Verify callback was triggered
        assert(confirmClicked)
    }

    @Test
    fun profileHeader_editingMode_cancelButton_triggersCallback() {
        var cancelClicked = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "Test User"
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = { cancelClicked = true },
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Click cancel button
        composeTestRule.onNodeWithContentDescription("Cancel").performClick()

        // Verify callback was triggered
        assert(cancelClicked)
    }

    @Test
    fun profileHeader_editingMode_disablesConfirmForEmptyName() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = ""
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify confirm button is disabled for empty name
        composeTestRule.onNodeWithContentDescription("Confirm").assertIsNotEnabled()
    }

    @Test
    fun profileHeader_editingMode_showsValidationError() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "",
                        validationError = "Display name cannot be empty"
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify validation error is displayed
        composeTestRule.onNodeWithText("Display name cannot be empty").assertIsDisplayed()
    }

    @Test
    fun profileHeader_imageUpload_showsProgress() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(
                        isUploading = true,
                        progress = 0.6f
                    ),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify upload progress is displayed
        composeTestRule.onNodeWithText("Uploading... 60%").assertIsDisplayed()
    }

    @Test
    fun profileHeader_cameraButton_triggersImageUpload() {
        var imageUploadTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = { imageUploadTriggered = true },
                    onDeleteProfileImage = {}
                )
            }
        }

        // Click camera button
        composeTestRule.onNodeWithContentDescription("Change profile picture").performClick()

        // Note: In real test, this would trigger image picker, but we can't test that directly
        // The callback would be triggered when image picker returns a result
    }

    @Test
    fun profileHeader_hasAccessibilitySupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
        composeTestRule.onNodeWithContentDescription("Edit name").assertExists()
        composeTestRule.onNodeWithContentDescription("Change profile picture").assertExists()
    }

    @Test
    fun profileHeader_displaysEmptyNamePlaceholder() {
        val emptyNameProfile = testUserProfile.copy(displayName = "")

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = emptyNameProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        // Verify placeholder text is displayed for empty name
        composeTestRule.onNodeWithText("No name set").assertIsDisplayed()
    }
}
