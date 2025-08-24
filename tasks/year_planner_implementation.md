# Year Planner Implementation Task

## Overview
Implement a multi-year planner with Notion-style rich text editor, Firebase sync, and swipe navigation between years according to the PRD v2.0.

## Requirements Analysis
- Multi-year planner with unlimited years
- Rich text editor with Notion-style formatting (markdown shortcuts)
- Firebase Firestore storage & sync with offline support
- Edge swipe navigation (left edge = previous year, right edge = next year)
- Auto-save with debounce (~800ms)
- Vintage paper UI design matching existing Dingo theme
- Always show full content (no expand/collapse)

## Architecture Integration Analysis

### Current Codebase Structure ✅
- **Clean Architecture**: Domain → Data → UI with clear module separation
- **Firebase Integration**: Direct Firebase-first approach (removed Room caching)
- **Navigation**: Simple Navigation Compose setup in MainActivity
- **DI**: Hilt with dedicated modules (RepositoryModule, AuthModule, etc.)
- **State Management**: StateFlow + ViewModel pattern
- **Theme**: Mountain Sunrise theme with ExtendedColors system

### Integration Points
- **Navigation**: Add YearPlanner screen to Screen.kt sealed class
- **Firebase**: Use existing FirebaseGoalService pattern for YearPlannerService
- **Theme**: Leverage existing MountainSunriseTheme with vintage palette
- **Access**: Add navigation item in HomeScreen or DingoAppScaffold
- **DI**: Create YearPlannerDataModule following existing patterns

### Key Patterns Found
1. **Repository Pattern**: Interface in domain, implementation in data with Firebase service
2. **Use Cases**: Separate use case classes in domain/usecase/ structure
3. **ViewModel**: Hilt ViewModels with StateFlow for UI state
4. **UI State**: Sealed classes for Loading/Success/Error states
5. **Firebase Service**: Direct Firestore integration with Flow-based reactive streams

## Todo List

### Phase 1: Domain Layer Setup
- [ ] Create domain models (YearPlan, MonthData, SyncStatus)
- [ ] Create repository interface (YearPlannerRepository)
- [ ] Create use cases (SaveMonthContentUseCase, LoadYearPlanUseCase, GetAllYearsUseCase)
- [ ] Add validation logic for year/month data

### Phase 2: Data Layer Implementation
- [ ] Create Firestore data models (FirebaseYearPlan, FirebaseMonthPlan)
- [ ] Create local cache entities with Room
- [ ] Implement YearPlannerRepositoryImpl with Firebase integration
- [ ] Add offline persistence and sync logic
- [ ] Create data mappers between domain and data models
- [ ] Add Hilt modules for dependency injection

### Phase 3: UI Components
- [ ] Create rich text editor component with Notion-style shortcuts
- [ ] Create MonthCard component with inline editing
- [ ] Create YearHeader component with navigation
- [ ] Create SwipeGestureDetector for edge swipe navigation
- [ ] Add vintage theme components and styling

### Phase 4: Screen Implementation
- [ ] Create YearPlannerViewModel with state management
- [ ] Create YearPlannerScreen with LazyColumn layout
- [ ] Implement auto-save with debounce logic
- [ ] Add loading states and error handling
- [ ] Integrate with existing navigation system

### Phase 5: Navigation Integration
- [ ] Add YearPlanner screen to navigation routes
- [ ] Add bottom navigation item or menu access
- [ ] Implement deep linking for specific years
- [ ] Add navigation animations

### Phase 6: Testing & Polish
- [ ] Add unit tests for use cases and repository
- [ ] Add UI tests for key interactions
- [ ] Performance testing for large content
- [ ] Add accessibility features
- [ ] Run code quality checks

## Implementation Strategy

### Module Structure
```
domain/src/main/java/io/sukhuat/dingo/domain/
├── model/yearplanner/
│   ├── YearPlan.kt
│   ├── MonthData.kt
│   └── SyncStatus.kt
├── repository/YearPlannerRepository.kt
└── usecase/yearplanner/
    ├── SaveMonthContentUseCase.kt
    ├── LoadYearPlanUseCase.kt
    └── GetAllYearsUseCase.kt

data/src/main/java/io/sukhuat/dingo/data/
├── model/
│   ├── FirebaseYearPlan.kt
│   └── FirebaseMonthPlan.kt
├── repository/YearPlannerRepositoryImpl.kt
├── cache/YearPlannerCacheManager.kt
├── mapper/YearPlannerMapper.kt
└── di/YearPlannerDataModule.kt

ui/src/main/java/io/sukhuat/dingo/ui/screens/yearplanner/
├── YearPlannerScreen.kt
├── YearPlannerViewModel.kt
└── components/
    ├── RichTextEditor.kt
    ├── MonthCard.kt
    ├── YearHeader.kt
    └── SwipeGestureDetector.kt
```

### Key Technologies
- Jetpack Compose for UI
- Firebase Firestore for cloud storage
- Room for local cache
- Hilt for dependency injection
- Navigation Compose for routing
- StateFlow for state management

### Dependencies to Add
Based on existing build.gradle, we need:
```kotlin
// Rich text editing - evaluate options:
// Option 1: Compose Markdown
implementation("com.github.jeziellago:compose-markdown:0.3.4")
// Option 2: Custom rich text implementation using existing TextField

// Already available in project:
// - Jetpack Compose (latest)
// - Firebase Firestore 
// - Hilt DI
// - Navigation Compose
// - StateFlow/Flow

// Note: Project uses Firebase-first approach, no Room needed
```

### Compatibility Notes
- **No Room**: Project removed Room caching, uses direct Firebase
- **Existing Firebase**: Leverage FirebaseGoalService pattern  
- **Theme System**: Use existing MountainSunriseTheme + ExtendedColors
- **Navigation**: Simple sealed class pattern in Screen.kt
- **Permissions**: Firebase offline persistence already configured

## Constraints
- Must follow existing code conventions
- Must integrate with existing Firebase setup
- Must maintain offline-first approach
- Must support existing auth system
- Performance: < 300ms year switch, < 50ms input latency
- Must run ktlintFormat before any commits

## Definition of Done
- [ ] All todo items completed
- [ ] All tests pass (unit + integration)
- [ ] Code quality checks pass (ktlint, lint)
- [ ] Firebase sync working with offline support
- [ ] Swipe navigation working smoothly
- [ ] Rich text editor with shortcuts working
- [ ] Auto-save functionality working
- [ ] Performance targets met
- [ ] Integrated with existing navigation
- [ ] No breaking changes to existing features

## Risk Mitigation
- **Complex rich text editing**: Start with basic markdown, iterate
- **Firebase sync conflicts**: Implement last-write-wins strategy
- **Performance with long content**: Use lazy loading and pagination if needed
- **Edge swipe conflicts**: Careful gesture detection zones
- **Offline sync complexity**: Leverage existing patterns from current app

## Next Steps
1. Confirm this plan with team
2. Start with Phase 1 (Domain Layer)
3. Implement incrementally, testing each phase
4. Regular check-ins for feedback and adjustments

---

## Progress Tracking
- **Started**: [Date to be filled]
- **Current Phase**: Planning
- **Completion**: [Date to be filled]

## Review Section
[To be filled upon completion]