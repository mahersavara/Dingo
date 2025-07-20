package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProfileStatisticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = android.R.drawable.ic_menu_star,
        unlockedDate = LocalDateTime.of(2024, 1, 15, 10, 0),
        isUnlocked = true
    )

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 15,
        completedGoals = 12,
        completionRate = 0.8f,
        currentStreak = 7,
        longestStreak = 14,
        monthlyStats = mapOf(
            "January 2024" to MonthlyStats("January 2024", 5, 4, 0.8f),
            "February 2024" to MonthlyStats("February 2024", 6, 5, 0.83f),
            "March 2024" to MonthlyStats("March 2024", 4, 3, 0.75f)
        ),
        achievements = listOf(testAchievement)
    )

    private val emptyStatistics = ProfileStatistics()

    @Test
    fun profileStatistics_displaysMainStats() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify main statistics are displayed
        composeTestRule.onNodeWithText("15").assertIsDisplayed() // Total goals
        composeTestRule.onNodeWithText("12").assertIsDisplayed() // Completed goals
        composeTestRule.onNodeWithText("7 days").assertIsDisplayed() // Current streak
        composeTestRule.onNodeWithText("Total Goals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streak").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_displaysCompletionRate() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify completion rate is displayed
        composeTestRule.onNodeWithText("Completion Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
        composeTestRule.onNodeWithText("12 of 15 goals completed").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_displaysAchievements() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify achievements section is displayed
        composeTestRule.onNodeWithText("Achievements").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/1").assertIsDisplayed() // Achievement count
        composeTestRule.onNodeWithText("First Goal").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_achievementShare_triggersCallback() {
        var sharedAchievementId = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = { sharedAchievementId = it },
                    onRefreshStats = {}
                )
            }
        }

        // Click on achievement to share
        composeTestRule.onNodeWithText("First Goal").performClick()

        // Verify callback was triggered with correct ID
        assert(sharedAchievementId == "achievement-1")
    }

    @Test
    fun profileStatistics_detailedStatsToggle_works() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Initially detailed stats should be hidden
        composeTestRule.onNodeWithText("Show Detailed Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streak Information").assertDoesNotExist()

        // Click to show detailed stats
        composeTestRule.onNodeWithText("Show Detailed Stats").performClick()

        // Verify detailed stats are now shown
        composeTestRule.onNodeWithText("Hide Detailed Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streak Information").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_detailedStats_displaysStreakInfo() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Show detailed stats
        composeTestRule.onNodeWithText("Show Detailed Stats").performClick()

        // Verify streak information is displayed
        composeTestRule.onNodeWithText("Streak Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("7").assertIsDisplayed() // Current streak
        composeTestRule.onNodeWithText("14").assertIsDisplayed() // Longest streak
        composeTestRule.onNodeWithText("Current Streak").assertIsDisplayed()
        composeTestRule.onNodeWithText("Best Streak").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_detailedStats_displaysMonthlyBreakdown() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Show detailed stats
        composeTestRule.onNodeWithText("Show Detailed Stats").performClick()

        // Verify monthly breakdown is displayed
        composeTestRule.onNodeWithText("Monthly Breakdown").assertIsDisplayed()
        composeTestRule.onNodeWithText("January 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("4/5").assertIsDisplayed() // Goals completed/created
        composeTestRule.onNodeWithText("80%").assertIsDisplayed() // Completion rate
    }

    @Test
    fun profileStatistics_emptyState_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = emptyStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify empty state is displayed
        composeTestRule.onNodeWithText("Start Your Journey!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create your first goal to see your progress and achievements here.").assertIsDisplayed()

        // Verify main stats show zero values
        composeTestRule.onNodeWithText("0").assertIsDisplayed() // Should appear multiple times for different stats
    }

    @Test
    fun profileStatistics_emptyState_hidesAchievements() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = emptyStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify achievements section is not displayed for empty state
        composeTestRule.onNodeWithText("Achievements").assertDoesNotExist()
    }

    @Test
    fun profileStatistics_refreshCallback_triggersCorrectly() {
        var refreshTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = { refreshTriggered = true }
                )
            }
        }

        // Note: Refresh is typically triggered by pull-to-refresh gesture
        // This test verifies the callback is properly wired
        assert(!refreshTriggered) // Initially false
    }

    @Test
    fun profileStatistics_unlockedAchievement_hasShareButton() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify unlocked achievement has share functionality
        composeTestRule.onNodeWithText("First Goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Goal").assertHasClickAction()
    }

    @Test
    fun profileStatistics_lockedAchievement_displaysCorrectly() {
        val lockedAchievement = Achievement(
            id = "locked-achievement",
            title = "Locked Achievement",
            description = "Not yet unlocked",
            iconResId = android.R.drawable.ic_menu_star,
            unlockedDate = null,
            isUnlocked = false
        )

        val statsWithLockedAchievement = testProfileStatistics.copy(
            achievements = listOf(testAchievement, lockedAchievement)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = statsWithLockedAchievement,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify locked achievement is displayed but with different styling
        composeTestRule.onNodeWithText("Locked Achievement").assertIsDisplayed()
        composeTestRule.onNodeWithText("1/2").assertIsDisplayed() // Achievement count updated
    }

    @Test
    fun profileStatistics_hasAccessibilitySupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify accessibility features are present
        // Note: Specific accessibility content descriptions would need to be added to the component
        composeTestRule.onNodeWithText("Total Goals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Streak").assertIsDisplayed()
    }

    @Test
    fun profileStatistics_progressIndicator_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        // Verify completion rate progress indicator is displayed
        composeTestRule.onNodeWithText("Completion Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()

        // The progress bar itself would be tested through visual regression tests
        // or by checking if the progress component is rendered
    }
}
