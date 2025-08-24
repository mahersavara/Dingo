# Dingo Project Overview

## Purpose
Dingo is a vision board Bingo app that gamifies goal achievement. Users create yearly goals in a Bingo-card format and get rewarded with notifications and animations when completing them.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM pattern
- **Dependency Injection**: Hilt
- **Backend**: Firebase (Auth, Firestore, Storage, Analytics)
- **State Management**: Kotlin Flow + StateFlow
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Animations**: Konfetti + Custom Compose animations
- **Testing**: JUnit 4, MockK, Turbine (Flow testing), Compose Test
- **Code Quality**: KtLint for formatting

## Multi-Module Structure
The project follows Clean Architecture with these modules:

- **:app** - Main application module with navigation and DI setup
- **:ui** - Presentation layer with Composables and ViewModels  
- **:data** - Data layer with repositories and Firebase services
- **:domain** - Business logic with use cases and models (no dependencies)
- **:common** - Shared UI components and utilities

## Dependency Flow
```
:app → :ui, :data, :domain, :common
:ui → :data, :domain, :common  
:data → :domain, :common
:domain → (no dependencies)
:common → (base components)
```

## Key Features
- Goal CRUD operations with Firebase Firestore
- Drag & drop reordering with haptic feedback
- Status tracking (Active, Completed, Failed, Archived)
- Rich media support (images, GIFs, stickers)
- Authentication (Email/Password and Google Sign-In)
- Mountain Sunrise theme with Material 3
- Confetti animations for completions
- Internationalization (English/Vietnamese)
- Accessibility with sound effects and haptic feedback
- Settings and profile management
- Social sharing functionality

## Target Platform
- **Platform**: Android
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34