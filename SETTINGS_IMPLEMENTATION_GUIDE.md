# 🎛️ Settings Feature Implementation Guide

This guide explains the comprehensive settings system implemented for the Dingo app, including notification management, user preferences, and the reusable GeneralItem component.

## 📋 Overview

The settings system includes:
- **User Preferences Management**: Sound, vibration, notifications, appearance, privacy settings
- **Notification System**: Smart notification handling with permission management
- **Reusable UI Components**: Flexible GeneralItem component for consistent settings UI
- **Clean Architecture**: Proper separation of concerns with use cases and repositories

## 🏗️ Architecture

### Domain Layer
```
domain/
├── model/
│   └── UserPreferences.kt                    # User preferences data model
├── repository/
│   └── UserPreferencesRepository.kt          # Repository interface
└── usecase/preferences/
    ├── GetUserPreferencesUseCase.kt          # Get all preferences
    ├── UpdatePreferencesUseCase.kt           # Update individual preferences
    ├── GetNotificationPreferencesUseCase.kt  # Get notification-specific preferences
    ├── GetAudioFeedbackPreferencesUseCase.kt # Get audio/feedback preferences
    ├── ToggleNotificationSettingsUseCase.kt  # Smart notification toggling
    └── ManageNotificationPermissionsUseCase.kt # Permission management
```

### Data Layer
```
data/
├── preferences/
│   └── UserPreferencesDataStore.kt          # DataStore implementation
├── repository/
│   └── UserPreferencesRepositoryImpl.kt     # Repository implementation
└── notification/
    ├── NotificationService.kt               # Notification display service
    ├── NotificationScheduler.kt             # Recurring notification scheduling
    └── WeeklyReminderReceiver.kt           # Broadcast receiver for reminders
```

### UI Layer
```
ui/screens/settings/
├── SettingsScreen.kt                        # Main settings screen
├── SettingsViewModel.kt                     # Settings view model
├── SettingsUiState.kt                      # UI state definitions
└── SettingsIntegrationExample.kt           # Integration examples
```

### Common Components
```
common/components/
└── GeneralItem.kt                          # Reusable settings item component
```

## 🎨 GeneralItem Component

The `GeneralItem` is a highly flexible component for creating consistent settings UI:

### Basic Usage
```kotlin
GeneralItem(
    title = "Sound Effects",
    description = "Play sounds for interactions",
    leadingIcon = Icons.Default.VolumeUp,
    trailingContent = TrailingContent.Switch(
        checked = soundEnabled,
        onCheckedChange = { enabled -> toggleSound(enabled) }
    )
)
```

### Supported Trailing Content Types
- **Switch**: Toggle switches for boolean settings
- **RadioButton**: Single selection from options
- **Checkbox**: Multi-selection checkboxes
- **Arrow**: Navigation indicators
- **Text**: Simple text display
- **Badge**: Highlighted status indicators
- **Icon**: Clickable icons
- **Custom**: Any custom composable content

### Convenience Functions
```kotlin
// Toggle item (switch behavior)
ToggleGeneralItem(
    title = "Vibration",
    description = "Haptic feedback for interactions",
    leadingIcon = Icons.Default.Vibration,
    checked = vibrationEnabled,
    onCheckedChange = ::toggleVibration
)

// Navigable item (arrow behavior)
NavigableGeneralItem(
    title = "Language",
    description = "Current: English",
    leadingIcon = Icons.Default.Language,
    onClick = ::openLanguageSelector
)

// Selectable item (radio button behavior)
SelectableGeneralItem(
    title = "Dark Mode",
    description = "Use dark theme",
    leadingIcon = Icons.Default.DarkMode,
    selected = darkModeEnabled,
    onClick = ::toggleDarkMode
)
```

## 🔔 Notification System

### Features
- **Permission Management**: Automatic permission checking and user guidance
- **Smart Scheduling**: Weekly reminder scheduling with AlarmManager
- **Multiple Channels**: Separate channels for different notification types
- **Preference Integration**: Respects user notification preferences

### Usage in ViewModels
```kotlin
@HiltViewModel
class YourViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {
    
    fun onGoalCompleted(goalTitle: String) {
        viewModelScope.launch {
            notificationService.showGoalCompletionNotification(goalTitle)
        }
    }
}
```

### Permission Handling
```kotlin
// Check permission status
val permissionStatus = manageNotificationPermissionsUseCase.getNotificationPermissionStatus()

when (permissionStatus) {
    NotificationPermissionStatus.GRANTED -> {
        // Notifications are fully enabled
    }
    NotificationPermissionStatus.PERMISSION_DENIED -> {
        // Need to request POST_NOTIFICATIONS permission (Android 13+)
    }
    NotificationPermissionStatus.NOTIFICATIONS_DISABLED -> {
        // User disabled notifications in system settings
    }
}
```

## ⚙️ Settings Integration

### 1. In Your Screen Composables
```kotlin
@Composable
fun YourScreen(
    viewModel: YourViewModel = hiltViewModel()
) {
    val preferences by viewModel.userPreferences.collectAsState()
    
    // Use preferences to control app behavior
    if (preferences.soundEnabled) {
        // Play sounds
    }
    
    if (preferences.vibrationEnabled) {
        // Provide haptic feedback
    }
}
```

### 2. In Your ViewModels
```kotlin
@HiltViewModel
class YourViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {
    
    val userPreferences = getUserPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )
}
```

### 3. Navigation Integration
```kotlin
// In your navigation setup
composable("settings") {
    SettingsScreen(
        onNavigateBack = { navController.popBackStack() },
        onLanguageChange = { languageCode ->
            // Handle language change
            // This might require activity recreation
        }
    )
}
```

## 🎯 User Preferences Model

```kotlin
data class UserPreferences(
    // Audio & Feedback
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    
    // Notifications
    val notificationsEnabled: Boolean = true,
    val weeklyRemindersEnabled: Boolean = true,
    val goalCompletionNotifications: Boolean = true,
    
    // Appearance
    val darkModeEnabled: Boolean = false,
    val languageCode: String = "en",
    
    // Privacy & Data
    val autoBackupEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true
)
```

## 📱 Required Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.VIBRATE" />
```

Register the broadcast receiver:
```xml
<receiver
    android:name="io.sukhuat.dingo.data.notification.WeeklyReminderReceiver"
    android:enabled="true"
    android:exported="false" />
```

## 🔧 Dependency Injection Setup

The settings system is fully integrated with Hilt DI:

### Repository Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideNotificationService(
            @ApplicationContext context: Context,
            userPreferencesDataStore: UserPreferencesDataStore
        ): NotificationService = NotificationService(context, userPreferencesDataStore)
    }
}
```

## 🎨 UI Examples

### Settings Screen Sections
The settings screen is organized into logical sections:

1. **Audio & Feedback**
   - Sound effects toggle
   - Vibration toggle

2. **Notifications**
   - Master notifications toggle
   - Weekly reminders toggle
   - Goal completion notifications toggle

3. **Appearance**
   - Dark mode toggle
   - Language selection

4. **Privacy & Data**
   - Auto backup toggle
   - Analytics toggle

5. **About**
   - App version display
   - Reset settings option

### Custom Settings Items
```kotlin
// Badge example
GeneralItem(
    title = "Weekly Reminders",
    description = "Get reminded about your goals",
    leadingIcon = Icons.Default.Notifications,
    trailingContent = TrailingContent.Badge(
        text = if (enabled) "ON" else "OFF",
        backgroundColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    )
)

// Custom trailing content
GeneralItem(
    title = "Notification Status",
    description = "Current permission status",
    leadingIcon = Icons.Default.Notifications,
    trailingContent = TrailingContent.Custom {
        when (permissionStatus) {
            NotificationPermissionStatus.GRANTED -> {
                Icon(Icons.Default.Check, tint = Color.Green)
            }
            else -> {
                Icon(Icons.Default.Warning, tint = Color.Orange)
            }
        }
    }
)
```

## 🚀 Getting Started

1. **Add the settings screen to your navigation**
2. **Inject SettingsViewModel where needed**
3. **Use GeneralItem components for consistent UI**
4. **Handle notification permissions appropriately**
5. **Integrate user preferences into your app logic**

## 🔄 Best Practices

1. **Always check notification permissions** before showing notifications
2. **Use the smart toggle use cases** for dependent settings
3. **Provide clear descriptions** for each setting
4. **Handle errors gracefully** with user-friendly messages
5. **Test on different Android versions** for permission handling
6. **Use the convenience functions** (ToggleGeneralItem, NavigableGeneralItem) for common patterns

## 🎉 Features Included

✅ **Complete Settings UI** with organized sections  
✅ **Notification Management** with permission handling  
✅ **Reusable Components** for consistent design  
✅ **Smart Dependencies** between related settings  
✅ **Weekly Reminders** with scheduling  
✅ **DataStore Integration** for persistent storage  
✅ **Clean Architecture** with proper separation  
✅ **Comprehensive Examples** and documentation  

The settings system is now ready to use and can be easily extended with additional preferences as your app grows!