package io.sukhuat.dingo.data.mapper

import com.google.firebase.Timestamp
import io.sukhuat.dingo.data.model.FirebaseUserProfile
import io.sukhuat.dingo.domain.model.AuthCapabilities
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile

/**
 * Mapper for converting between domain UserProfile and Firebase data models
 */
object ProfileMapper {

    /**
     * Convert Firebase model to domain model
     */
    fun toDomain(firebaseProfile: FirebaseUserProfile): UserProfile {
        val authCapabilities = AuthCapabilities(
            hasGoogleAuth = firebaseProfile.hasGoogleAuth,
            hasPasswordAuth = firebaseProfile.hasPasswordAuth,
            canChangePassword = firebaseProfile.canChangePassword
        )

        return UserProfile(
            userId = firebaseProfile.userId,
            displayName = firebaseProfile.displayName,
            email = firebaseProfile.email,
            profileImageUrl = firebaseProfile.profileImageUrl,
            googlePhotoUrl = firebaseProfile.googlePhotoUrl,
            hasCustomImage = firebaseProfile.hasCustomImage,
            lastImageUpdate = firebaseProfile.lastImageUpdate?.toDate()?.time,
            joinDate = firebaseProfile.joinDate.toDate().time,
            isEmailVerified = firebaseProfile.isEmailVerified,
            authProvider = AuthProvider.valueOf(firebaseProfile.authProvider),
            authCapabilities = authCapabilities,
            lastLoginDate = firebaseProfile.lastLoginDate?.toDate()?.time
        )
    }

    /**
     * Convert domain model to Firebase model
     */
    fun toFirebase(userProfile: UserProfile): FirebaseUserProfile {
        return FirebaseUserProfile(
            userId = userProfile.userId,
            displayName = userProfile.displayName,
            email = userProfile.email,
            profileImageUrl = userProfile.profileImageUrl,
            googlePhotoUrl = userProfile.googlePhotoUrl,
            hasCustomImage = userProfile.hasCustomImage,
            lastImageUpdate = userProfile.lastImageUpdate?.let { Timestamp(java.util.Date(it)) },
            joinDate = Timestamp(java.util.Date(userProfile.joinDate)),
            isEmailVerified = userProfile.isEmailVerified,
            authProvider = userProfile.authProvider.name,
            hasGoogleAuth = userProfile.authCapabilities.hasGoogleAuth,
            hasPasswordAuth = userProfile.authCapabilities.hasPasswordAuth,
            canChangePassword = userProfile.authCapabilities.canChangePassword,
            lastLoginDate = userProfile.lastLoginDate?.let { Timestamp(java.util.Date(it)) }
        )
    }
}
