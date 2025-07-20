package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HelpSupportTest {

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
    fun helpSupport_displaysFAQSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify FAQ section is displayed
        composeTestRule.onNodeWithText("Frequently Asked Questions").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do I create a goal?").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do I track my progress?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Can I share my achievements?").assertIsDisplayed()
        composeTestRule.onNodeWithText("How do I change my password?").assertIsDisplayed()
    }

    @Test
    fun helpSupport_faqItems_expandAndCollapse() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Initially FAQ answers should be hidden
        composeTestRule.onNodeWithText("Tap the + button to create a new goal").assertDoesNotExist()

        // Click on FAQ item to expand
        composeTestRule.onNodeWithText("How do I create a goal?").performClick()

        // Verify answer is now displayed
        composeTestRule.onNodeWithText("Tap the + button to create a new goal").assertIsDisplayed()

        // Click again to collapse
        composeTestRule.onNodeWithText("How do I create a goal?").performClick()

        // Verify answer is hidden again
        composeTestRule.onNodeWithText("Tap the + button to create a new goal").assertDoesNotExist()
    }

    @Test
    fun helpSupport_displaysTutorialSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify tutorial section is displayed
        composeTestRule.onNodeWithText("Tutorials & Guides").assertIsDisplayed()
        composeTestRule.onNodeWithText("Getting Started Guide").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Features").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tips & Tricks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Video Tutorials").assertIsDisplayed()
    }

    @Test
    fun helpSupport_tutorialItems_triggerCallback() {
        var tutorialOpened = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = { tutorialOpened = it }
                )
            }
        }

        // Click on tutorial item
        composeTestRule.onNodeWithText("Getting Started Guide").performClick()

        // Verify callback was triggered with correct tutorial
        assert(tutorialOpened == "getting_started")
    }

    @Test
    fun helpSupport_displaysFeedbackSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify feedback section is displayed
        composeTestRule.onNodeWithText("Send Feedback").assertIsDisplayed()
        composeTestRule.onNodeWithText("Help us improve the app").assertIsDisplayed()
        composeTestRule.onNodeWithText("Feedback Type").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bug Report").assertIsDisplayed()
        composeTestRule.onNodeWithText("Feature Request").assertIsDisplayed()
        composeTestRule.onNodeWithText("General Feedback").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit Feedback").assertIsDisplayed()
    }

    @Test
    fun helpSupport_feedbackForm_worksCorrectly() {
        var feedbackType = ""
        var feedbackMessage = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { type, message ->
                        feedbackType = type
                        feedbackMessage = message
                    },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Select feedback type
        composeTestRule.onNodeWithText("Bug Report").performClick()

        // Enter feedback message
        composeTestRule.onNodeWithText("Your Message").performTextInput("The app crashes when I try to save a goal")

        // Submit feedback
        composeTestRule.onNodeWithText("Submit Feedback").performClick()

        // Verify callback was triggered with correct values
        assert(feedbackType == "Bug Report")
        assert(feedbackMessage == "The app crashes when I try to save a goal")
    }

    @Test
    fun helpSupport_feedbackSubmission_showsLoadingState() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = true,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify loading state is displayed
        composeTestRule.onNodeWithText("Submitting...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit Feedback").assertIsNotEnabled()
    }

    @Test
    fun helpSupport_feedbackError_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = "Failed to submit feedback. Please try again.",
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to submit feedback. Please try again.").assertIsDisplayed()
    }

    @Test
    fun helpSupport_displaysContactSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify contact section is displayed
        composeTestRule.onNodeWithText("Contact Support").assertIsDisplayed()
        composeTestRule.onNodeWithText("Need more help? Get in touch with our support team").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Support").assertIsDisplayed()
        composeTestRule.onNodeWithText("Live Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Response time: 24-48 hours").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available 9 AM - 5 PM EST").assertIsDisplayed()
    }

    @Test
    fun helpSupport_contactSupport_triggersCallback() {
        var contactMethod = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = { contactMethod = it },
                    onOpenTutorial = {}
                )
            }
        }

        // Click email support
        composeTestRule.onNodeWithText("Email Support").performClick()

        // Verify callback was triggered with correct method
        assert(contactMethod == "email")
    }

    @Test
    fun helpSupport_displaysAppInfoSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify app info section is displayed
        composeTestRule.onNodeWithText("App Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version 1.0.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terms of Service").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()
    }

    @Test
    fun helpSupport_searchFAQ_worksCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Enter search query
        composeTestRule.onNodeWithText("Search FAQ").performTextInput("goal")

        // Verify relevant FAQ items are shown
        composeTestRule.onNodeWithText("How do I create a goal?").assertIsDisplayed()

        // Verify irrelevant FAQ items are hidden
        composeTestRule.onNodeWithText("How do I change my password?").assertDoesNotExist()
    }

    @Test
    fun helpSupport_hasAccessibilitySupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Verify accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Frequently asked questions").assertExists()
        composeTestRule.onNodeWithContentDescription("Submit feedback form").assertExists()
        composeTestRule.onNodeWithContentDescription("Contact support").assertExists()
        composeTestRule.onNodeWithContentDescription("Open tutorial").assertExists()
    }

    @Test
    fun helpSupport_feedbackValidation_worksCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, _ -> },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Initially submit button should be disabled (no message)
        composeTestRule.onNodeWithText("Submit Feedback").assertIsNotEnabled()

        // Enter message
        composeTestRule.onNodeWithText("Your Message").performTextInput("Great app!")

        // Submit button should now be enabled
        composeTestRule.onNodeWithText("Submit Feedback").assertIsEnabled()
    }

    @Test
    fun helpSupport_deviceInfo_includedInBugReport() {
        var feedbackMessage = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                HelpSupport(
                    profile = testUserProfile,
                    isSubmittingFeedback = false,
                    feedbackError = null,
                    onSubmitFeedback = { _, message -> feedbackMessage = message },
                    onContactSupport = {},
                    onOpenTutorial = {}
                )
            }
        }

        // Select bug report
        composeTestRule.onNodeWithText("Bug Report").performClick()

        // Enter message
        composeTestRule.onNodeWithText("Your Message").performTextInput("App crashes")

        // Submit feedback
        composeTestRule.onNodeWithText("Submit Feedback").performClick()

        // Verify device info is included in bug reports
        assert(feedbackMessage.contains("Device:"))
        assert(feedbackMessage.contains("Android"))
        assert(feedbackMessage.contains("App Version:"))
    }
}
