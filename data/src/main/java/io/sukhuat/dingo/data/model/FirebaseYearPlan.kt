package io.sukhuat.dingo.data.model

/**
 * Firebase model for year planning data
 * Used for Firestore serialization/deserialization
 */
data class FirebaseYearPlan(
    val userId: String = "",
    val year: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val months: Map<String, FirebaseMonthPlan> = emptyMap(), // "1" to "12"
    val metadata: YearPlanMetadata = YearPlanMetadata()
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", 0, 0L, 0L, emptyMap(), YearPlanMetadata())
}

/**
 * Metadata for year plan
 */
data class YearPlanMetadata(
    val totalEntries: Int = 0,
    val lastAccessedMonth: Int = 1,
    val theme: String = "vintage"
) {
    // Required no-argument constructor for Firebase
    constructor() : this(0, 1, "vintage")
}
