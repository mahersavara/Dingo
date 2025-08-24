# Coding Conventions and Style Guide

## Code Style
- **Formatter**: KtLint enforced formatting with pre-commit hooks
- **Language**: Kotlin with Android conventions
- **Architecture**: Clean Architecture principles with clear module boundaries

## Naming Conventions

### Files and Classes
- **Activity**: `MainActivity.kt`
- **Objects**: `Constants` (object declaration)
- **Packages**: lowercase with dots (e.g., `io.sukhuat.dingo.common.components`)

### Code Patterns from Examples

#### Object Declaration (Constants)
```kotlin
object Constants {
    /**
     * Documentation comments for important constants
     */
    const val GOOGLE_WEB_CLIENT_ID = "..."
}
```

#### Activity Structure
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Private properties with descriptive comments
    private var isLanguageChange = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DingoTheme {
                // Compose UI structure
            }
        }
    }
}
```

## Dependency Injection
- Use `@AndroidEntryPoint` for Activities
- Hilt modules organized by functionality:
  - `AuthModule`: Authentication services
  - `RepositoryModule`: Repository implementations  
  - `UseCaseModule`: Business logic use cases
  - `FirebaseModule`: Firebase services

## State Management
- ViewModels use `StateFlow` for UI state
- Repository layer uses `Flow` for reactive data
- Error handling with sealed classes

## Comments and Documentation
- Use KDoc style comments (`/**`) for important constants and public APIs
- Inline comments for complex logic explanation
- Keep comments concise and focused on "why" rather than "what"

## Module Dependencies
Strictly follow Clean Architecture dependency rules:
- `:domain` has no dependencies on other modules
- `:data` depends only on `:domain` and `:common`
- `:ui` can depend on `:data`, `:domain`, and `:common`
- `:app` can depend on all modules

## Testing Conventions
- **Unit tests**: Located in `src/test/` directories
- **Integration tests**: Located in `src/androidTest/` directories
- **Test libraries**: JUnit 4, MockK, Turbine for Flow testing, Compose Test
- Test classes should mirror the structure of the code they test

## Android-Specific Conventions
- Use `enableEdgeToEdge()` for modern Android UI
- Follow Material 3 design principles
- Implement proper lifecycle handling in Activities
- Use Jetpack Compose for all UI components