# 🎯 Dingo - Vision Board Bingo App

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg" alt="UI">
  <img src="https://img.shields.io/badge/Architecture-Clean%20Architecture-red.svg" alt="Architecture">
</div>

## 🎮 Overview
**Dingo** (Bingo + Ding! 🔔) revolutionizes goal achievement by transforming traditional vision boards into an interactive, game-like experience. Combining the excitement of Bingo with the satisfaction of task completion, every goal achieved triggers that rewarding "Ding!" notification.

## 🎯 Purpose
Transform your yearly planning into an engaging, gamified journey where achieving goals becomes as thrilling as winning Bingo. The app empowers users to:
- 📋 **Set & Visualize**: Create yearly goals in an intuitive Bingo-card format
- 📊 **Track Progress**: Monitor achievements with real-time visual feedback  
- 🎉 **Celebrate Success**: Enjoy satisfying notifications and confetti animations
- 🚀 **Stay Motivated**: Maintain momentum through gamification and weekly wrap-ups
- 🌐 **Sync Everywhere**: Access your goals across all devices with Firebase integration

## ✨ Features

### 🔐 Authentication System
Complete user authentication with Firebase integration:
- **Email/Password**: Secure account creation and login
- **Google Sign-In**: One-tap authentication with enhanced error handling
- **Session Management**: Persistent login state across app launches
- **Security**: Firebase Auth backend with proper token handling
- **User Feedback**: Comprehensive error messages and loading states

### 🎯 Goal Management
Transform your goals into an interactive Bingo experience:
- **Visual Goal Cards**: Beautiful, customizable goal representations
- **Status Tracking**: Active, completed, failed, and archived states
- **Drag & Drop**: Intuitive goal reordering with haptic feedback
- **Rich Media**: Custom images, GIFs, and hand-drawn stickers
- **Real-time Sync**: Firebase Firestore for instant cross-device updates

### 🎨 Interactive UI/UX
Engaging interface designed for motivation:
- **Mountain Sunrise Theme**: Custom Material 3 design with gradient backgrounds
- **Confetti Celebrations**: Physics-based animations for goal completions
- **Bubble Animations**: Dynamic scroll effects and visual feedback
- **Responsive Design**: Adaptive layouts for phones and tablets
- **Accessibility**: Sound effects, haptic feedback, and visual cues

### 📊 Progress Tracking
Stay motivated with comprehensive tracking:
- **Weekly Wrap-ups**: Automatic progress summaries with sharing
- **Visual Indicators**: Clear status representation for all goals
- **Statistics**: Completion rates and achievement analytics
- **Gamification**: Bingo-style achievement system

### 🌍 Internationalization
Multi-language support for global users:
- **Dynamic Language Switching**: Change language without restart
- **Localized Content**: Full UI translation (English/Vietnamese)
- **Cultural Adaptation**: Locale-specific formatting and preferences

## Architecture
The project follows Clean Architecture principles with a modular structure:

```
Dingo/
├── app/          # Navigation & DI setup
├── ui/           # UI Components & ViewModels
├── data/         # Data models & local storage
├── domain/   # Data operations & business logic
└── common/       # Shared UI components & utilities
```

### Module Responsibilities
- **:app** - Handles navigation and dependency injection setup
- **:ui** - Contains all screens, ViewModels, and UI state management
- **:data** - Manages Firebase services, data models, and cloud storage
- **:domain** - Implements business logic and data operations
- **:common** - Houses shared components, themes, and utilities

## Design Patterns

### Architectural Patterns
1. **MVVM (Model-View-ViewModel)**
  - Views (Composables) in :ui module
  - ViewModels for state management and business logic
  - Models in :data module

2. **Repository Pattern**
  - Abstract data operations behind repository interfaces
  - Separate data sources from business logic
  - Implemented in :domain module

3. **Dependency Injection**
  - Using Hilt for dependency management
  - Modules scoped appropriately for better testability
  - Clear dependency graph through module boundaries

### Design Patterns Used
1. **Observer Pattern**
  - Using Kotlin Flow for reactive data streams
  - StateFlow for UI state management
  - Firebase real-time listeners for data observations

2. **Factory Pattern**
  - Firebase service initialization
  - ViewModel creation through Hilt

3. **Singleton Pattern**
  - Firebase service instances
  - Repository implementations
  - Application-level components

## SOLID Principles Implementation

1. **Single Responsibility Principle**
  - Each module has a specific responsibility
  - Clear separation between data, UI, and business logic

2. **Open/Closed Principle**
  - Abstract repositories allow for extension
  - Modular design enables adding features without modifying existing code

3. **Liskov Substitution Principle**
  - Repository implementations are substitutable
  - Interface-based design in repositories and ViewModels

4. **Interface Segregation Principle**
  - Specific interfaces for different repository functionalities
  - Separated UI components for reusability

5. **Dependency Inversion Principle**
  - High-level modules depend on abstractions
  - Dependency injection for loose coupling

## 🛠️ Tech Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **UI Framework** | Jetpack Compose | Modern declarative UI |
| **Navigation** | Navigation Compose | Type-safe navigation |
| **Backend** | Firebase Suite | Authentication, Database, Storage |
| **Database** | Firebase Firestore | Real-time NoSQL database |
| **Storage** | Firebase Storage | Image and media storage |
| **Authentication** | Firebase Auth + Google Sign-In | Secure user management |
| **Dependency Injection** | Hilt | Clean dependency management |
| **Architecture** | Clean Architecture + MVVM | Scalable, testable structure |
| **State Management** | Kotlin Flow + StateFlow | Reactive programming |
| **Image Loading** | Coil | Efficient image loading |
| **Animations** | Konfetti + Custom Compose | Rich visual feedback |
| **Build System** | Gradle (Kotlin DSL) | Modern build configuration |
| **Code Quality** | KtLint | Consistent code formatting |

## Getting Started
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Configure Firebase Authentication:
  - Add your `google-services.json` to the app module
  - Update the Google Web Client ID in `Constants.kt`
5. Run the app

## Setup & Configuration

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9.0
- Firebase project with Authentication enabled

### Environment Setup
1. Clone the repository:
```bash
git clone https://github.com/yourusername/Dingo.git
```

2. Configure local.properties:
- Create `local.properties` in the project root if it doesn't exist
- Add your Android SDK path:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

3. Firebase Configuration:
- Create a Firebase project in the Firebase Console
- Enable Authentication with Email/Password and Google Sign-In
- Download `google-services.json` and place it in the app module
- Update the Google Web Client ID in `common/src/main/java/io/sukhuat/dingo/common/Constants.kt`

### Build Configuration
The project uses Gradle Version Catalog for dependency management:

1. Key Dependencies (in `gradle/libs.versions.toml`):
```toml
[versions]
compose-compiler = "1.5.1"
firebase-bom = "32.7.2"
hilt = "2.50"
play-services-auth = "20.7.0"
coil-compose = "2.4.0"

[libraries]
# Core dependencies are managed here
```

2. Module-specific setup:
- **:app**: Main application module with navigation
- **:data**: Firebase services and repository implementations
- **:domain**: Business logic and repository interfaces
- **:ui**: Compose UI components and ViewModels
- **:common**: Shared resources and UI components

### Firebase Setup
Firebase services are configured in the application:
```kotlin
@HiltAndroidApp
class DingoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        syncManager.startSyncObserver()
    }
}
```

### Dependency Injection
Hilt is configured at the application level:
```kotlin
@HiltAndroidApp
class DingoApplication : Application()
```

### Running Tests
1. Unit Tests:
```bash
./gradlew test
```

2. Instrumented Tests:
```bash
./gradlew connectedAndroidTest
```

### Build Variants
- **debug**: Development build with logging enabled
- **release**: Optimized build with ProGuard rules

### Common Issues & Solutions
1. Build Errors
  - Clean project: `./gradlew clean`
  - Invalid Gradle cache: Delete `.gradle` folder
  - Sync project with Gradle files

2. Firebase Issues
  - Check Firebase configuration and google-services.json
  - Verify Firestore rules and authentication
  - Enable Firebase debugging in logs

3. Compose Preview Issues
  - Invalidate caches / Restart
  - Update Compose compiler version
  - Check module dependencies

### Development Workflow
1. Feature Development
  - Create feature branch
  - Implement in appropriate module
  - Add tests
  - Create pull request

2. Best Practices
  - Follow Clean Architecture
  - Use Composition over Inheritance
  - Write unit tests for ViewModels
  - Document public APIs

### IDE Configuration
Recommended Android Studio settings:
- Enable Compose Preview
- Configure KSP for Hilt
- Set up detekt for static analysis
- Enable compose compiler metrics

## Gradle Commands

### Code Quality
The project uses ktlint for code formatting and various checks. Here are the main Gradle commands:

```bash
# Run all project checks (compilation, lint, tests)
# On Linux/Mac:
./gradlew check
# On Windows:
gradlew.bat check

# Format code using ktlint (fixes most style issues automatically)
# On Linux/Mac:
./gradlew ktlintFormat
# On Windows:
gradlew.bat ktlintFormat

# Verify code formatting
# On Linux/Mac:
./gradlew ktlintCheck
# On Windows:
gradlew.bat ktlintCheck

# One-command fix for most common issues (recommended before committing)
# On Linux/Mac:
./gradlew clean ktlintFormat build
# On Windows:
gradlew.bat clean ktlintFormat build
```

These commands are automatically run as part of the git pre-commit hook to ensure code quality. If you encounter KtLint errors, the `ktlintFormat` command will automatically fix most issues. For more complex problems, check the error reports in the `build/reports/ktlint/` directory.

## Architecture Diagrams

### Module Dependencies
```mermaid
graph TD
    A[app] --> B[ui]
    A --> C[common]
    B --> D[domain]
    B --> C
    D --> E[data]
    D --> C
```

### Clean Architecture Layers
```mermaid
graph TB
    subgraph Presentation Layer
        UI[UI Components]
        VM[ViewModels]
    end
    
    subgraph Domain Layer
        UC[Use Cases]
        RE[Repository Interfaces]
    end
    
    subgraph Data Layer
        RI[Repository Implementations]
        DS[Data Sources]
    end
    
    UI --> VM
    VM --> UC
    UC --> RE
    RE --> RI
    RI --> DS
```

### Authentication Flow Diagram
```mermaid
graph TD
    A[App Launch] --> B[Splash Screen]
    B --> C{User Authenticated?}
    C -->|Yes| D[Home Screen]
    C -->|No| E[Auth Screen]
    E -->|Sign In/Up Success| D
    D -->|Sign Out| E
    E -->|Google Sign-In| F[Google Auth Flow]
    F -->|Success| D
    F -->|Failure| E
```

### Data Flow
```mermaid
sequenceDiagram
    participant U as UI/Composable
    participant VM as ViewModel
    participant R as Repository
    participant FB as Firebase Services
    
    U->>VM: User Action
    VM->>R: Request Data
    R->>FB: Query Firestore/Auth
    FB-->>R: Return Data Flow
    R-->>VM: Transform Data
    VM-->>U: Update UI State
```

### MVVM Implementation
```mermaid
graph LR
    subgraph View Layer
        C[Composables]
    end
    
    subgraph ViewModel Layer
        VS[ViewState]
        VM[ViewModel]
        E[Events]
    end
    
    subgraph Model Layer
        R[Repository]
        DB[(Database)]
    end

    C -->|Observes| VS
    C -->|Triggers| E
    E -->|Handles| VM
    VM -->|Updates| VS
    VM -->|Uses| R
    R -->|Accesses| DB
```

### Dependency Injection Graph
```mermaid
graph TB
    subgraph Hilt Modules
        FM[FirebaseModule]
        RM[RepositoryModule]
        AuthM[AuthModule]
        NM[NetworkModule]
    end
    
    subgraph Components
        VM[ViewModels]
        DOM[Domain Layer]
        FS[Firebase Services]
        FB[(Firebase)]
        Auth[Authentication]
    end

    FM -->|Provides| FB
    FB -->|Provides| FS
    RM -->|Binds| DOM
    DOM -->|Uses| FS
    VM -->|Injects| DOM
    AuthM -->|Provides| Auth
    VM -->|Injects| Auth
```

## Future Enhancements
- Custom board creation
- Goal categories
- Progress tracking
- Social sharing
- Achievement notifications
- Statistical analysis

## Key Components

### Authentication
- **AuthScreen**: Composable for login/signup with email/password and Google Sign-In
- **AuthViewModel**: Manages authentication state and operations
- **AuthUiState**: Sealed class representing different authentication states with granular loading states
- **SplashScreen**: Entry point that checks authentication status
- **HomeScreen**: Main screen with logout functionality
- **GoogleAuthService**: Singleton service for managing Google Sign-In with improved error handling

### Use Cases
- **SignInUseCase**: Handles email/password authentication and Google Sign-In
- **SignUpWithEmailUseCase**: Manages user registration
- **GetAuthStatusUseCase**: Checks if user is authenticated
- **SignOutUseCase**: Handles user logout

### Services
- **GoogleAuthService**: Manages Google Sign-In functionality with proper initialization checks
- **ToastHelper**: Provides consistent toast messages for user feedback

## UI Screens

### Splash Screen
- Validates authentication status on app launch
- Directs users to appropriate screen based on status

### Auth Screen
- Email/password sign-in form
- Email/password registration
- Google Sign-In button with improved error handling
- Error handling with user feedback
- Granular loading states for different authentication processes

### Home Screen
- Main application interface
- Sign-out functionality in top app bar
- User session management

## Error Handling
The app includes comprehensive error handling:
- Descriptive error messages for authentication failures
- Toast notifications for user feedback
- Logging for debugging purposes
- UI state updates to reflect error conditions
- Specific error handling for Google Sign-In failures

## Navigation
- **NavHost**: Manages navigation between screens
- **Screen**: Sealed class defining navigation routes
- Proper back stack management for authentication flow

## Security Considerations
- Secure storage of authentication tokens
- Proper session management
- Firebase Authentication for secure credential handling
- Google OAuth for third-party authentication
- Improved error handling for authentication failures
