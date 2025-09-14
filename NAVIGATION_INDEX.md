# Dingo - Navigation & Quick Reference Index

## 🧭 Quick Navigation

### 📚 **Documentation Hub**
- **[PROJECT_INDEX.md](PROJECT_INDEX.md)** - Comprehensive project overview
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines & Claude Code instructions
- **[README.md](README.md)** - Project setup and basic information

### 🎨 **Design & UI System**
- **[Mountain Sunrise Color Palette](mountain_sunrise_color_palette.md)** - Complete color system
- **[UI Mockups](mountain_sunrise_ui_mockup.md)** - Visual design specifications
- **[Style Guide](mountain_sunrise_style_guide.md)** - Design system guidelines

---

## 📁 **Module Quick Access**

### `:app` Module - Application Entry Point
```
app/src/main/java/io/sukhuat/dingo/
├── MainActivity.kt                    # Main application entry
├── navigation/
│   └── Screen.kt                     # Navigation routes
├── widget/                           # Home screen widgets
│   ├── WeeklyGoalWidget.kt          # Main widget
│   ├── WeeklyGoalWidget2x3.kt       # 2x3 size variant
│   └── WeeklyGoalWidget3x2.kt       # 3x2 size variant
└── di/                              # Dependency injection
```

### `:ui` Module - Presentation Layer
```
ui/src/main/java/io/sukhuat/dingo/ui/
├── screens/
│   ├── auth/                        # Authentication screens
│   │   ├── AuthScreen.kt           # Main auth screen
│   │   ├── AuthViewModel.kt        # Auth logic
│   │   └── EnhancedRegistrationScreen.kt
│   ├── home/                        # Home/main screens
│   │   ├── HomeScreen.kt           # Main goal grid
│   │   ├── HomeViewModel.kt        # Home logic
│   │   └── HomeUiState.kt          # Home state
│   ├── profile/                     # User profile
│   │   ├── ProfileScreen.kt        # Main profile
│   │   ├── ProfileViewModel.kt     # Profile logic
│   │   └── components/             # Profile components
│   ├── settings/                    # App settings
│   │   ├── SettingsScreen.kt       # Settings UI
│   │   └── SettingsViewModel.kt    # Settings logic
│   └── yearplanner/                 # Year planning feature
│       ├── YearPlannerScreen.kt    # Year planner UI
│       └── YearPlannerViewModel.kt # Planning logic
└── components/                      # Reusable UI components
    ├── DragDropGrid.kt             # Main goal grid with drag & drop
    ├── GoalCell.kt                 # Individual goal display
    └── CachedAsyncImage.kt         # Optimized image loading
```

### `:data` Module - Data Access Layer
```
data/src/main/java/io/sukhuat/dingo/data/
├── repository/                      # Repository implementations
│   ├── GoalRepositoryImpl.kt       # Goal data operations
│   ├── UserProfileRepositoryImpl.kt # Profile data
│   └── ProfileStatisticsRepositoryImpl.kt
├── remote/                          # Firebase services
│   ├── FirebaseGoalService.kt      # Goal Firebase ops
│   └── FirebaseStorageService.kt   # File storage
├── auth/                           # Authentication
│   ├── GoogleAuthService.kt        # Google sign-in
│   └── EmailVerificationManager.kt # Email verification
├── notification/                    # Push notifications
│   ├── NotificationService.kt      # Notification handling
│   └── NotificationScheduler.kt    # Scheduled notifications
└── di/                             # Dependency injection modules
```

### `:domain` Module - Business Logic
```
domain/src/main/java/io/sukhuat/dingo/domain/
├── model/                          # Domain models
│   ├── Goal.kt                     # Core goal entity
│   ├── UserPreferences.kt          # User settings
│   └── yearplanner/               # Year planning models
├── repository/                     # Repository interfaces
│   ├── GoalRepository.kt          # Goal data interface
│   ├── AuthRepository.kt          # Auth interface
│   └── UserProfileRepository.kt   # Profile interface
├── usecase/                       # Business logic use cases
│   ├── auth/                      # Authentication use cases
│   ├── goal/                      # Goal management
│   ├── profile/                   # User profile
│   └── preferences/               # User preferences
└── di/                           # Domain DI modules
```

### `:common` Module - Shared Components
```
common/src/main/java/io/sukhuat/dingo/common/
├── theme/                         # Design system
│   ├── Theme.kt                  # Main theme configuration
│   ├── Color.kt                  # Mountain Sunrise palette
│   ├── Typography.kt             # Font definitions
│   └── Dimensions.kt             # Layout dimensions
├── components/                    # Reusable UI components
│   ├── DingoButton.kt           # Styled button
│   ├── DingoCard.kt             # Styled card
│   ├── DingoTextField.kt        # Styled text input
│   ├── LoadingIndicator.kt      # Loading states
│   └── ConfettiAnimation.kt     # Goal completion animation
├── utils/                        # Utility functions
│   ├── DimensionUtils.kt        # Layout utilities
│   └── ImageUtils.kt            # Image processing
└── localization/                 # Internationalization
    ├── LanguagePreferences.kt   # Language settings
    └── LocalizationModule.kt    # Localization DI
```

---

## 🔧 **Development Quick Reference**

### Build & Development Commands
```bash
./gradlew build                    # Complete build with tests
./gradlew assemble                 # Build without tests
./gradlew clean                    # Clean build artifacts
./gradlew ktlintFormat             # Auto-format code
./gradlew ktlintCheck              # Check formatting
```

### Key Configuration Files
- **[build.gradle.kts](build.gradle.kts)** - Project build configuration
- **[settings.gradle.kts](settings.gradle.kts)** - Module configuration
- **[gradle.properties](gradle.properties)** - Gradle properties
- **[proguard-rules.pro](app/proguard-rules.pro)** - Code obfuscation rules

### Firebase Configuration
- **[google-services.json](app/google-services.json)** - Firebase config
- **[Constants.kt](common/src/main/java/io/sukhuat/dingo/common/Constants.kt)** - App constants

---

## 🎯 **Feature Navigation**

### Core Features
| Feature | Screen | ViewModel | Repository | Use Cases |
|---------|--------|-----------|------------|-----------|
| **Goal Management** | [HomeScreen.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/home/HomeScreen.kt) | [HomeViewModel.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/home/HomeViewModel.kt) | [GoalRepositoryImpl.kt](data/src/main/java/io/sukhuat/dingo/data/repository/GoalRepositoryImpl.kt) | [goal/](domain/src/main/java/io/sukhuat/dingo/domain/usecase/goal/) |
| **Authentication** | [AuthScreen.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/auth/AuthScreen.kt) | [AuthViewModel.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/auth/AuthViewModel.kt) | [AuthRepository.kt](domain/src/main/java/io/sukhuat/dingo/domain/repository/AuthRepository.kt) | [auth/](domain/src/main/java/io/sukhuat/dingo/domain/usecase/auth/) |
| **User Profile** | [ProfileScreen.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/profile/ProfileScreen.kt) | [ProfileViewModel.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/profile/ProfileViewModel.kt) | [UserProfileRepositoryImpl.kt](data/src/main/java/io/sukhuat/dingo/data/repository/UserProfileRepositoryImpl.kt) | [profile/](domain/src/main/java/io/sukhuat/dingo/domain/usecase/profile/) |
| **Settings** | [SettingsScreen.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/settings/SettingsScreen.kt) | [SettingsViewModel.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/settings/SettingsViewModel.kt) | [UserPreferencesRepositoryImpl.kt](data/src/main/java/io/sukhuat/dingo/data/repository/UserPreferencesRepositoryImpl.kt) | [preferences/](domain/src/main/java/io/sukhuat/dingo/domain/usecase/preferences/) |
| **Year Planning** | [YearPlannerScreen.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/yearplanner/YearPlannerScreen.kt) | [YearPlannerViewModel.kt](ui/src/main/java/io/sukhuat/dingo/ui/screens/yearplanner/YearPlannerViewModel.kt) | [YearPlannerRepository.kt](domain/src/main/java/io/sukhuat/dingo/domain/repository/YearPlannerRepository.kt) | [yearplanner/](domain/src/main/java/io/sukhuat/dingo/domain/usecase/yearplanner/) |

### Widget System
| Component | Purpose | File |
|-----------|---------|------|
| **Main Widget** | Core widget implementation | [WeeklyGoalWidget.kt](app/src/main/java/io/sukhuat/dingo/widget/WeeklyGoalWidget.kt) |
| **2x3 Widget** | Compact size variant | [WeeklyGoalWidget2x3.kt](app/src/main/java/io/sukhuat/dingo/widget/WeeklyGoalWidget2x3.kt) |
| **3x2 Widget** | Wide size variant | [WeeklyGoalWidget3x2.kt](app/src/main/java/io/sukhuat/dingo/widget/WeeklyGoalWidget3x2.kt) |
| **Data Loader** | Widget data management | [WidgetDataLoader.kt](app/src/main/java/io/sukhuat/dingo/widget/WidgetDataLoader.kt) |
| **Update Scheduler** | Automatic widget updates | [WidgetUpdateScheduler.kt](app/src/main/java/io/sukhuat/dingo/widget/WidgetUpdateScheduler.kt) |

---

## 📋 **Task & Development Tracking**

### Task Documentation
- **[tasks/](tasks/)** - Development task tracking directory
- **[weekly_goal_widget_implementation.md](tasks/weekly_goal_widget_implementation.md)** - Widget development task
- **[profile_system_implementation.md](tasks/profile_system_implementation.md)** - Profile feature task
- **[year_planner_implementation.md](tasks/year_planner_implementation.md)** - Year planner task

### Specifications & Planning
- **[docs/](docs/)** - Project specifications directory
- **[.kiro/specs/](kiro/specs/)** - Kiro AI specifications
- **[.serena/memories/](.serena/memories/)** - Serena AI project memories

---

## 🧪 **Testing Navigation**

### Test Structure
```
Testing Organization:
├── Unit Tests
│   ├── domain/src/test/           # Business logic tests
│   └── ui/src/test/              # ViewModel tests
├── Integration Tests
│   ├── ui/src/androidTest/       # UI & Firebase integration
│   └── app/src/androidTest/      # Widget tests
└── Test Resources
    ├── MockK                     # Mocking framework
    ├── Turbine                   # Flow testing
    └── Compose Test              # UI testing
```

### Key Test Files
- **[ProfileViewModelTest.kt](ui/src/test/java/io/sukhuat/dingo/ui/screens/profile/ProfileViewModelTest.kt)** - Profile logic tests
- **[ProfileFirebaseIntegrationTest.kt](ui/src/androidTest/java/io/sukhuat/dingo/ui/screens/profile/ProfileFirebaseIntegrationTest.kt)** - Firebase integration
- **[Domain Use Case Tests](domain/src/test/java/io/sukhuat/dingo/domain/usecase/)** - Business logic validation

---

## 🔍 **Code Search Patterns**

### Common Search Queries
```bash
# Find all ViewModels
grep -r "ViewModel" --include="*.kt"

# Find all Composable functions
grep -r "@Composable" --include="*.kt"

# Find sealed classes and interfaces
grep -r "sealed class\|interface" --include="*.kt"

# Find Hilt modules
grep -r "@Module" --include="*.kt"

# Find repository implementations
grep -r "Repository" --include="*.kt"
```

### Architecture Pattern Locations
- **Models**: `domain/src/main/java/*/model/`
- **ViewModels**: `ui/src/main/java/*/screens/*/`
- **Repositories**: `data/src/main/java/*/repository/`
- **Use Cases**: `domain/src/main/java/*/usecase/`
- **DI Modules**: `*/src/main/java/*/di/`

---

## 📖 **External References**

### Development Resources
- **[Android Jetpack Compose](https://developer.android.com/jetpack/compose)** - UI framework
- **[Material 3 Design](https://m3.material.io/)** - Design system
- **[Firebase Documentation](https://firebase.google.com/docs)** - Backend services
- **[Hilt Documentation](https://dagger.dev/hilt/)** - Dependency injection

### Project Conventions
- **[Coding Conventions](.serena/memories/coding_conventions.md)** - Code style standards
- **[Architecture Patterns](.serena/memories/architecture_patterns.md)** - Architectural guidelines
- **[Task Completion Workflow](.serena/memories/task_completion_workflow.md)** - Development workflow

---

## 🚀 **Getting Started Checklist**

### New Developer Onboarding
- [ ] Read [PROJECT_INDEX.md](PROJECT_INDEX.md) for project overview
- [ ] Review [CLAUDE.md](CLAUDE.md) for development guidelines
- [ ] Set up Firebase configuration ([google-services.json](app/google-services.json))
- [ ] Run `./gradlew ktlintFormat build` to verify setup
- [ ] Explore [Design System](mountain_sunrise_color_palette.md)
- [ ] Review [Architecture Patterns](.serena/memories/architecture_patterns.md)

### Feature Development Process
1. **Read codebase** for relevant files using this navigation guide
2. **Create task plan** in [tasks/](tasks/) directory
3. **Implement changes** following [coding conventions](.serena/memories/coding_conventions.md)
4. **Run quality checks** with `./gradlew ktlintFormat check`
5. **Test thoroughly** using testing structure above
6. **Document changes** in task completion files

---

*Navigation index generated with Claude Code SuperClaude framework*
*Cross-references maintained with PROJECT_INDEX.md*