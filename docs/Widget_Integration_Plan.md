# 📱 Android Widgets Integration Plan

## 📋 Project Overview

**Objective**: Integrate Weekly Goal Widget và Yearly Note Widget vào existing Dingo app
**Timeline**: 6-8 weeks  
**Priority**: Weekly Goal Widget (Priority 1), Yearly Note Widget (Priority 2)

## 🏗️ Current Architecture Analysis

### Existing Tech Stack
- **Target Android**: API 34, Min API 24
- **Architecture**: Clean Architecture (domain → data → ui → app)
- **UI Framework**: Jetpack Compose with Material 3
- **DI**: Hilt
- **Database**: Room + Firebase Firestore sync
- **State Management**: StateFlow + Flow
- **Theme**: Mountain Sunrise custom theme

### Existing Modules Structure
```
:app (Application entry point)
├── :ui (Presentation layer - Compose UI)
├── :data (Data layer - Firebase + Room)
├── :domain (Business logic - Pure Kotlin)
└── :common (Shared utilities + theme)
```

## 🎯 Integration Strategy

### New Module Creation
Tạo module riêng cho widgets để tách biệt logic và không ảnh hưởng main app:

```
:widget (New module)
├── weeklygoal/ (Weekly Goal Widget)
├── yearlynote/ (Yearly Note Widget) 
├── shared/ (Common widget utilities)
└── di/ (Widget-specific DI)
```

### Dependencies
```kotlin
// :widget module dependencies
dependencies {
    implementation(project(":domain"))
    implementation(project(":data")) 
    implementation(project(":common"))
    
    // Widget-specific
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

## 📅 Weekly Goal Widget Implementation

### Phase 1: Core Infrastructure (Week 1-2)

#### 1.1 Create Widget Module Structure
```
:widget/
├── src/main/java/io/sukhuat/dingo/widget/
│   ├── weeklygoal/
│   │   ├── WeeklyGoalWidgetProvider.kt
│   │   ├── WeeklyGoalWidget.kt
│   │   ├── WeeklyGoalRepository.kt
│   │   └── WeeklyGoalWorker.kt
│   ├── shared/
│   │   ├── WidgetTheme.kt
│   │   ├── WidgetUtils.kt
│   │   └── WidgetDataManager.kt
│   └── di/
│       └── WidgetModule.kt
└── src/main/res/
    ├── layout/ (RemoteViews layouts)
    ├── drawable/ (Widget icons, backgrounds)
    └── xml/ (Widget provider config)
```

#### 1.2 Widget Provider Implementation
```kotlin
@HiltAndroidApp
class WeeklyGoalWidgetProvider : AppWidgetProvider() {
    
    @Inject lateinit var repository: WeeklyGoalRepository
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }
    
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.weekly_goal_widget)
        
        // Populate widget with current week's goals
        val weekOffset = getWeekOffset(widgetId) // From SharedPreferences
        loadGoalsForWeek(context, remoteViews, weekOffset, widgetId)
        
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}
```

#### 1.3 Data Integration với Existing Repository
```kotlin
@Singleton
class WeeklyGoalRepository @Inject constructor(
    private val goalRepository: GoalRepository, // Existing repository
    private val context: Context
) {
    
    suspend fun getGoalsForWeek(weekOffset: Int): List<Goal> {
        return goalRepository.getAllGoals().first()
            .filter { goal ->
                // Use existing Goal.weekOfYear logic
                isGoalInWeek(goal, weekOffset)
            }
            .take(6) // Max 6 goals for widget
    }
    
    suspend fun getWeekInfo(weekOffset: Int): WeekInfo {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
        return WeekInfo(
            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR),
            year = calendar.get(Calendar.YEAR),
            displayText = getWeekDisplayText(weekOffset)
        )
    }
}
```

#### 1.4 Widget Configuration Activity
```kotlin
class WeeklyGoalWidgetConfigActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        
        setContent {
            DingoAppTheme { // Reuse existing theme
                WeeklyGoalWidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onConfigurationComplete = { config ->
                        saveWidgetConfiguration(appWidgetId, config)
                        finishConfiguration(appWidgetId)
                    }
                )
            }
        }
    }
}
```

### Phase 2: Widget Layouts & UI (Week 2-3)

#### 2.1 RemoteViews Layouts
```xml
<!-- res/layout/weekly_goal_widget_2x3.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/widget_background"
    android:padding="8dp">
    
    <!-- Header with week navigation -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <ImageButton
            android:id="@+id/btn_prev_week"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:src="@drawable/ic_arrow_left"
            android:background="@drawable/widget_button_bg" />
            
        <TextView
            android:id="@+id/tv_week_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Week 23, 2024"
            android:textAlign="center"
            android:textColor="@color/widget_text_primary"
            android:textSize="14sp"
            android:fontFamily="@font/roboto_medium" />
            
        <ImageButton
            android:id="@+id/btn_next_week"
            android:layout_width="32dp"  
            android:layout_height="32dp"
            android:src="@drawable/ic_arrow_right"
            android:background="@drawable/widget_button_bg" />
    </LinearLayout>
    
    <!-- Goals Grid -->
    <GridLayout
        android:id="@+id/goals_grid"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:columnCount="2"
        android:rowCount="3"
        android:layout_marginTop="8dp">
        
        <!-- Goal items will be populated programmatically -->
        
    </GridLayout>
    
</LinearLayout>
```

#### 2.2 Goal Item Layout
```xml
<!-- res/layout/widget_goal_item.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@drawable/goal_item_background"
    android:padding="6dp"
    android:layout_margin="2dp">
    
    <ImageView
        android:id="@+id/iv_goal_icon"
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:layout_gravity="center_horizontal"
        android:scaleType="centerCrop" />
        
    <TextView
        android:id="@+id/tv_goal_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:maxLines="2"
        android:ellipsize="end"
        android:textSize="10sp"
        android:textColor="@color/widget_text_secondary"
        android:textAlign="center" />
        
    <!-- Status indicator -->
    <View
        android:id="@+id/status_indicator"
        android:layout_width="match_parent"
        android:layout_height="2dp"
        android:layout_marginTop="4dp"
        android:background="@color/goal_status_active" />
        
</LinearLayout>
```

### Phase 3: Background Updates & Sync (Week 3-4)

#### 3.1 Background Worker
```kotlin
@HiltWorker
class WeeklyGoalSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val widgetRepository: WeeklyGoalRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Sync goals data
            val allWidgetIds = getAllWidgetIds()
            
            allWidgetIds.forEach { widgetId ->
                updateWidgetData(widgetId)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun updateWidgetData(widgetId: Int) {
        val weekOffset = getWidgetWeekOffset(widgetId)
        val goals = widgetRepository.getGoalsForWeek(weekOffset)
        
        // Update widget UI
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = createRemoteViews(goals, weekOffset)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}
```

#### 3.2 Data Change Broadcast
```kotlin
// In existing GoalRepository
class GoalRepositoryImpl {
    
    suspend fun updateGoal(goal: Goal) {
        // Existing update logic
        goalDao.updateGoal(goal.toEntity())
        
        // Notify widgets of data change
        sendWidgetUpdateBroadcast()
    }
    
    private fun sendWidgetUpdateBroadcast() {
        val intent = Intent(WIDGET_UPDATE_ACTION).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
    
    companion object {
        const val WIDGET_UPDATE_ACTION = "io.sukhuat.dingo.WIDGET_UPDATE"
    }
}
```

### Phase 4: Polish & Testing (Week 4)

#### 4.1 Widget Theme Integration
```kotlin
object WidgetTheme {
    
    // Integrate with existing Mountain Sunrise theme
    val backgroundColor = Color(0xFFF5F5F3) // MountainSunriseTheme.surface
    val primaryColor = Color(0xFFD4A574) // RusticGold
    val textPrimary = Color(0xFF2D2D2D)
    val textSecondary = Color(0xFF666666)
    
    fun createWidgetBackground(): GradientDrawable {
        return GradientDrawable().apply {
            colors = intArrayOf(
                MountainSunriseTheme.surfaceGradientStart,
                MountainSunriseTheme.surfaceGradientEnd
            )
            cornerRadius = 16f
        }
    }
}
```

#### 4.2 Testing Strategy
```kotlin
@RunWith(AndroidJUnit4::class)
class WeeklyGoalWidgetTest {
    
    @Test
    fun testGoalDataLoading() {
        // Test goal filtering by week
        val goals = repository.getGoalsForWeek(0) // Current week
        assertTrue(goals.size <= 6)
    }
    
    @Test
    fun testWeekNavigation() {
        // Test week offset calculations
        val weekInfo = repository.getWeekInfo(-1) // Previous week
        assertNotNull(weekInfo.displayText)
    }
    
    @Test
    fun testWidgetConfiguration() {
        // Test widget config persistence
        val config = WidgetConfiguration(size = WidgetSize.SMALL)
        saveConfiguration(widgetId, config)
        assertEquals(config, loadConfiguration(widgetId))
    }
}
```

## 📔 Yearly Note Widget Implementation

### Phase 1: Core Components (Week 5-6)

#### 5.1 Widget Structure
```kotlin
class YearlyNoteWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateYearlyNoteWidget(context, appWidgetManager, widgetId)
        }
    }
    
    private fun updateYearlyNoteWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val config = loadWidgetConfiguration(widgetId)
        val currentMonth = loadCurrentMonthData(config.year, config.month)
        
        val remoteViews = RemoteViews(context.packageName, getLayoutResource(config.size))
        populateNoteContent(remoteViews, currentMonth)
        
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
}
```

#### 5.2 Data Integration với Year Planner
```kotlin
@Singleton
class YearlyNoteWidgetRepository @Inject constructor(
    private val yearPlannerRepository: YearPlannerRepository, // Existing
    private val context: Context
) {
    
    suspend fun getCurrentMonthData(year: Int, month: Int): MonthData? {
        return yearPlannerRepository.getYearPlan(year).first()?.getMonth(month)
    }
    
    suspend fun updateMonthContent(year: Int, month: Int, content: String) {
        val yearPlan = yearPlannerRepository.getYearPlan(year).first()
        yearPlan?.let {
            val updatedPlan = it.updateMonth(month, content)
            yearPlannerRepository.saveYearPlan(updatedPlan)
        }
    }
}
```

### Phase 2: Rich Text Editing (Week 6-7)

#### 6.1 Text Input Activity
```kotlin
class WidgetTextInputActivity : ComponentActivity() {
    
    private val viewModel: WidgetTextInputViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, -1)
        val year = intent.getIntExtra(EXTRA_YEAR, Calendar.getInstance().get(Calendar.YEAR))
        val month = intent.getIntExtra(EXTRA_MONTH, Calendar.getInstance().get(Calendar.MONTH) + 1)
        
        setContent {
            DingoAppTheme {
                WidgetTextInputScreen(
                    viewModel = viewModel,
                    year = year,
                    month = month,
                    onSave = { content ->
                        viewModel.saveContent(year, month, content)
                        updateWidget(widgetId)
                        finish()
                    }
                )
            }
        }
    }
}
```

#### 6.2 Rich Text Processing
```kotlin
class RichTextProcessor {
    
    fun processMarkdown(content: String): SpannedString {
        return buildSpannedString {
            val lines = content.split('\n')
            
            lines.forEach { line ->
                when {
                    line.startsWith("# ") -> {
                        // Heading
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            append(line.substring(2))
                        }
                    }
                    line.startsWith("* ") || line.startsWith("- ") -> {
                        // Bullet point
                        append("• ${line.substring(2)}")
                    }
                    line.startsWith("> ") -> {
                        // Quote
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color.Gray)) {
                            append(line.substring(2))
                        }
                    }
                    else -> {
                        append(line)
                    }
                }
                if (line != lines.last()) append('\n')
            }
        }
    }
}
```

### Phase 3: Auto-Save & Sync (Week 7-8)

#### 7.1 Auto-Save Implementation
```kotlin
class WidgetAutoSaveManager(
    private val repository: YearlyNoteWidgetRepository,
    private val debounceTimeMs: Long = 800L
) {
    private val saveScope = CoroutineScope(Dispatchers.IO)
    private var saveJob: Job? = null
    
    fun onContentChange(year: Int, month: Int, content: String) {
        saveJob?.cancel()
        saveJob = saveScope.launch {
            delay(debounceTimeMs)
            repository.updateMonthContent(year, month, content)
            broadcastWidgetUpdate()
        }
    }
    
    private fun broadcastWidgetUpdate() {
        // Notify all yearly note widgets to refresh
        val intent = Intent(YEARLY_NOTE_WIDGET_UPDATE_ACTION)
        context.sendBroadcast(intent)
    }
}
```

## 🔗 Integration với Main App

### App Module Changes

#### 1. Update settings.gradle.kts
```kotlin
include(":app")
include(":domain") 
include(":data")
include(":common")
include(":ui")
include(":widget") // New module
```

#### 2. Update app/build.gradle.kts
```kotlin
dependencies {
    implementation(project(":widget")) // Add widget module
    // ... existing dependencies
}
```

#### 3. AndroidManifest.xml Updates
```xml
<!-- app/src/main/AndroidManifest.xml -->
<application>
    <!-- Existing components -->
    
    <!-- Widget Providers -->
    <receiver android:name="io.sukhuat.dingo.widget.weeklygoal.WeeklyGoalWidgetProvider"
        android:exported="true">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data android:name="android.appwidget.provider"
            android:resource="@xml/weekly_goal_widget_info" />
    </receiver>
    
    <receiver android:name="io.sukhuat.dingo.widget.yearlynote.YearlyNoteWidgetProvider"
        android:exported="true">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data android:name="android.appwidget.provider"
            android:resource="@xml/yearly_note_widget_info" />
    </receiver>
    
    <!-- Widget Configuration Activities -->
    <activity android:name="io.sukhuat.dingo.widget.weeklygoal.WeeklyGoalWidgetConfigActivity"
        android:exported="false">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
        </intent-filter>
    </activity>
    
    <activity android:name="io.sukhuat.dingo.widget.yearlynote.WidgetTextInputActivity"
        android:exported="false"
        android:theme="@style/Theme.Dingo.Fullscreen" />
        
</application>
```

### Existing Repository Integration
```kotlin
// Update existing GoalRepositoryImpl to support widget broadcasts
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val firebaseGoalService: FirebaseGoalService,
    private val context: Context // Add context for broadcasts
) : GoalRepository {
    
    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal.toEntity())
        
        // Broadcast to widgets
        val intent = Intent("io.sukhuat.dingo.GOAL_UPDATED")
        context.sendBroadcast(intent)
    }
}
```

## 📋 Development Timeline

### Week 1-2: Weekly Goal Widget Foundation
- [ ] Create :widget module structure
- [ ] Implement WeeklyGoalWidgetProvider
- [ ] Create basic RemoteViews layouts
- [ ] Setup data integration with existing GoalRepository
- [ ] Basic widget configuration

### Week 3-4: Weekly Goal Widget Features  
- [ ] Implement week navigation functionality
- [ ] Add multiple widget size support (2x2, 2x3, 3x2)
- [ ] Background sync with WorkManager
- [ ] Widget update broadcasts from main app
- [ ] Theme integration with Mountain Sunrise

### Week 5-6: Yearly Note Widget Foundation
- [ ] Implement YearlyNoteWidgetProvider  
- [ ] Create vintage paper UI layouts
- [ ] Integration with existing YearPlannerRepository
- [ ] Basic month content display
- [ ] Text input activity for editing

### Week 7-8: Yearly Note Widget Features
- [ ] Rich text processing and display
- [ ] Auto-save with 800ms debounce
- [ ] Month navigation functionality
- [ ] Multi-year support
- [ ] Real-time sync with main app

### Week 9: Testing & Polish
- [ ] Unit tests for both widgets
- [ ] Integration tests with main app
- [ ] UI testing on various devices
- [ ] Performance optimization
- [ ] Documentation updates

### Week 10: Release Preparation
- [ ] Final bug fixes
- [ ] Play Store assets for widgets
- [ ] User documentation
- [ ] Release notes
- [ ] Code review and approval

## 🧪 Testing Strategy

### Unit Testing
```kotlin
// Widget-specific tests
@RunWith(AndroidJUnit4::class)
class WeeklyGoalWidgetRepositoryTest {
    
    @Test
    fun `getGoalsForWeek returns correct goals for current week`() = runTest {
        // Test implementation
    }
    
    @Test
    fun `getGoalsForWeek filters goals by weekOfYear correctly`() = runTest {
        // Test implementation  
    }
}
```

### Integration Testing
```kotlin
@RunWith(AndroidJUnit4::class) 
@HiltAndroidTest
class WidgetIntegrationTest {
    
    @Test
    fun `widget updates when main app goals change`() {
        // Test widget broadcast updates
    }
    
    @Test
    fun `yearly note widget syncs with year planner`() {
        // Test data consistency
    }
}
```

## 🚀 Deployment Strategy

### Phased Rollout
1. **Internal Testing**: Team testing on various devices
2. **Beta Release**: Limited user group (100 users)
3. **Gradual Rollout**: 10% → 50% → 100% over 2 weeks
4. **Performance Monitoring**: Track widget crash rates, performance metrics

### Success Metrics
- **Adoption Rate**: 30%+ of active users add widgets within 30 days
- **Performance**: <1s widget load time, <0.1% crash rate
- **Engagement**: 20%+ increase in goal interaction frequency

## 📝 Notes & Considerations

### Technical Considerations
- **Battery Impact**: Minimize background updates, use efficient WorkManager scheduling
- **Memory Usage**: Optimize RemoteViews, cache widget configurations
- **Device Compatibility**: Test on various screen sizes and Android versions
- **Data Sync**: Handle offline scenarios gracefully

### UX Considerations  
- **Onboarding**: Clear instructions for widget setup and usage
- **Visual Consistency**: Maintain Mountain Sunrise theme across widgets
- **Accessibility**: Support TalkBack and large text sizes
- **Error Handling**: Graceful fallbacks when data unavailable

### Future Enhancements
- **Widget Stacks**: Group multiple weeks/months in single widget
- **Interactive Elements**: More advanced gestures and interactions
- **Customization**: User-defined themes and layouts
- **Smart Suggestions**: AI-powered content recommendations

---

**Document Version**: 1.0  
**Created**: [Current Date]  
**Last Updated**: [Current Date]  
**Author**: Development Team  
**Status**: Ready for Implementation

This comprehensive integration plan provides step-by-step guidance for implementing both Android widgets within the existing Dingo app architecture, ensuring seamless integration while maintaining code quality and user experience standards.