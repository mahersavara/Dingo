package io.sukhuat.dingo.data.mapper

import com.google.firebase.Timestamp
import io.sukhuat.dingo.data.model.FirebaseUserProfile
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
        return UserProfile(
            userId = firebaseProfile.userId,
            displayName = firebaseProfile.displayName,
            email = firebaseProfile.email,
            profileImageUrl = firebaseProfile.profileImageUrl,
            joinDate = firebaseProfile.joinDate.toDate().time,
            isEmailVerified = firebaseProfile.isEmailVerified,
            authProvider = AuthProvider.valueOf(firebaseProfile.authProvider),
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
            joinDate = Timestamp(java.util.Date(userProfile.joinDate)),
            isEmailVerified = userProfile.isEmailVerified,
            authProvider = userProfile.authProvider.name,
            lastLoginDate = userProfile.lastLoginDate?.let { Timestamp(java.util.Date(it)) }
        )
    }
}
