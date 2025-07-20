package io.sukhuat.dingo.data.mapper

import com.google.firebase.Timestamp
import io.sukhuat.dingo.data.model.FirebaseAchievement
import io.sukhuat.dingo.data.model.FirebaseMonthlyStats
import io.sukhuat.dingo.data.model.FirebaseProfileStatistics
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ProfileStatisticsMapperTest {

    private val testTimestamp = Timestamp(Date(System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)))

    private val testFirebaseAchievement = FirebaseAchievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = 1,
        unlockedDate = testTimestamp,
        isUnlocked = true
    )

    private val testDomainAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = 1,
        unlockedDate = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L),
        isUnlocked = true
    )

    private val testFirebaseMonthlyStats = FirebaseMonthlyStats(
        month = "2024-01",
        goalsCreated = 5,
        goalsCompleted = 3,
        completionRate = 0.6f
    )

    private val testDomainMonthlyStats = MonthlyStats(
        month = "2024-01",
        goalsCreated = 5,
        goalsCompleted = 3,
        completionRate = 0.6f
    )

    private val testFirebaseStatistics = FirebaseProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = mapOf("2024-01" to testFirebaseMonthlyStats),
        achievements = mapOf("achievement-1" to testFirebaseAchievement)
    )

    private val testDomainStatistics = ProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = mapOf("2024-01" to testDomainMonthlyStats),
        achievements = listOf(testDomainAchievement)
    )

    @Test
    fun `toDomain should convert Firebase statistics to domain statistics correctly`() {
        // When
        val result = ProfileStatisticsMapper.toDomain(testFirebaseStatistics)

        // Then
        assertEquals(testDomainStatistics.totalGoalsCreated, result.totalGoalsCreated)
        assertEquals(testDomainStatistics.completedGoals, result.completedGoals)
        assertEquals(testDomainStatistics.completionRate, result.completionRate)
        assertEquals(testDomainStatistics.currentStreak, result.currentStreak)
        assertEquals(testDomainStatistics.longestStreak, result.longestStreak)
        assertEquals(1, result.monthlyStats.size)
        assertEquals(testDomainMonthlyStats.month, result.monthlyStats["2024-01"]?.month)
        assertEquals(1, result.achievements.size)
        assertEquals(testDomainAchievement.id, result.achievements[0].id)
    }

    @Test
    fun `toDomain should handle empty monthly stats and achievements`() {
        // Given
        val emptyFirebaseStats = testFirebaseStatistics.copy(
            monthlyStats = emptyMap(),
            achievements = emptyMap()
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(emptyFirebaseStats)

        // Then
        assertEquals(0, result.monthlyStats.size)
        assertEquals(0, result.achievements.size)
    }

    @Test
    fun `toDomain should handle multiple monthly stats and achievements`() {
        // Given
        val multipleFirebaseStats = testFirebaseStatistics.copy(
            monthlyStats = mapOf(
                "2024-01" to testFirebaseMonthlyStats,
                "2024-02" to testFirebaseMonthlyStats.copy(month = "2024-02", goalsCreated = 8, goalsCompleted = 6)
            ),
            achievements = mapOf(
                "achievement-1" to testFirebaseAchievement,
                "achievement-2" to testFirebaseAchievement.copy(id = "achievement-2", title = "Second Goal")
            )
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(multipleFirebaseStats)

        // Then
        assertEquals(2, result.monthlyStats.size)
        assertEquals(2, result.achievements.size)
        assertEquals("2024-01", result.monthlyStats["2024-01"]?.month)
        assertEquals("2024-02", result.monthlyStats["2024-02"]?.month)
        assertEquals("achievement-1", result.achievements.find { it.id == "achievement-1" }?.id)
        assertEquals("achievement-2", result.achievements.find { it.id == "achievement-2" }?.id)
    }

    @Test
    fun `toFirebase should convert domain statistics to Firebase statistics correctly`() {
        // When
        val result = ProfileStatisticsMapper.toFirebase(testDomainStatistics)

        // Then
        assertEquals(testFirebaseStatistics.totalGoalsCreated, result.totalGoalsCreated)
        assertEquals(testFirebaseStatistics.completedGoals, result.completedGoals)
        assertEquals(testFirebaseStatistics.completionRate, result.completionRate)
        assertEquals(testFirebaseStatistics.currentStreak, result.currentStreak)
        assertEquals(testFirebaseStatistics.longestStreak, result.longestStreak)
        assertEquals(1, result.monthlyStats.size)
        assertEquals(testFirebaseMonthlyStats.month, result.monthlyStats["2024-01"]?.month)
        assertEquals(1, result.achievements.size)
        assertEquals(testFirebaseAchievement.id, result.achievements["achievement-1"]?.id)
    }

    @Test
    fun `toFirebase should handle empty monthly stats and achievements`() {
        // Given
        val emptyDomainStats = testDomainStatistics.copy(
            monthlyStats = emptyMap(),
            achievements = emptyList()
        )

        // When
        val result = ProfileStatisticsMapper.toFirebase(emptyDomainStats)

        // Then
        assertEquals(0, result.monthlyStats.size)
        assertEquals(0, result.achievements.size)
    }

    @Test
    fun `achievement conversion should handle null unlocked date`() {
        // Given
        val lockedAchievement = testFirebaseAchievement.copy(
            unlockedDate = null,
            isUnlocked = false
        )
        val statsWithLockedAchievement = testFirebaseStatistics.copy(
            achievements = mapOf("achievement-1" to lockedAchievement)
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(statsWithLockedAchievement)

        // Then
        assertEquals(1, result.achievements.size)
        assertNull(result.achievements[0].unlockedDate)
        assertEquals(false, result.achievements[0].isUnlocked)
    }

    @Test
    fun `monthly stats conversion should preserve all fields`() {
        // Given
        val detailedMonthlyStats = FirebaseMonthlyStats(
            month = "2024-03",
            goalsCreated = 15,
            goalsCompleted = 12,
            completionRate = 0.8f
        )
        val statsWithDetailedMonthly = testFirebaseStatistics.copy(
            monthlyStats = mapOf("2024-03" to detailedMonthlyStats)
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(statsWithDetailedMonthly)

        // Then
        val monthlyResult = result.monthlyStats["2024-03"]!!
        assertEquals("2024-03", monthlyResult.month)
        assertEquals(15, monthlyResult.goalsCreated)
        assertEquals(12, monthlyResult.goalsCompleted)
        assertEquals(0.8f, monthlyResult.completionRate)
    }

    @Test
    fun `round trip conversion should preserve data integrity`() {
        // When
        val firebaseConverted = ProfileStatisticsMapper.toFirebase(testDomainStatistics)
        val domainConverted = ProfileStatisticsMapper.toDomain(firebaseConverted)

        // Then
        assertEquals(testDomainStatistics.totalGoalsCreated, domainConverted.totalGoalsCreated)
        assertEquals(testDomainStatistics.completedGoals, domainConverted.completedGoals)
        assertEquals(testDomainStatistics.completionRate, domainConverted.completionRate)
        assertEquals(testDomainStatistics.currentStreak, domainConverted.currentStreak)
        assertEquals(testDomainStatistics.longestStreak, domainConverted.longestStreak)
        assertEquals(testDomainStatistics.monthlyStats.size, domainConverted.monthlyStats.size)
        assertEquals(testDomainStatistics.achievements.size, domainConverted.achievements.size)
    }

    @Test
    fun `should handle zero values correctly`() {
        // Given
        val zeroStats = testFirebaseStatistics.copy(
            totalGoalsCreated = 0,
            completedGoals = 0,
            completionRate = 0.0f,
            currentStreak = 0,
            longestStreak = 0
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(zeroStats)

        // Then
        assertEquals(0, result.totalGoalsCreated)
        assertEquals(0, result.completedGoals)
        assertEquals(0.0f, result.completionRate)
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
    }

    @Test
    fun `should handle high completion rates correctly`() {
        // Given
        val perfectStats = testFirebaseStatistics.copy(
            totalGoalsCreated = 100,
            completedGoals = 100,
            completionRate = 1.0f
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(perfectStats)

        // Then
        assertEquals(100, result.totalGoalsCreated)
        assertEquals(100, result.completedGoals)
        assertEquals(1.0f, result.completionRate)
    }

    @Test
    fun `should handle achievements with different icon resources`() {
        // Given
        val achievementWithDifferentIcon = testFirebaseAchievement.copy(
            iconResId = 999,
            title = "Special Achievement"
        )
        val statsWithSpecialAchievement = testFirebaseStatistics.copy(
            achievements = mapOf("special" to achievementWithDifferentIcon)
        )

        // When
        val result = ProfileStatisticsMapper.toDomain(statsWithSpecialAchievement)

        // Then
        assertEquals(1, result.achievements.size)
        assertEquals(999, result.achievements[0].iconResId)
        assertEquals("Special Achievement", result.achievements[0].title)
    }
}
