# Architecture Patterns and Design Guidelines

## Clean Architecture Implementation

### Module Structure and Responsibilities

#### :domain (Business Logic Layer)
- **Purpose**: Pure business logic, no Android dependencies
- **Contents**: Use cases, models, repository interfaces, validators
- **Dependencies**: None (completely independent)
- **Example Structures**:
  - `model/` - Data models and entities
  - `usecase/` - Business logic use cases
  - `repository/` - Repository interfaces
  - `util/` - Business logic utilities
  - `validation/` - Input validation logic

#### :data (Data Layer)
- **Purpose**: Data access and external services
- **Contents**: Repository implementations, Firebase services, cache managers
- **Dependencies**: `:domain`, `:common`
- **Example Structures**:
  - `repository/` - Repository implementations
  - `network/` - Network services and connectivity
  - `cache/` - Caching implementations
  - `mapper/` - Data model mappers
  - `sync/` - Data synchronization services
  - `di/` - Data layer dependency injection modules

#### :ui (Presentation Layer)
- **Purpose**: UI components, ViewModels, and user interactions
- **Contents**: Composables, ViewModels, UI state management
- **Dependencies**: `:data`, `:domain`, `:common`
- **Example Structures**:
  - `screens/` - Screen-level Composables
  - `components/` - Reusable UI components
  - `navigation/` - Navigation setup
  - `settings/` - Settings-related UI

#### :common (Shared Layer)
- **Purpose**: Shared utilities and base components
- **Contents**: Constants, themes, shared UI components
- **Dependencies**: None (base layer)
- **Example Structures**:
  - `components/` - Shared UI components
  - `theme/` - App theming and styling
  - `utils/` - Utility functions
  - `localization/` - Internationalization
  - `icons/` - Icon resources

#### :app (Application Layer)
- **Purpose**: Application entry point and global configuration
- **Contents**: MainActivity, application class, global DI setup
- **Dependencies**: All other modules

## Key Design Patterns

### Repository Pattern
- Repository interfaces defined in `:domain`
- Implementations in `:data` with Firebase integration
- Use cases in `:domain` orchestrate business logic

### Dependency Injection (Hilt)
- `@AndroidEntryPoint` for Android components
- Module organization by functionality:
  - `AuthModule` - Authentication services
  - `RepositoryModule` - Repository bindings
  - `UseCaseModule` - Business logic providers
  - `FirebaseModule` - Firebase service configuration

### State Management
- ViewModels use `StateFlow` for UI state
- Repository layer uses `Flow` for reactive data streams
- Error handling with sealed classes
- Compose state management with `remember` and `CompositionLocalProvider`

### MVVM Implementation
- **Model**: Domain models and data from repositories
- **View**: Jetpack Compose UI components
- **ViewModel**: State management and business logic coordination

## Firebase Integration Patterns

### Services Used
- **Firebase Auth**: User authentication
- **Firestore**: Document database for app data
- **Firebase Storage**: File and media storage
- **Firebase Analytics**: User behavior tracking

### Configuration
- Requires `google-services.json` in app module
- Google Web Client ID configuration in `Constants.kt`
- Firebase modules configured through Hilt DI

## Compose-First Development
- All UI built with Jetpack Compose
- Material 3 design system implementation
- Custom "Mountain Sunrise" theme
- Responsive and accessible UI components

## Error Handling Strategy
- Sealed classes for error types
- Flow-based error propagation
- Comprehensive error states in UI
- User-friendly error messages with fallback behavior

## Testing Architecture
- **Domain layer**: Pure unit tests for use cases and models
- **Data layer**: Repository and service integration tests
- **UI layer**: Compose UI tests and ViewModel tests
- **App layer**: End-to-end navigation and integration tests

## Internationalization
- Support for English and Vietnamese
- `LocaleHelper` for language switching
- `LanguagePreferences` for persistence
- Context-aware language application