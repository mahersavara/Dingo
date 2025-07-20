package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.components.GeneralItem
import io.sukhuat.dingo.common.components.NavigableGeneralItem
import io.sukhuat.dingo.common.components.TrailingContent
import io.sukhuat.dingo.common.theme.MountainSunriseTheme

/**
 * Help and Support component for user assistance and feedback
 */
@Composable
fun HelpSupport(
    uiState: HelpSupportUiState,
    actions: HelpSupportActions,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            actions.onDismissError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Actions Section
            QuickActionsSection(actions = actions)

            // FAQ Section
            FaqSection(
                faqState = uiState.faqState,
                actions = actions
            )

            // Contact Support Section
            ContactSupportSection(
                contactState = uiState.contactState,
                actions = actions
            )
        }

        // Feedback Dialog
        if (uiState.feedbackState.showFeedbackDialog) {
            FeedbackDialog(
                feedbackState = uiState.feedbackState,
                actions = actions
            )
        }

        // Bug Report Dialog
        if (uiState.bugReportState.showBugReportDialog) {
            BugReportDialog(
                bugReportState = uiState.bugReportState,
                actions = actions
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun QuickActionsSection(
    actions: HelpSupportActions
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NavigableGeneralItem(
            title = "Interactive Tutorials",
            description = "Learn how to use key features",
            leadingIcon = Icons.Default.School,
            onClick = actions.onAccessTutorials
        )

        NavigableGeneralItem(
            title = "Send Feedback",
            description = "Share your thoughts and suggestions",
            leadingIcon = Icons.Default.Feedback,
            onClick = actions.onShowFeedbackDialog
        )

        NavigableGeneralItem(
            title = "Report a Bug",
            description = "Help us fix issues you encounter",
            leadingIcon = Icons.Default.BugReport,
            onClick = actions.onShowBugReportDialog
        )
    }
}

@Composable
private fun FaqSection(
    faqState: FaqState,
    actions: HelpSupportActions
) {
    Column {
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = faqState.searchQuery,
            onValueChange = actions.onSearchQueryChange,
            label = { Text("Search FAQs") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = if (faqState.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // FAQ List
        if (faqState.filteredFaqs.isEmpty() && faqState.isSearching) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No FAQs found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try different search terms or browse all FAQs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            faqState.filteredFaqs.forEach { faq ->
                FaqItem(
                    faq = faq,
                    isExpanded = faqState.expandedFaqIds.contains(faq.id),
                    onToggleExpansion = { actions.onToggleFaqExpansion(faq.id) },
                    onMarkHelpful = { actions.onMarkFaqHelpful(faq.id) }
                )
            }
        }
    }
}

@Composable
private fun FaqItem(
    faq: FaqItem,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onMarkHelpful: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleExpansion) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Category and Last Updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Updated ${faq.lastUpdated}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Answer (Expandable)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Helpful Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onMarkHelpful
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Helpful (${faq.helpfulCount})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSupportSection(
    contactState: ContactState,
    actions: HelpSupportActions
) {
    Column {
        Text(
            text = "Contact Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (contactState.isLoadingMethods) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            contactState.supportMethods.forEach { method ->
                SupportMethodItem(
                    method = method,
                    onClick = { actions.onContactSupport(method) }
                )
            }
        }
    }
}

@Composable
private fun SupportMethodItem(
    method: SupportMethod,
    onClick: () -> Unit
) {
    val icon = when (method.id) {
        "email" -> Icons.Default.Email
        "chat" -> Icons.Default.Chat
        "community" -> Icons.Default.Forum
        "phone" -> Icons.Default.Phone
        else -> Icons.Default.ContactSupport
    }

    GeneralItem(
        title = method.name,
        description = "${method.description} • ${method.responseTime}",
        leadingIcon = icon,
        trailingContent = if (method.isAvailable) {
            TrailingContent.Text(
                text = method.availability,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            TrailingContent.Text(
                text = "Premium",
                color = MaterialTheme.colorScheme.outline
            )
        },
        enabled = method.isAvailable,
        onClick = if (method.isAvailable) onClick else null
    )
}

@Composable
private fun FeedbackDialog(
    feedbackState: FeedbackState,
    actions: HelpSupportActions
) {
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = actions.onHideFeedbackDialog,
        title = {
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Feedback Type Dropdown
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = feedbackState.feedbackType.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Feedback Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        FeedbackType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    actions.onFeedbackTypeChange(type)
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Field
                OutlinedTextField(
                    value = feedbackState.subject,
                    onValueChange = actions.onFeedbackSubjectChange,
                    label = { Text("Subject") },
                    isError = feedbackState.validationErrors.containsKey("subject"),
                    supportingText = feedbackState.validationErrors["subject"]?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Message Field
                OutlinedTextField(
                    value = feedbackState.message,
                    onValueChange = actions.onFeedbackMessageChange,
                    label = { Text("Message") },
                    isError = feedbackState.validationErrors.containsKey("message"),
                    supportingText = feedbackState.validationErrors["message"]?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Include User Context Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = feedbackState.includeUserContext,
                        onCheckedChange = actions.onToggleUserContext
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Include user context (helpful for our team)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Success Message
                if (feedbackState.submissionSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Feedback submitted successfully! Thank you for helping us improve.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onSubmitFeedback,
                enabled = !feedbackState.isSubmitting && !feedbackState.submissionSuccess
            ) {
                if (feedbackState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (feedbackState.submissionSuccess) "Submitted" else "Send Feedback")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHideFeedbackDialog,
                enabled = !feedbackState.isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BugReportDialog(
    bugReportState: BugReportState,
    actions: HelpSupportActions
) {
    var severityDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = actions.onHideBugReportDialog,
        title = {
            Text(
                text = "Report a Bug",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bug Title
                OutlinedTextField(
                    value = bugReportState.bugTitle,
                    onValueChange = actions.onBugTitleChange,
                    label = { Text("Bug Title") },
                    isError = bugReportState.validationErrors.containsKey("title"),
                    supportingText = bugReportState.validationErrors["title"]?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Bug Description
                OutlinedTextField(
                    value = bugReportState.bugDescription,
                    onValueChange = actions.onBugDescriptionChange,
                    label = { Text("Description") },
                    isError = bugReportState.validationErrors.containsKey("description"),
                    supportingText = bugReportState.validationErrors["description"]?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Severity Dropdown
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = severityDropdownExpanded,
                    onExpandedChange = { severityDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${bugReportState.severity.displayName} - ${bugReportState.severity.description}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Severity") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = severityDropdownExpanded,
                        onDismissRequest = { severityDropdownExpanded = false }
                    ) {
                        BugSeverity.values().forEach { severity ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = severity.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = severity.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    actions.onBugSeverityChange(severity)
                                    severityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Include Device Info Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = bugReportState.includeDeviceInfo,
                        onCheckedChange = actions.onToggleDeviceInfo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Include device information",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Include App Logs Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = bugReportState.includeAppLogs,
                        onCheckedChange = actions.onToggleAppLogs
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Include app logs (helps with debugging)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Device Info Preview
                if (bugReportState.includeDeviceInfo && bugReportState.deviceInfo != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Device Information to Include:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• ${bugReportState.deviceInfo.deviceModel}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• ${bugReportState.deviceInfo.osVersion}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• App ${bugReportState.deviceInfo.appVersion}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Success Message
                if (bugReportState.submissionSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Bug report submitted successfully! We'll investigate this issue.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onSubmitBugReport,
                enabled = !bugReportState.isSubmitting && !bugReportState.submissionSuccess
            ) {
                if (bugReportState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (bugReportState.submissionSuccess) "Submitted" else "Submit Report")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHideBugReportDialog,
                enabled = !bugReportState.isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HelpSupportPreview() {
    MountainSunriseTheme {
        HelpSupport(
            uiState = HelpSupportUiState(
                faqState = FaqState(
                    filteredFaqs = listOf(
                        FaqItem(
                            id = "1",
                            question = "How do I create my first vision board?",
                            answer = "To create your first vision board, tap the '+' button on the main screen...",
                            category = FaqCategory.GETTING_STARTED,
                            helpfulCount = 45,
                            lastUpdated = "2 days ago"
                        )
                    )
                ),
                contactState = ContactState(
                    supportMethods = listOf(
                        SupportMethod(
                            id = "email",
                            name = "Email Support",
                            description = "Send us an email for detailed assistance",
                            responseTime = "Within 24 hours",
                            availability = "24/7",
                            contactInfo = "support@dingo-app.com",
                            isAvailable = true
                        )
                    )
                )
            ),
            actions = HelpSupportActions(
                onSearchQueryChange = {},
                onToggleFaqExpansion = {},
                onMarkFaqHelpful = {},
                onShowFeedbackDialog = {},
                onHideFeedbackDialog = {},
                onFeedbackTypeChange = {},
                onFeedbackSubjectChange = {},
                onFeedbackMessageChange = {},
                onToggleUserContext = {},
                onSubmitFeedback = {},
                onShowBugReportDialog = {},
                onHideBugReportDialog = {},
                onBugTitleChange = {},
                onBugDescriptionChange = {},
                onStepsToReproduceChange = {},
                onExpectedBehaviorChange = {},
                onActualBehaviorChange = {},
                onBugSeverityChange = {},
                onToggleDeviceInfo = {},
                onToggleAppLogs = {},
                onSubmitBugReport = {},
                onContactSupport = {},
                onAccessTutorials = {},
                onDismissError = {}
            )
        )
    }
}
