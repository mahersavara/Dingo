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

    @PropertyName("display_name")
    val displayName: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("profile_image_url")
    val profileImageUrl: String? = null,

    @PropertyName("join_date")
    val joinDate: Timestamp = Timestamp.now(),

    @PropertyName("is_email_verified")
    val isEmailVerified: Boolean = false,

    @PropertyName("auth_provider")
    val authProvider: String = "EMAIL_PASSWORD",

    @PropertyName("last_login_date")
    val lastLoginDate: Timestamp? = null
)
