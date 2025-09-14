# Dingo - Project Index Documentation

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Module Architecture](#module-architecture)
4. [Core Components](#core-components)
5. [Features & Functionality](#features--functionality)
6. [Technology Stack](#technology-stack)
7. [Development Workflow](#development-workflow)
8. [API Reference](#api-reference)
9. [Testing Structure](#testing-structure)
10. [Resources & References](#resources--references)

---

## 🎯 Project Overview

**Dingo** is a vision board Bingo app that gamifies goal achievement. Users create yearly goals in a Bingo-card format and get rewarded with notifications and animations when completing them.

### Key Highlights
- **Architecture**: Clean Architecture with multi-module structure
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Theme**: Mountain Sunrise aesthetic with custom color palette
- **Localization**: English/Vietnamese support

---

## 📁 Project Structure

### Root Directory Structure
```
Dingo/
├── app/                    # Main application module
├── ui/                     # Presentation layer
├── data/                   # Data access layer
├── domain/                 # Business logic layer
├── common/                 # Shared components & utilities
├── docs/                   # Project documentation
├── tasks/                  # Development task tracking
├── .kiro/                  # Kiro specifications
├── .serena/                # Serena AI memories
└── build files & configs
```

### Module Dependencies
```
:app → :ui, :data, :domain, :common
:ui → :data, :domain, :common
:data → :domain, :common
:domain → (no dependencies)
:common → (base components)
```

---

## 🏗️ Module Architecture

### `:app` Module
**Main application module with navigation and DI setup**

#### Key Components:
- **MainActivity.kt** - Main entry point
- **Navigation/** - App navigation setup
- **DI Modules** - Hilt dependency injection
- **Widget System** - Home screen widgets

#### Widget Implementation:
- **WeeklyGoalWidget.kt** - Main widget component
- **WeeklyGoalWidget2x3.kt** / **WeeklyGoalWidget3x2.kt** - Size variants
- **WidgetDataLoader.kt** - Data loading for widgets
- **WidgetUpdateScheduler.kt** - Automatic widget updates

### `:ui` Module
**Presentation layer with Composables and ViewModels**

#### Screen Structure:
```
ui/screens/
├── auth/                   # Authentication screens
│   ├── AuthScreen.kt
│   ├── AuthViewModel.kt
│   ├── EnhancedRegistrationScreen.kt
│   └── ForgotPasswordScreen.kt
├── home/                   # Home/main screens
│   ├── HomeScreen.kt
│   ├── HomeViewModel.kt
│   └── HomeUiState.kt
├── profile/                # User profile screens
│   ├── ProfileScreen.kt
│   ├── ProfileViewModel.kt
│   └── components/         # Profile sub-components
├── settings/               # Settings screens
│   ├── SettingsScreen.kt
│   └── SettingsViewModel.kt
├── yearplanner/            # Year planning feature
│   ├── YearPlannerScreen.kt
│   └── YearPlannerViewModel.kt
└── splash/                 # App launch screen
    ├── SplashScreen.kt
    └── SplashViewModel.kt
```

#### UI Components:
- **DragDropGrid.kt** - Main goal grid with drag & drop
- **GoalCell.kt** - Individual goal display
- **CachedAsyncImage.kt** - Optimized image loading
- **TouchInteractionManager.kt** - Touch gesture handling

### `:data` Module
**Data layer with repositories and Firebase services**

#### Repository Pattern:
```
data/repository/
├── GoalRepositoryImpl.kt
├── UserProfileRepositoryImpl.kt
├── ProfileStatisticsRepositoryImpl.kt
└── SharingRepositoryImpl.kt
```

#### Firebase Services:
```
data/
├── auth/                   # Authentication
│   ├── GoogleAuthService.kt
│   └── EmailVerificationManager.kt
├── remote/                 # Firebase services
│   ├── FirebaseGoalService.kt
│   └── FirebaseStorageService.kt
├── notification/           # Push notifications
│   ├── NotificationService.kt
│   └── NotificationScheduler.kt
└── di/                     # Dependency injection modules
```

### `:domain` Module
**Business logic with use cases and models (no dependencies)**

#### Use Cases by Feature:
```
domain/usecase/
├── auth/                   # Authentication
│   ├── SignInUseCase.kt
│   ├── SignUpUseCase.kt
│   └── GetAuthStatusUseCase.kt
├── goal/                   # Goal management
│   ├── CreateGoalUseCase.kt
│   ├── UpdateGoalUseCase.kt
│   └── DeleteGoalUseCase.kt
├── profile/                # User profile
│   ├── UpdateProfileUseCase.kt
│   └── ManageProfileImageUseCase.kt
└── preferences/            # User preferences
    ├── GetUserPreferencesUseCase.kt
    └── UpdatePreferencesUseCase.kt
```

#### Domain Models:
```
domain/model/
├── Goal.kt                 # Core goal entity
├── UserPreferences.kt      # User settings
├── ProfileError.kt         # Error handling
└── yearplanner/           # Year planning models
    ├── YearPlan.kt
    └── MonthData.kt
```

### `:common` Module
**Shared UI components and utilities**

#### Theme System:
```
common/theme/
├── Theme.kt               # Main theme configuration
├── Color.kt               # Mountain Sunrise palette
├── Typography.kt          # Font definitions
└── Dimensions.kt          # Layout dimensions
```

#### Reusable Components:
```
common/components/
├── DingoButton.kt         # Styled button component
├── DingoCard.kt           # Styled card component
├── DingoTextField.kt      # Styled text input
├── DingoDialog.kt         # Modal dialogs
├── LoadingIndicator.kt    # Loading states
├── ConfettiAnimation.kt   # Goal completion animation
└── GoalCompletionCelebration.kt
```

---

## 🧩 Core Components

### State Management Architecture

#### ViewModel Pattern
```kotlin
// Example: HomeViewModel.kt:82
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
```

#### UI State Management
```kotlin
// Example: HomeUiState.kt:15
data class HomeUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showConfetti: Boolean = false
)
```

### Dependency Injection (Hilt)

#### Module Structure:
- **AuthModule.kt** - Authentication services
- **RepositoryModule.kt** - Repository implementations
- **UseCaseModule.kt** - Business logic use cases
- **FirebaseModule.kt** - Firebase services

### Navigation System
```kotlin
// Navigation implemented with Compose Navigation
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object YearPlanner : Screen("year_planner")
}
```

---

## ✨ Features & Functionality

### 🎯 Core Features

#### Goal Management
- **CRUD Operations** - Create, read, update, delete goals
- **Status Tracking** - Active, Completed, Failed, Archived
- **Rich Media Support** - Images, GIFs, stickers
- **Drag & Drop Reordering** - With haptic feedback
- **Firebase Sync** - Real-time synchronization

#### User Experience
- **Mountain Sunrise Theme** - Custom Material 3 implementation
- **Confetti Animations** - Goal completion celebrations
- **Haptic Feedback** - Touch interaction enhancement
- **Sound Effects** - Audio feedback for actions
- **Accessibility Support** - Screen reader compatible

#### Authentication System
- **Email/Password** - Firebase Auth integration
- **Google Sign-In** - OAuth authentication
- **Password Reset** - Email-based recovery
- **Email Verification** - Account verification
- **Comprehensive Error Handling** - User-friendly messages

### 📱 Advanced Features

#### Home Screen Widgets
- **Multiple Sizes** - 2x3 and 3x2 grid layouts
- **Dynamic Updates** - Automatic refresh on goal changes
- **Scrollable Content** - Shows all 12 goals
- **Image Support** - Goal images in widgets
- **Performance Optimized** - Efficient data loading

#### Profile System
- **User Statistics** - Goal completion tracking
- **Achievement Sharing** - Social media integration
- **Profile Image Management** - Upload and optimization
- **Account Security** - Password management
- **Data Export** - User data portability

#### Settings & Preferences
- **Notification Management** - Granular control
- **Language Selection** - English/Vietnamese
- **Audio Feedback** - Sound preference controls
- **Theme Customization** - Future extensibility
- **Privacy Settings** - Data management options

#### Year Planning
- **Monthly Goal Planning** - Structured goal setting
- **Progress Visualization** - Visual progress tracking
- **Content Management** - Rich text and media support
- **Calendar Integration** - Timeline-based planning

---

## 🛠️ Technology Stack

### Frontend Technologies
- **Jetpack Compose** - Modern Android UI toolkit
- **Material 3 Design** - Latest Material Design system
- **Navigation Compose** - Compose-based navigation
- **Coil** - Image loading library
- **Konfetti** - Confetti animation library

### Backend & Data
- **Firebase Auth** - Authentication service
- **Firebase Firestore** - NoSQL database
- **Firebase Storage** - File storage service
- **DataStore** - Local preferences storage
- **Flow & StateFlow** - Reactive programming

### Architecture & DI
- **Hilt** - Dependency injection
- **MVVM Architecture** - Model-View-ViewModel
- **Clean Architecture** - Multi-module structure
- **Repository Pattern** - Data abstraction

### Development Tools
- **Kotlin** - Primary language
- **KtLint** - Code formatting
- **Gradle KTS** - Build configuration
- **JUnit 4** - Unit testing
- **MockK** - Mocking framework
- **Turbine** - Flow testing

---

## 🔄 Development Workflow

### Build Commands
```bash
# Complete build with tests
./gradlew build

# Build without tests
./gradlew assemble

# Clean all build artifacts
./gradlew clean
```

### Testing Commands
```bash
# Run all unit tests
./gradlew test

# Run debug unit tests
./gradlew testDebugUnitTest

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run all checks (lint, tests, etc.)
./gradlew check
```

### Code Quality
```bash
# Auto-format code (run before committing)
./gradlew ktlintFormat

# Check code formatting
./gradlew ktlintCheck

# Run Android lint
./gradlew lint

# Recommended pre-commit workflow
./gradlew clean ktlintFormat build
```

### Git Hooks
- **Pre-commit Hook** - Runs ktlintFormat, check, and ktlintCheck automatically

---

## 📚 API Reference

### Core Domain Models

#### Goal Model
```kotlin
// domain/model/Goal.kt:12
data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val status: GoalStatus = GoalStatus.ACTIVE,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class GoalStatus {
    ACTIVE, COMPLETED, FAILED, ARCHIVED
}
```

#### User Preferences
```kotlin
// domain/model/UserPreferences.kt:8
data class UserPreferences(
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val weeklyRemindersEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true
)
```

### Repository Interfaces

#### Goal Repository
```kotlin
// domain/repository/GoalRepository.kt:15
interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    suspend fun createGoal(goal: Goal): Result<Goal>
    suspend fun updateGoal(goal: Goal): Result<Goal>
    suspend fun deleteGoal(goalId: String): Result<Unit>
    suspend fun reorderGoals(goals: List<Goal>): Result<Unit>
}
```

#### Auth Repository
```kotlin
// domain/repository/AuthRepository.kt:12
interface AuthRepository {
    fun getAuthStatus(): Flow<AuthResult>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun resetPassword(email: String): AuthResult
}
```

### Use Case Examples

#### Create Goal Use Case
```kotlin
// domain/usecase/goal/CreateGoalUseCase.kt:18
class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal): Result<Goal> {
        return try {
            repository.createGoal(goal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🧪 Testing Structure

### Test Organization
```
Testing Structure:
├── Unit Tests (domain/src/test/)
│   ├── Use case testing
│   ├── Business logic validation
│   └── Domain model testing
├── Integration Tests (ui/src/androidTest/)
│   ├── UI component testing
│   ├── Firebase integration
│   └── End-to-end workflows
└── Widget Tests (app/src/test/)
    ├── Widget functionality
    ├── Data loading
    └── Update scheduling
```

### Testing Tools & Libraries
- **JUnit 4** - Testing framework
- **MockK** - Mocking library for Kotlin
- **Turbine** - Flow testing utility
- **Compose Test** - UI testing for Compose
- **Firebase Test Lab** - Cloud testing platform

### Test Categories
- **Unit Tests** - Fast, isolated component testing
- **Integration Tests** - Component interaction testing
- **UI Tests** - User interface and interaction testing
- **Performance Tests** - Widget and image loading performance

---

## 📖 Resources & References

### Project Documentation
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines and Claude Code instructions
- **[Mountain Sunrise Theme](mountain_sunrise_color_palette.md)** - Design system documentation
- **[UI Mockups](mountain_sunrise_ui_mockup.md)** - Visual design specifications

### Development Resources
- **[Architecture Patterns](.serena/memories/architecture_patterns.md)** - Architectural guidelines
- **[Coding Conventions](.serena/memories/coding_conventions.md)** - Code style standards
- **[Task Workflows](tasks/)** - Development task tracking

### Feature Documentation
- **[Profile System](docs/prd/profile.md)** - Profile feature specification
- **[Year Planner](docs/YearPlanner_Feature_PRD.md)** - Year planning feature
- **[Widget System](docs/Widget_Integration_Plan.md)** - Widget implementation guide

### Configuration Files
- **[build.gradle.kts](build.gradle.kts)** - Project build configuration
- **[settings.gradle.kts](settings.gradle.kts)** - Module configuration
- **[gradle.properties](gradle.properties)** - Gradle properties

### External Dependencies
- **Firebase Console** - Backend service management
- **Google Cloud Platform** - Infrastructure and services
- **Material Design 3** - UI component specifications
- **Jetpack Compose** - Modern Android UI toolkit

---

## 🚀 Quick Start Guide

### Setup Requirements
1. **Android Studio** - Latest stable version
2. **JDK 11** - Java development kit
3. **Firebase Project** - With Auth, Firestore, and Storage enabled
4. **google-services.json** - Place in app/ module

### First Time Setup
```bash
# Clone repository
git clone [repository-url]

# Open in Android Studio
# Wait for Gradle sync to complete

# Add google-services.json to app/ module
# Configure Firebase project settings in Constants.kt

# Run the app
./gradlew :app:installDebug
```

### Development Process
1. **Read codebase** for relevant files
2. **Create task plan** in tasks/ directory
3. **Implement changes** following coding conventions
4. **Run tests** and quality checks
5. **Format code** with ktlintFormat
6. **Commit changes** using conventional commit format

---

*Documentation generated with Claude Code SuperClaude framework*
*Last updated: September 2025*