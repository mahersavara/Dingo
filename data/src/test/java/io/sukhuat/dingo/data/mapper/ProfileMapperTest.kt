package io.sukhuat.dingo.data.mapper

import com.google.firebase.Timestamp
import io.sukhuat.dingo.data.model.FirebaseUserProfile
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ProfileMapperTest {

    private val testTimestamp = Timestamp(Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)))

    private val testFirebaseProfile = FirebaseUserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/image.jpg",
        joinDate = testTimestamp,
        isEmailVerified = true,
        authProvider = "EMAIL_PASSWORD",
        lastLoginDate = testTimestamp
    )

    private val testDomainProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/image.jpg",
        joinDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L)
    )

    @Test
    fun `toDomain should convert Firebase profile to domain profile correctly`() {
        // When
        val result = ProfileMapper.toDomain(testFirebaseProfile)

        // Then
        assertEquals(testDomainProfile.userId, result.userId)
        assertEquals(testDomainProfile.displayName, result.displayName)
        assertEquals(testDomainProfile.email, result.email)
        assertEquals(testDomainProfile.profileImageUrl, result.profileImageUrl)
        assertEquals(testDomainProfile.isEmailVerified, result.isEmailVerified)
        assertEquals(testDomainProfile.authProvider, result.authProvider)
        // Note: Exact time comparison might be tricky due to timezone conversions
        // Compare timestamps with some tolerance for conversion differences
        assertTrue(kotlin.math.abs(testDomainProfile.joinDate - result.joinDate) < 1000)
    }

    @Test
    fun `toDomain should handle null profile image URL`() {
        // Given
        val firebaseProfileWithNullImage = testFirebaseProfile.copy(profileImageUrl = null)

        // When
        val result = ProfileMapper.toDomain(firebaseProfileWithNullImage)

        // Then
        assertNull(result.profileImageUrl)
    }

    @Test
    fun `toDomain should handle null last login date`() {
        // Given
        val firebaseProfileWithNullLogin = testFirebaseProfile.copy(lastLoginDate = null)

        // When
        val result = ProfileMapper.toDomain(firebaseProfileWithNullLogin)

        // Then
        assertNull(result.lastLoginDate)
    }

    @Test
    fun `toDomain should handle different auth providers`() {
        // Given
        val googleProfile = testFirebaseProfile.copy(authProvider = "GOOGLE")
        val anonymousProfile = testFirebaseProfile.copy(authProvider = "ANONYMOUS")

        // When
        val googleResult = ProfileMapper.toDomain(googleProfile)
        val anonymousResult = ProfileMapper.toDomain(anonymousProfile)

        // Then
        assertEquals(AuthProvider.GOOGLE, googleResult.authProvider)
        assertEquals(AuthProvider.ANONYMOUS, anonymousResult.authProvider)
    }

    @Test
    fun `toFirebase should convert domain profile to Firebase profile correctly`() {
        // When
        val result = ProfileMapper.toFirebase(testDomainProfile)

        // Then
        assertEquals(testFirebaseProfile.userId, result.userId)
        assertEquals(testFirebaseProfile.displayName, result.displayName)
        assertEquals(testFirebaseProfile.email, result.email)
        assertEquals(testFirebaseProfile.profileImageUrl, result.profileImageUrl)
        assertEquals(testFirebaseProfile.isEmailVerified, result.isEmailVerified)
        assertEquals(testFirebaseProfile.authProvider, result.authProvider)
        // Note: Exact time comparison might be tricky due to timezone conversions
        // Compare Firebase timestamps with some tolerance
        assertTrue(kotlin.math.abs(testFirebaseProfile.joinDate.toDate().time - result.joinDate.toDate().time) < 1000)
    }

    @Test
    fun `toFirebase should handle null profile image URL`() {
        // Given
        val domainProfileWithNullImage = testDomainProfile.copy(profileImageUrl = null)

        // When
        val result = ProfileMapper.toFirebase(domainProfileWithNullImage)

        // Then
        assertNull(result.profileImageUrl)
    }

    @Test
    fun `toFirebase should handle null last login date`() {
        // Given
        val domainProfileWithNullLogin = testDomainProfile.copy(lastLoginDate = null)

        // When
        val result = ProfileMapper.toFirebase(domainProfileWithNullLogin)

        // Then
        assertNull(result.lastLoginDate)
    }

    @Test
    fun `toFirebase should handle different auth providers`() {
        // Given
        val googleProfile = testDomainProfile.copy(authProvider = AuthProvider.GOOGLE)
        val anonymousProfile = testDomainProfile.copy(authProvider = AuthProvider.ANONYMOUS)

        // When
        val googleResult = ProfileMapper.toFirebase(googleProfile)
        val anonymousResult = ProfileMapper.toFirebase(anonymousProfile)

        // Then
        assertEquals("GOOGLE", googleResult.authProvider)
        assertEquals("ANONYMOUS", anonymousResult.authProvider)
    }

    @Test
    fun `round trip conversion should preserve data integrity`() {
        // When
        val firebaseConverted = ProfileMapper.toFirebase(testDomainProfile)
        val domainConverted = ProfileMapper.toDomain(firebaseConverted)

        // Then
        assertEquals(testDomainProfile.userId, domainConverted.userId)
        assertEquals(testDomainProfile.displayName, domainConverted.displayName)
        assertEquals(testDomainProfile.email, domainConverted.email)
        assertEquals(testDomainProfile.profileImageUrl, domainConverted.profileImageUrl)
        assertEquals(testDomainProfile.isEmailVerified, domainConverted.isEmailVerified)
        assertEquals(testDomainProfile.authProvider, domainConverted.authProvider)
    }

    @Test
    fun `should handle edge case with empty strings`() {
        // Given
        val profileWithEmptyStrings = testFirebaseProfile.copy(
            displayName = "",
            email = "",
            profileImageUrl = ""
        )

        // When
        val result = ProfileMapper.toDomain(profileWithEmptyStrings)

        // Then
        assertEquals("", result.displayName)
        assertEquals("", result.email)
        assertEquals("", result.profileImageUrl)
    }

    @Test
    fun `should handle very long display names`() {
        // Given
        val longName = "A".repeat(1000)
        val profileWithLongName = testFirebaseProfile.copy(displayName = longName)

        // When
        val result = ProfileMapper.toDomain(profileWithLongName)

        // Then
        assertEquals(longName, result.displayName)
    }
}
