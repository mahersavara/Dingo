package io.sukhuat.dingo.data.model

/**
 * Firebase model for month planning data
 * Used for Firestore serialization/deserialization
 */
data class FirebaseMonthPlan(
    val monthIndex: Int = 0,
    val content: String = "",
    val lastModified: Long = 0L,
    val wordCount: Int = 0
) {
    // Required no-argument constructor for Firebase
    constructor() : this(0, "", 0L, 0)
}
