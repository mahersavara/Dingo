package io.sukhuat.dingo.ui.screens.profile.components

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HelpSupport component
 */
@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpSupportUiState())
    val uiState: StateFlow<HelpSupportUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Load initial data for help and support
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(isLoading = true)

                // Load FAQ data
                val faqs = loadFaqData()
                val supportMethods = loadSupportMethods()
                val deviceInfo = collectDeviceInfo()

                val updatedState = _uiState.value
                _uiState.value = updatedState.copy(
                    isLoading = false,
                    faqState = updatedState.faqState.copy(filteredFaqs = faqs),
                    contactState = updatedState.contactState.copy(supportMethods = supportMethods),
                    bugReportState = updatedState.bugReportState.copy(deviceInfo = deviceInfo)
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "Failed to load help data. Please try again."
                )
            }
        }
    }

    /**
     * Update FAQ search query and filter results
     */
    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        val allFaqs = loadFaqData() // In real app, this would be cached

        val filteredFaqs = if (query.isBlank()) {
            allFaqs
        } else {
            allFaqs.filter { faq ->
                faq.question.contains(query, ignoreCase = true) ||
                    faq.answer.contains(query, ignoreCase = true) ||
                    faq.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        _uiState.value = currentState.copy(
            faqState = currentState.faqState.copy(
                searchQuery = query,
                filteredFaqs = filteredFaqs,
                isSearching = query.isNotBlank()
            )
        )
    }

    /**
     * Toggle FAQ expansion
     */
    fun toggleFaqExpansion(faqId: String) {
        val currentState = _uiState.value
        val expandedIds = currentState.faqState.expandedFaqIds.toMutableSet()

        if (expandedIds.contains(faqId)) {
            expandedIds.remove(faqId)
        } else {
            expandedIds.add(faqId)
        }

        _uiState.value = currentState.copy(
            faqState = currentState.faqState.copy(expandedFaqIds = expandedIds)
        )
    }

    /**
     * Mark FAQ as helpful
     */
    fun markFaqHelpful(faqId: String) {
        // In a real app, this would send analytics or update server data
        viewModelScope.launch {
            try {
                // Simulate API call
                kotlinx.coroutines.delay(500)

                // Update local state to show feedback was recorded
                val currentState = _uiState.value
                val updatedFaqs = currentState.faqState.filteredFaqs.map { faq ->
                    if (faq.id == faqId) {
                        faq.copy(helpfulCount = faq.helpfulCount + 1)
                    } else {
                        faq
                    }
                }

                _uiState.value = currentState.copy(
                    faqState = currentState.faqState.copy(filteredFaqs = updatedFaqs)
                )
            } catch (error: Exception) {
                // Handle error silently for this non-critical operation
            }
        }
    }

    /**
     * Show feedback dialog
     */
    fun showFeedbackDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(
                showFeedbackDialog = true,
                // Reset form
                subject = "",
                message = "",
                feedbackType = FeedbackType.GENERAL,
                validationErrors = emptyMap(),
                submissionSuccess = false
            )
        )
    }

    /**
     * Hide feedback dialog
     */
    fun hideFeedbackDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(showFeedbackDialog = false)
        )
    }

    /**
     * Update feedback type
     */
    fun updateFeedbackType(type: FeedbackType) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(feedbackType = type)
        )
    }

    /**
     * Update feedback subject
     */
    fun updateFeedbackSubject(subject: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(
                subject = subject,
                validationErrors = currentState.feedbackState.validationErrors - "subject"
            )
        )
    }

    /**
     * Update feedback message
     */
    fun updateFeedbackMessage(message: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(
                message = message,
                validationErrors = currentState.feedbackState.validationErrors - "message"
            )
        )
    }

    /**
     * Toggle user context inclusion
     */
    fun toggleUserContext(include: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            feedbackState = currentState.feedbackState.copy(includeUserContext = include)
        )
    }

    /**
     * Submit feedback
     */
    fun submitFeedback() {
        val feedbackState = _uiState.value.feedbackState

        // Validate inputs
        val validationErrors = validateFeedbackInputs(feedbackState.subject, feedbackState.message)
        if (validationErrors.isNotEmpty()) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                feedbackState = currentState.feedbackState.copy(validationErrors = validationErrors)
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    feedbackState = currentState.feedbackState.copy(isSubmitting = true)
                )

                // Simulate API call to submit feedback
                kotlinx.coroutines.delay(2000)

                // Success
                val successState = _uiState.value
                _uiState.value = successState.copy(
                    feedbackState = successState.feedbackState.copy(
                        isSubmitting = false,
                        submissionSuccess = true
                    )
                )

                // Auto-close dialog after success
                kotlinx.coroutines.delay(1500)
                hideFeedbackDialog()
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    feedbackState = currentState.feedbackState.copy(isSubmitting = false),
                    error = "Failed to submit feedback. Please try again."
                )
            }
        }
    }

    /**
     * Show bug report dialog
     */
    fun showBugReportDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(
                showBugReportDialog = true,
                // Reset form
                bugTitle = "",
                bugDescription = "",
                stepsToReproduce = "",
                expectedBehavior = "",
                actualBehavior = "",
                severity = BugSeverity.MEDIUM,
                validationErrors = emptyMap(),
                submissionSuccess = false
            )
        )
    }

    /**
     * Hide bug report dialog
     */
    fun hideBugReportDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(showBugReportDialog = false)
        )
    }

    /**
     * Update bug title
     */
    fun updateBugTitle(title: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(
                bugTitle = title,
                validationErrors = currentState.bugReportState.validationErrors - "title"
            )
        )
    }

    /**
     * Update bug description
     */
    fun updateBugDescription(description: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(
                bugDescription = description,
                validationErrors = currentState.bugReportState.validationErrors - "description"
            )
        )
    }

    /**
     * Update steps to reproduce
     */
    fun updateStepsToReproduce(steps: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(stepsToReproduce = steps)
        )
    }

    /**
     * Update expected behavior
     */
    fun updateExpectedBehavior(behavior: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(expectedBehavior = behavior)
        )
    }

    /**
     * Update actual behavior
     */
    fun updateActualBehavior(behavior: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(actualBehavior = behavior)
        )
    }

    /**
     * Update bug severity
     */
    fun updateBugSeverity(severity: BugSeverity) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(severity = severity)
        )
    }

    /**
     * Toggle device info inclusion
     */
    fun toggleDeviceInfo(include: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(includeDeviceInfo = include)
        )
    }

    /**
     * Toggle app logs inclusion
     */
    fun toggleAppLogs(include: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            bugReportState = currentState.bugReportState.copy(includeAppLogs = include)
        )
    }

    /**
     * Submit bug report
     */
    fun submitBugReport() {
        val bugState = _uiState.value.bugReportState

        // Validate inputs
        val validationErrors = validateBugReportInputs(bugState.bugTitle, bugState.bugDescription)
        if (validationErrors.isNotEmpty()) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                bugReportState = currentState.bugReportState.copy(validationErrors = validationErrors)
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    bugReportState = currentState.bugReportState.copy(isSubmitting = true)
                )

                // Simulate API call to submit bug report
                kotlinx.coroutines.delay(2000)

                // Success
                val successState = _uiState.value
                _uiState.value = successState.copy(
                    bugReportState = successState.bugReportState.copy(
                        isSubmitting = false,
                        submissionSuccess = true
                    )
                )

                // Auto-close dialog after success
                kotlinx.coroutines.delay(1500)
                hideBugReportDialog()
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    bugReportState = currentState.bugReportState.copy(isSubmitting = false),
                    error = "Failed to submit bug report. Please try again."
                )
            }
        }
    }

    /**
     * Contact support method
     */
    fun contactSupport(method: SupportMethod) {
        // In a real app, this would open email client, web browser, or chat
        viewModelScope.launch {
            try {
                // Simulate opening external app or service
                kotlinx.coroutines.delay(500)

                // For now, just show a success message
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    error = "Opening ${method.name}..."
                )

                // Clear message after delay
                kotlinx.coroutines.delay(2000)
                dismissError()
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    error = "Failed to open ${method.name}. Please try again."
                )
            }
        }
    }

    /**
     * Access tutorials
     */
    fun accessTutorials() {
        // In a real app, this would navigate to tutorials screen
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                error = "Opening tutorials..."
            )

            kotlinx.coroutines.delay(2000)
            dismissError()
        }
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(error = null)
    }

    /**
     * Get actions for the UI
     */
    fun getActions(): HelpSupportActions {
        return HelpSupportActions(
            onSearchQueryChange = ::updateSearchQuery,
            onToggleFaqExpansion = ::toggleFaqExpansion,
            onMarkFaqHelpful = ::markFaqHelpful,
            onShowFeedbackDialog = ::showFeedbackDialog,
            onHideFeedbackDialog = ::hideFeedbackDialog,
            onFeedbackTypeChange = ::updateFeedbackType,
            onFeedbackSubjectChange = ::updateFeedbackSubject,
            onFeedbackMessageChange = ::updateFeedbackMessage,
            onToggleUserContext = ::toggleUserContext,
            onSubmitFeedback = ::submitFeedback,
            onShowBugReportDialog = ::showBugReportDialog,
            onHideBugReportDialog = ::hideBugReportDialog,
            onBugTitleChange = ::updateBugTitle,
            onBugDescriptionChange = ::updateBugDescription,
            onStepsToReproduceChange = ::updateStepsToReproduce,
            onExpectedBehaviorChange = ::updateExpectedBehavior,
            onActualBehaviorChange = ::updateActualBehavior,
            onBugSeverityChange = ::updateBugSeverity,
            onToggleDeviceInfo = ::toggleDeviceInfo,
            onToggleAppLogs = ::toggleAppLogs,
            onSubmitBugReport = ::submitBugReport,
            onContactSupport = ::contactSupport,
            onAccessTutorials = ::accessTutorials,
            onDismissError = ::dismissError
        )
    }

    /**
     * Load FAQ data (mock data for now)
     */
    private fun loadFaqData(): List<FaqItem> {
        return listOf(
            FaqItem(
                id = "1",
                question = "How do I create my first vision board?",
                answer = "To create your first vision board, tap the '+' button on the main screen, choose 'New Vision Board', and follow the guided setup. You can add images, text, and goals to visualize your dreams.",
                category = FaqCategory.GETTING_STARTED,
                tags = listOf("vision board", "create", "getting started"),
                helpfulCount = 45,
                lastUpdated = "2 days ago"
            ),
            FaqItem(
                id = "2",
                question = "How do I set and track goals?",
                answer = "Goals can be added to any vision board. Tap on a vision board, then tap 'Add Goal'. Set a title, description, target date, and track your progress with regular check-ins.",
                category = FaqCategory.GOALS,
                tags = listOf("goals", "tracking", "progress"),
                helpfulCount = 32,
                lastUpdated = "1 week ago"
            ),
            FaqItem(
                id = "3",
                question = "Can I share my vision boards with others?",
                answer = "Yes! You can share your vision boards as images or links. Go to your vision board, tap the share icon, and choose how you'd like to share - social media, email, or generate a shareable link.",
                category = FaqCategory.SHARING,
                tags = listOf("sharing", "social", "export"),
                helpfulCount = 28,
                lastUpdated = "3 days ago"
            ),
            FaqItem(
                id = "4",
                question = "How do I change my profile picture?",
                answer = "Go to your Profile tab, tap on your current profile picture, and select 'Change Photo'. You can choose from your gallery or take a new photo.",
                category = FaqCategory.ACCOUNT,
                tags = listOf("profile", "picture", "avatar"),
                helpfulCount = 19,
                lastUpdated = "5 days ago"
            ),
            FaqItem(
                id = "5",
                question = "Why am I not receiving notifications?",
                answer = "Check your notification settings in the app and your device settings. Make sure notifications are enabled for Dingo in your device's Settings > Apps > Dingo > Notifications.",
                category = FaqCategory.TROUBLESHOOTING,
                tags = listOf("notifications", "settings", "troubleshooting"),
                helpfulCount = 67,
                lastUpdated = "1 day ago"
            ),
            FaqItem(
                id = "6",
                question = "Is my data secure and private?",
                answer = "Yes, we take your privacy seriously. Your data is encrypted and stored securely. We never share your personal information with third parties without your consent. See our Privacy Policy for details.",
                category = FaqCategory.PRIVACY,
                tags = listOf("privacy", "security", "data"),
                helpfulCount = 41,
                lastUpdated = "1 week ago"
            )
        )
    }

    /**
     * Load support methods (mock data for now)
     */
    private fun loadSupportMethods(): List<SupportMethod> {
        return listOf(
            SupportMethod(
                id = "email",
                name = "Email Support",
                description = "Send us an email for detailed assistance",
                responseTime = "Within 24 hours",
                availability = "24/7",
                contactInfo = "support@dingo-app.com",
                isAvailable = true
            ),
            SupportMethod(
                id = "chat",
                name = "Live Chat",
                description = "Chat with our support team in real-time",
                responseTime = "Usually within 5 minutes",
                availability = "Mon-Fri, 9 AM - 6 PM EST",
                contactInfo = "Available in app",
                isAvailable = true
            ),
            SupportMethod(
                id = "community",
                name = "Community Forum",
                description = "Get help from other users and our team",
                responseTime = "Varies",
                availability = "24/7",
                contactInfo = "community.dingo-app.com",
                isAvailable = true
            ),
            SupportMethod(
                id = "phone",
                name = "Phone Support",
                description = "Speak directly with our support team",
                responseTime = "Immediate",
                availability = "Mon-Fri, 10 AM - 4 PM EST",
                contactInfo = "+1 (555) 123-4567",
                isAvailable = false // Premium feature
            )
        )
    }

    /**
     * Collect device information for bug reports
     */
    private fun collectDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = "1.0.0", // This would come from BuildConfig
            buildNumber = "100", // This would come from BuildConfig
            screenResolution = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}",
            availableMemory = "Available", // This would be calculated
            storageSpace = "Available", // This would be calculated
            networkType = "WiFi" // This would be detected
        )
    }

    /**
     * Validate feedback inputs
     */
    private fun validateFeedbackInputs(subject: String, message: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (subject.isBlank()) {
            errors["subject"] = "Subject is required"
        } else if (subject.length < 5) {
            errors["subject"] = "Subject must be at least 5 characters"
        }

        if (message.isBlank()) {
            errors["message"] = "Message is required"
        } else if (message.length < 10) {
            errors["message"] = "Message must be at least 10 characters"
        }

        return errors
    }

    /**
     * Validate bug report inputs
     */
    private fun validateBugReportInputs(title: String, description: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (title.isBlank()) {
            errors["title"] = "Bug title is required"
        } else if (title.length < 5) {
            errors["title"] = "Title must be at least 5 characters"
        }

        if (description.isBlank()) {
            errors["description"] = "Bug description is required"
        } else if (description.length < 20) {
            errors["description"] = "Description must be at least 20 characters"
        }

        return errors
    }
}
