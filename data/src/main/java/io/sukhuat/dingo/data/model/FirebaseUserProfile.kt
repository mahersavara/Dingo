package io.sukhuat.dingo.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firebase data model for user profile information
 */
data class FirebaseUserProfile(
    @DocumentId
    val userId: String = "",

    val displayName: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("profileImageUrl")
    val profileImageUrl: String? = null,

    @PropertyName("google_photo_url")
    val googlePhotoUrl: String? = null,

    @PropertyName("has_custom_image")
    val hasCustomImage: Boolean = false,

    @PropertyName("last_image_update")
    val lastImageUpdate: Timestamp? = null,

    @PropertyName("joinDate")
    val joinDate: Timestamp = Timestamp.now(),

    @PropertyName("emailVerified")
    val isEmailVerified: Boolean = false,

    @PropertyName("authProvider")
    val authProvider: String = "EMAIL_PASSWORD",

    @PropertyName("lastLoginDate")
    val lastLoginDate: Timestamp? = null,

    @PropertyName("has_google_auth")
    val hasGoogleAuth: Boolean = false,

    @PropertyName("has_password_auth")
    val hasPasswordAuth: Boolean = false,

    @PropertyName("can_change_password")
    val canChangePassword: Boolean = false
)
