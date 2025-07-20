# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dingo is a vision board Bingo app that gamifies goal achievement. Users create yearly goals in a Bingo-card format and get rewarded with notifications and animations when completing them.

## Commands

### Build & Development
```bash
./gradlew build                    # Complete build with tests
./gradlew assemble                 # Build without tests
./gradlew clean                    # Clean all build artifacts
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew testDebugUnitTest        # Run debug unit tests
./gradlew connectedAndroidTest     # Run instrumentation tests
./gradlew check                    # Run all checks (lint, tests, etc.)
```

### Code Quality
```bash
./gradlew ktlintFormat             # Auto-format code (run before committing)
./gradlew ktlintCheck              # Check code formatting
./gradlew lint                     # Run Android lint
./gradlew lintFix                  # Apply safe lint fixes
```

### Pre-commit Workflow
```bash
./gradlew clean ktlintFormat build  # Recommended before committing
```

The project has an automated git pre-commit hook that runs ktlintFormat, check, and ktlintCheck.

## Architecture

### Clean Architecture with Multi-Module Structure
- **:app** - Main application module with navigation and DI setup
- **:ui** - Presentation layer with Composables and ViewModels
- **:data** - Data layer with repositories and Firebase services
- **:domain** - Business logic with use cases and models (no dependencies)
- **:common** - Shared UI components and utilities

### Key Technologies
- **UI**: Jetpack Compose with Navigation Compose
- **Backend**: Firebase (Auth, Firestore, Storage)
- **DI**: Hilt
- **State**: Kotlin Flow + StateFlow
- **Architecture**: MVVM + Repository pattern
- **Image Loading**: Coil
- **Animations**: Konfetti + Custom Compose

### Dependency Flow
```
:app → :ui, :data, :domain, :common
:ui → :data, :domain, :common  
:data → :domain, :common
:domain → (no dependencies)
:common → (base components)
```

## Key Implementation Patterns

### Repository Pattern
- Repository interfaces defined in `:domain`
- Implementations in `:data` with Firebase integration
- Use cases in `:domain` orchestrate business logic

### Hilt Modules
- **AuthModule**: Authentication services
- **RepositoryModule**: Repository implementations
- **UseCaseModule**: Business logic use cases
- **FirebaseModule**: Firebase services

### State Management
- ViewModels use StateFlow for UI state
- Repository layer uses Flow for reactive data
- Error handling with sealed classes

## Firebase Configuration

Requires `google-services.json` in app module and Google Web Client ID configuration in `Constants.kt`.

## Testing Structure

- **Unit tests**: `domain/src/test/` for use cases and business logic
- **Integration tests**: `ui/src/androidTest/` for UI components
- **Tools**: JUnit 4, MockK, Turbine (Flow testing), Compose Test

## Code Quality Standards

- KtLint enforced formatting with pre-commit hooks
- Clean Architecture principles with clear module boundaries
- Compose-first UI development
- Flow-based reactive programming
- SOLID principles throughout

## Features

### Core Features
- Goal CRUD operations with Firebase Firestore
- Drag & drop reordering with haptic feedback
- Status tracking (Active, Completed, Failed, Archived)
- Rich media support (images, GIFs, stickers)

### Authentication
- Email/Password and Google Sign-In via Firebase Auth
- Comprehensive error handling and loading states

### UI/UX
- Mountain Sunrise theme with Material 3
- Confetti animations for completions
- Internationalization (English/Vietnamese)
- Accessibility with sound effects and haptic feedback

### Settings & Social
- Comprehensive settings with notification management
- Achievement sharing with social media integration
- Profile statistics and referral system

## Claude Rules

- Always preserve the original design when fixing bugs
- Run `./gradlew ktlintFormat` before making any commits
- When fixing UI crashes, add proper null checks and fallback components
- Maintain existing Vietnamese/English localization support
- Use conventional commit format for all commits:
  ```
  Title (conventional commit format):
  Start with feat(<component/module>): then a concise summary of what was added or changed.
  
  ✨ Features (bullet list):
  List new features or visible behaviors.
  Highlight UI interactions, toggles, dynamic layout behavior, and user flows.
  
  🔧 Technical Changes (bullet list):
  List code-level enhancements: states, handlers, positioning logic, components used/updated.
  Include offset/positioning logic, hooks, dismiss behavior, and integration notes.
  
  ⚠️ Drawbacks or known issues (optional):
  Mention any known visual/UI quirks or behavior that still needs polish.
  
  ✅ Reference (optional):
  Add the internal name, ticket ID, version, or author if available.
  ```

## Development Workflow

1. First think through the problem, read the codebase for relevant files, and write a plan to tasks/<task_name>.md.
2. The plan should have a list of todo items that you can check off as you complete them
3. Before you begin working, check in with me and I will verify the plan.
4. Then, begin working on the todo items, marking them as complete as you go.
5. Please every step of the way just give me a high level explanation of what changes you made
6. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
7. Finally, add a review section to the tasks/<task_name>.md file with a summary of the changes you made and any other relevant information.

## Known Issues

- Need to implement swipe feature for profile
- Edit bug name and image need to be fix