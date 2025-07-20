package io.sukhuat.dingo.ui.screens.profile.components

/**
 * UI state for help and support functionality
 */
data class HelpSupportUiState(
    val isLoading: Boolean = false,
    val faqState: FaqState = FaqState(),
    val feedbackState: FeedbackState = FeedbackState(),
    val bugReportState: BugReportState = BugReportState(),
    val contactState: ContactState = ContactState(),
    val error: String? = null
)

/**
 * State for FAQ section
 */
data class FaqState(
    val searchQuery: String = "",
    val filteredFaqs: List<FaqItem> = emptyList(),
    val expandedFaqIds: Set<String> = emptySet(),
    val isSearching: Boolean = false
)

/**
 * State for feedback submission
 */
data class FeedbackState(
    val feedbackType: FeedbackType = FeedbackType.GENERAL,
    val subject: String = "",
    val message: String = "",
    val userEmail: String = "",
    val includeUserContext: Boolean = true,
    val isSubmitting: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val submissionSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

/**
 * State for bug reporting
 */
data class BugReportState(
    val bugTitle: String = "",
    val bugDescription: String = "",
    val stepsToReproduce: String = "",
    val expectedBehavior: String = "",
    val actualBehavior: String = "",
    val severity: BugSeverity = BugSeverity.MEDIUM,
    val includeDeviceInfo: Boolean = true,
    val includeAppLogs: Boolean = true,
    val deviceInfo: DeviceInfo? = null,
    val isSubmitting: Boolean = false,
    val showBugReportDialog: Boolean = false,
    val submissionSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

/**
 * State for contact support
 */
data class ContactState(
    val supportMethods: List<SupportMethod> = emptyList(),
    val selectedMethod: SupportMethod? = null,
    val isLoadingMethods: Boolean = false
)

/**
 * FAQ item data class
 */
data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: FaqCategory,
    val tags: List<String> = emptyList(),
    val helpfulCount: Int = 0,
    val lastUpdated: String
)

/**
 * Device information for bug reports
 */
data class DeviceInfo(
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val buildNumber: String,
    val screenResolution: String,
    val availableMemory: String,
    val storageSpace: String,
    val networkType: String
)

/**
 * Support method information
 */
data class SupportMethod(
    val id: String,
    val name: String,
    val description: String,
    val responseTime: String,
    val availability: String,
    val contactInfo: String,
    val isAvailable: Boolean
)

/**
 * Feedback types
 */
enum class FeedbackType(val displayName: String) {
    GENERAL("General Feedback"),
    FEATURE_REQUEST("Feature Request"),
    IMPROVEMENT("Improvement Suggestion"),
    COMPLIMENT("Compliment"),
    COMPLAINT("Complaint")
}

/**
 * Bug severity levels
 */
enum class BugSeverity(val displayName: String, val description: String) {
    LOW("Low", "Minor issue that doesn't affect core functionality"),
    MEDIUM("Medium", "Issue that affects some functionality but has workarounds"),
    HIGH("High", "Issue that significantly impacts functionality"),
    CRITICAL("Critical", "Issue that prevents app from working or causes data loss")
}

/**
 * FAQ categories
 */
enum class FaqCategory(val displayName: String) {
    GETTING_STARTED("Getting Started"),
    GOALS("Goals & Vision Boards"),
    ACCOUNT("Account & Profile"),
    SETTINGS("Settings & Preferences"),
    SHARING("Sharing & Social"),
    TROUBLESHOOTING("Troubleshooting"),
    PRIVACY("Privacy & Security"),
    BILLING("Billing & Subscriptions")
}

/**
 * Actions available in help and support
 */
data class HelpSupportActions(
    val onSearchQueryChange: (String) -> Unit,
    val onToggleFaqExpansion: (String) -> Unit,
    val onMarkFaqHelpful: (String) -> Unit,
    val onShowFeedbackDialog: () -> Unit,
    val onHideFeedbackDialog: () -> Unit,
    val onFeedbackTypeChange: (FeedbackType) -> Unit,
    val onFeedbackSubjectChange: (String) -> Unit,
    val onFeedbackMessageChange: (String) -> Unit,
    val onToggleUserContext: (Boolean) -> Unit,
    val onSubmitFeedback: () -> Unit,
    val onShowBugReportDialog: () -> Unit,
    val onHideBugReportDialog: () -> Unit,
    val onBugTitleChange: (String) -> Unit,
    val onBugDescriptionChange: (String) -> Unit,
    val onStepsToReproduceChange: (String) -> Unit,
    val onExpectedBehaviorChange: (String) -> Unit,
    val onActualBehaviorChange: (String) -> Unit,
    val onBugSeverityChange: (BugSeverity) -> Unit,
    val onToggleDeviceInfo: (Boolean) -> Unit,
    val onToggleAppLogs: (Boolean) -> Unit,
    val onSubmitBugReport: () -> Unit,
    val onContactSupport: (SupportMethod) -> Unit,
    val onAccessTutorials: () -> Unit,
    val onDismissError: () -> Unit
)
