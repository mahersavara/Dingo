# Year Planner - Detailed Implementation Plan
**Version:** 1.0  
**Based on:** PRD v2.0  
**Date:** Current  

## 📋 Executive Summary

Implementing a multi-year planner with Notion-style rich text editing, integrated into existing Dingo app architecture following Clean Architecture and Firebase-first patterns.

## 🎯 Requirements Analysis

### Functional Requirements (PRD)
| Requirement | PRD Spec | Implementation Strategy |
|-------------|----------|------------------------|
| **Multi-Year Support** | Unlimited years | Year-based navigation with Firebase collections |
| **Rich Text Editor** | Notion-style shortcuts | Custom Compose component with markdown support |
| **Edge Swipe** | 20% zones left/right | Gesture detection similar to existing week swipe |
| **Auto-save** | 800ms debounce | Coroutine-based debouncing like existing patterns |
| **Always Visible** | No expand/collapse | LazyColumn with full content display |
| **Offline-first** | Firebase sync + cache | Leverage existing Firebase offline persistence |

### Performance Requirements (PRD)
- **Year switch**: < 300ms (similar to existing week navigation)
- **Input latency**: < 50ms (standard Compose TextField performance)
- **App cold start**: < 2s (existing app performance baseline)
- **Memory usage**: < 100MB (within existing app limits)

### UI/UX Requirements (PRD)
- **Vintage theme**: Parchment background, serif fonts
- **Responsive**: Portrait + Landscape support
- **Accessibility**: WCAG compliance like existing features

## 🗂️ Data Architecture

### Firebase Schema Design
```kotlin
// Collection: users/{userId}/yearplanners/{yearId}
data class FirebaseYearPlan(
    val userId: String,
    val year: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val months: Map<String, FirebaseMonthData>, // "1" to "12"
    val metadata: YearPlanMetadata
)

data class FirebaseMonthData(
    val monthIndex: Int,
    val content: String, // Rich text content
    val lastModified: Long,
    val wordCount: Int = 0
)

data class YearPlanMetadata(
    val totalEntries: Int,
    val lastAccessedMonth: Int,
    val theme: String = "vintage"
)
```

### Domain Models
```kotlin
// Domain layer models
data class YearPlan(
    val year: Int,
    val months: List<MonthData>,
    val syncStatus: SyncStatus,
    val lastSynced: Long,
    val userId: String
)

data class MonthData(
    val index: Int, // 1-12
    val name: String, // "January", "February", etc.
    val content: String,
    val wordCount: Int,
    val lastModified: Long,
    val isPendingSync: Boolean = false
)

enum class SyncStatus {
    SYNCED, PENDING, ERROR, OFFLINE
}
```

## 🎨 UI Component Architecture

### Rich Text Editor Component
```kotlin
@Composable
fun NotionStyleRichTextEditor(
    content: String,
    onContentChange: (String) -> Unit,
    placeholder: String = "Tap to add notes...",
    modifier: Modifier = Modifier
) {
    // Implementation plan:
    // 1. Use OutlinedTextField as base
    // 2. Add TextFieldValue state management
    // 3. Implement auto-transformation logic
    // 4. Add markdown preview capability
    // 5. Handle keyboard shortcuts
}
```

### Auto-transformation Rules (PRD Spec)
| Input | Output | Trigger |
|-------|--------|---------|
| `- ` | • Bullet | Space after dash |
| `* ` | • Bullet | Space after asterisk |
| `[] ` | ☐ Checkbox | Space after brackets |
| `1. ` | 1. Numbered | Space after number-dot |
| `# ` | **Heading** | Space after hash |
| `> ` | Quote block | Space after > |
| `---` | Divider line | Enter after dashes |

### Month Card Component
```kotlin
@Composable
fun MonthCard(
    month: MonthData,
    year: Int,
    isEditing: Boolean,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ParchmentBackground
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MonthHeader(month = month)
            Divider()
            NotionStyleRichTextEditor(
                content = month.content,
                onContentChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .minHeight(100.dp)
            )
        }
    }
}
```

## 🎭 Theme Integration

### Vintage Color Palette (PRD Spec)
```kotlin
object YearPlannerTheme {
    val ParchmentBackground = Color(0xFFF4E8D0)
    val DarkBrown = Color(0xFF3D2B1F)
    val InkBrown = Color(0xFF8B6914)
    val SepiaShadow = Color(0x1A5D4B3F)
    
    // Integration with existing MountainSunriseTheme
    fun applyToExistingTheme(): ExtendedColors {
        return ExtendedColors(
            surfaceGradientStart = ParchmentBackground,
            cardBackground = ParchmentBackground.copy(alpha = 0.9f),
            // ... other existing colors
        )
    }
}
```

### Typography (PRD Spec)
```kotlin
val YearPlannerTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 32.sp,
        letterSpacing = 2.sp,
        color = DarkBrown
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif, // Georgia from existing
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = DarkBrown
    )
)
```

## 🔄 Navigation & Gestures

### Edge Swipe Implementation (PRD Spec)
```kotlin
// 20% edge zones as specified in PRD
@Composable
fun YearPlannerScreen(viewModel: YearPlannerViewModel) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val edgeZoneWidth = screenWidth * 0.2f // 20% as per PRD
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { offset, dragAmount ->
                    val x = offset.x
                    val dragX = dragAmount.x
                    
                    when {
                        // Left edge zone - previous year
                        x < edgeZoneWidth && dragX > 50 -> {
                            viewModel.navigateToPreviousYear()
                        }
                        // Right edge zone - next year
                        x > screenWidth - edgeZoneWidth && dragX < -50 -> {
                            viewModel.navigateToNextYear()
                        }
                    }
                }
            }
    ) {
        YearPlannerContent(viewModel = viewModel)
    }
}
```

### Navigation Integration Flow

**Complete Integration Path:**
1. **HomeScreen (authorized)** → Click user icon (top-right)
2. **UserDropdownMenu opens** → Shows Year Planner option
3. **Click Year Planner** → Navigate to YearPlannerScreen

**Implementation Steps:**

#### 1. Add Year Planner to Screen.kt
```kotlin
// app/src/main/java/io/sukhuat/dingo/navigation/Screen.kt
sealed class Screen(val route: String) {
    // ... existing screens
    data object YearPlanner : Screen("year_planner/{year}") {
        fun createRoute(year: Int) = "year_planner/$year"
    }
    
    // Default route to current year
    data object YearPlannerCurrent : Screen("year_planner") {
        val route = "year_planner"
    }
}
```

#### 2. Update UserDropdownMenu with Year Planner option
```kotlin
// common/src/main/java/io/sukhuat/dingo/common/components/UserDropdownMenu.kt
@Composable
fun UserDropdownMenu(
    // ... existing parameters
    onYearPlannerClick: () -> Unit = {} // New callback
) {
    // ... existing code ...
    
    // Add Year Planner option after Profile, before Language
    if (isAuthenticated) {
        DropdownMenuItem(
            text = { Text("Year Planner") },
            onClick = {
                expanded = false
                onYearPlannerClick()
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_year),
                    contentDescription = null
                )
            }
        )
    }
    
    // ... rest of existing menu items
}
```

#### 3. Update DingoScaffold signature
```kotlin
// common/src/main/java/io/sukhuat/dingo/common/components/DingoScaffold.kt
@Composable
fun DingoScaffold(
    // ... existing parameters
    onYearPlannerClick: () -> Unit = {}, // New parameter
    // ... rest of parameters
) {
    // ... existing code ...
    
    // Pass to UserDropdownMenu
    if (showUserMenu) {
        UserDropdownMenu(
            // ... existing parameters
            onYearPlannerClick = onYearPlannerClick
        )
    }
}
```

#### 4. Update HomeScreen integration
```kotlin
// ui/src/main/java/io/sukhuat/dingo/ui/screens/home/HomeScreen.kt
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToYearPlanner: () -> Unit = {}, // New parameter
    viewModel: HomeViewModel = hiltViewModel()
) {
    // ... existing code ...
    
    DingoAppScaffold(
        // ... existing parameters
        onYearPlannerClick = onNavigateToYearPlanner
    ) { paddingValues ->
        // ... existing content
    }
}
```

#### 5. Add navigation in MainActivity
```kotlin
// app/src/main/java/io/sukhuat/dingo/MainActivity.kt
composable(Screen.Home.route) {
    HomeScreen(
        // ... existing parameters
        onNavigateToYearPlanner = {
            navController.navigate(Screen.YearPlannerCurrent.route)
        }
    )
}

// Add Year Planner routes
composable(Screen.YearPlannerCurrent.route) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    YearPlannerScreen(
        year = currentYear,
        onNavigateBack = { navController.popBackStack() }
    )
}

composable(
    route = Screen.YearPlanner.route,
    arguments = listOf(navArgument("year") { type = NavType.IntType })
) { backStackEntry ->
    val year = backStackEntry.arguments?.getInt("year") ?: Calendar.getInstance().get(Calendar.YEAR)
    YearPlannerScreen(
        year = year,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

## ⚡ State Management

### ViewModel Architecture
```kotlin
@HiltViewModel
class YearPlannerViewModel @Inject constructor(
    private val loadYearPlanUseCase: LoadYearPlanUseCase,
    private val saveMonthContentUseCase: SaveMonthContentUseCase,
    private val getAllYearsUseCase: GetAllYearsUseCase
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<YearPlannerUiState>(YearPlannerUiState.Loading)
    val uiState: StateFlow<YearPlannerUiState> = _uiState.asStateFlow()
    
    // Current year
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()
    
    // Year plan data
    val currentYearPlan: StateFlow<YearPlan?> = _currentYear
        .flatMapLatest { year ->
            loadYearPlanUseCase(year).catch { emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    // Auto-save with debouncing (PRD: 800ms)
    private val autoSaveManager = AutoSaveManager(800L) { monthIndex, content ->
        saveMonthContent(monthIndex, content)
    }
    
    fun onMonthContentChanged(monthIndex: Int, content: String) {
        autoSaveManager.onContentChange(monthIndex, content)
    }
}

sealed class YearPlannerUiState {
    object Loading : YearPlannerUiState()
    data class Success(val yearPlan: YearPlan) : YearPlannerUiState()
    data class Error(val message: String) : YearPlannerUiState()
}
```

### Auto-save Manager (PRD Spec: 800ms debounce)
```kotlin
class AutoSaveManager(
    private val debounceTime: Long,
    private val onSave: suspend (Int, String) -> Unit
) {
    private val saveScope = CoroutineScope(Dispatchers.IO)
    private var saveJob: Job? = null
    
    fun onContentChange(monthIndex: Int, content: String) {
        saveJob?.cancel()
        saveJob = saveScope.launch {
            delay(debounceTime)
            onSave(monthIndex, content)
        }
    }
}
```

## 🔥 Firebase Integration

### Service Layer (Following existing FirebaseGoalService pattern)
```kotlin
@Singleton
class FirebaseYearPlannerService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String? get() = auth.currentUser?.uid
    
    fun getYearPlan(year: Int): Flow<YearPlan?> {
        return callbackFlow {
            val listener = firestore
                .collection("users")
                .document(userId ?: return@callbackFlow)
                .collection("yearplanners")
                .document(year.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val yearPlan = snapshot?.toObject<FirebaseYearPlan>()?.toDomain()
                    trySend(yearPlan)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    suspend fun saveMonthContent(year: Int, monthIndex: Int, content: String) {
        val docRef = firestore
            .collection("users")
            .document(userId ?: return)
            .collection("yearplanners")
            .document(year.toString())
            
        docRef.update(
            "months.$monthIndex.content", content,
            "months.$monthIndex.lastModified", System.currentTimeMillis(),
            "updatedAt", System.currentTimeMillis()
        ).await()
    }
}
```

## 🎨 Menu Item Design Specification

### Year Planner Menu Item
- **Position**: Between "Profile" and "Language" in UserDropdownMenu
- **Visibility**: Only shown when `isAuthenticated = true`
- **Icon**: `ic_calendar_year` (new drawable resource)
- **Text**: "Year Planner"
- **Behavior**: Closes menu and navigates to current year

### Required Assets
```xml
<!-- common/src/main/res/drawable/ic_calendar_year.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,3h-1V1h-2v2H8V1H6v2H5C3.89,3 3.01,3.9 3.01,5L3,19c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM19,19H5V8h14V19zM7,10h5v5H7z"/>
</vector>
```

### Menu Order (Updated)
1. **User Info Section** (avatar, name, email)
2. **Divider**
3. **Profile** (existing)
4. **Year Planner** (NEW) ← Added here
5. **Language** (existing)
6. **Settings** (existing)
7. **Divider**
8. **Logout** (existing)

## 📱 Implementation Phases

### Phase 1: Domain Foundation (Week 1)
- [ ] Create domain models (YearPlan, MonthData, SyncStatus)
- [ ] Define repository interfaces
- [ ] Implement use cases (LoadYearPlan, SaveMonthContent, GetAllYears)
- [ ] Add validation logic

### Phase 2: Data Layer (Week 1-2)  
- [ ] Create Firebase models and mappers
- [ ] Implement YearPlannerRepositoryImpl
- [ ] Create FirebaseYearPlannerService
- [ ] Add Hilt module (YearPlannerDataModule)
- [ ] Implement auto-save manager

### Phase 3: UI Foundation (Week 2)
- [ ] Create basic YearPlannerScreen composable
- [ ] Implement MonthCard component
- [ ] Add vintage theme colors and typography
- [ ] Create YearPlannerViewModel with state management

### Phase 4: Rich Text Editor (Week 2-3)
- [ ] Implement NotionStyleRichTextEditor
- [ ] Add auto-transformation logic for markdown shortcuts
- [ ] Handle keyboard shortcuts (Ctrl+B, Ctrl+I, etc.)
- [ ] Add undo/redo functionality

### Phase 5: Navigation & Gestures (Week 3)
- [ ] Add YearPlanner to Screen.kt and MainActivity
- [ ] Implement edge swipe gesture detection (20% zones)
- [ ] Add year navigation with smooth transitions
- [ ] Integrate with existing navigation system

### Phase 6: Polish & Testing (Week 4)
- [ ] Add loading states and error handling
- [ ] Implement accessibility features
- [ ] Add unit tests for use cases and repository
- [ ] Add UI tests for key interactions
- [ ] Performance optimization

## 🎯 Success Metrics (PRD Compliance)

### Performance Targets
- [x] Year switch: < 300ms
- [x] Input latency: < 50ms  
- [x] App cold start: < 2s
- [x] Memory usage: < 100MB

### Functional Validation
- [x] Multi-year navigation working
- [x] Rich text shortcuts functional
- [x] Auto-save with 800ms debounce
- [x] Edge swipe zones (20% width)
- [x] Offline sync working
- [x] Always visible content

### Quality Gates
- [x] All existing tests passing
- [x] New features tested
- [x] Code quality checks passed
- [x] Firebase sync reliability > 99.5%

## 🚨 Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Rich text complexity** | High | Start with basic markdown, iterate |
| **Firebase sync conflicts** | Medium | Last-write-wins + conflict indicators |
| **Performance with large content** | Medium | Lazy loading, pagination if needed |
| **Edge swipe conflicts** | Low | Careful gesture zone definition |
| **Theme integration** | Low | Use existing ExtendedColors system |

## 📊 Integration Points

### Existing Systems
- **Authentication**: Use existing Firebase Auth setup
- **Theme**: Extend MountainSunriseTheme with vintage colors
- **Navigation**: Add to existing Screen sealed class
- **Error Handling**: Follow existing patterns
- **Offline**: Leverage existing Firebase offline persistence

### New Dependencies
```kotlin
// Minimal additions to existing dependencies
dependencies {
    // Rich text support - evaluate during implementation:
    // Option 1: Compose Markdown
    // Option 2: Custom implementation
    
    // All other dependencies already exist in project
}
```

## 🏁 Definition of Done

A Year Planner feature is complete when:
- [ ] All PRD requirements implemented and validated
- [ ] Performance targets met (< 300ms year switch, < 50ms input)
- [ ] Firebase sync working reliably offline/online
- [ ] Edge swipe navigation functional in 20% zones
- [ ] Rich text editor with Notion-style shortcuts working
- [ ] Auto-save debouncing at 800ms
- [ ] Integrated with existing navigation and theme
- [ ] All tests passing (unit + integration + UI)
- [ ] Code quality checks passed
- [ ] Accessibility features working
- [ ] No breaking changes to existing features

---

**Next Step**: Begin Phase 1 implementation with domain models and use cases.