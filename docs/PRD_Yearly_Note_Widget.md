# 📔 Yearly Note Widget PRD

## 📋 Product Overview

**Product Name**: Yearly Note Widget  
**Version**: 1.0  
**Target Platform**: Android Home Screen Widget  
**Integration**: Dingo Vision Board App - Year Planner Feature

## 🎯 Product Vision

Create an elegant, journal-style Android home screen widget that provides quick access to monthly notes from Dingo's Year Planner, enabling users to capture thoughts, plans, and reflections directly from their home screen with a vintage, paper-like aesthetic.

## 📊 Problem Statement

Current Dingo Year Planner users need to navigate through the full app to:
- Add quick notes to their monthly plans
- Review current month's planning content
- Capture spontaneous thoughts and ideas for the year
- Access their yearly planning without app overhead

A home screen widget would provide instant note-taking and viewing capabilities, encouraging more frequent yearly planning engagement.

## 👥 Target Users

**Primary Users**: Dingo users who actively use the Year Planner feature for strategic planning
**Secondary Users**: Users who prefer quick note-taking and journaling on their device

**User Personas**:
- **The Strategic Planner**: Uses yearly planning for long-term goal setting
- **The Reflective Journaler**: Captures thoughts and insights throughout the year
- **The Quick Capturer**: Needs immediate access to jot down ideas
- **The Progress Reviewer**: Regularly reviews monthly progress and planning

## 🎯 Goals & Success Metrics

### Business Goals
- Increase Year Planner feature adoption by 30%
- Improve user retention for yearly planning by 25%
- Encourage more frequent content creation (+40% monthly notes)

### User Goals
- Instant access to current month's notes (< 3 seconds)
- Quick note capture without app navigation
- Beautiful, motivating writing experience
- Seamless synchronization with main Year Planner

### Success Metrics
- Widget adoption rate: 25%+ of Year Planner users
- Note creation frequency: 2x increase per month
- Average note length: 50+ words per entry
- Year Planner engagement: 40%+ increase in monthly access

## ✅ Core Requirements

### Functional Requirements

#### FR1: Monthly Note Display
- **Requirement**: Display current month's note content with vintage paper aesthetic
- **Acceptance Criteria**:
  - Show current month name and year prominently
  - Display note content with readable typography (serif font)
  - Support rich text formatting display (bullets, headings, etc.)
  - Scroll through content if it exceeds widget space
- **Priority**: P0 (Must Have)

#### FR2: Quick Note Editing
- **Requirement**: Allow direct text editing within the widget
- **Acceptance Criteria**:
  - Tap on content area to enter edit mode
  - Support basic text input with auto-save
  - Notion-style formatting shortcuts (*, -, #, etc.)
  - Character count indicator for long content
- **Priority**: P0 (Must Have)

#### FR3: Month Navigation
- **Requirement**: Navigate between months within current year
- **Acceptance Criteria**:
  - Vintage-style navigation arrows or month picker
  - Can access all 12 months of current year
  - Visual indicator showing current month vs. other months
  - Smooth transitions between months
- **Priority**: P1 (Should Have)

#### FR4: Multi-Year Support
- **Requirement**: Access notes from different years
- **Acceptance Criteria**:
  - Year selector or swipe gesture to change years
  - Can navigate to past and future years
  - Clear indication of which year is being viewed
  - Create new years automatically when accessed
- **Priority**: P2 (Could Have)

#### FR5: Real-Time Synchronization
- **Requirement**: Seamless sync with main app's Year Planner
- **Acceptance Criteria**:
  - Changes reflect in main app within 2 seconds
  - Auto-save with 800ms debounce (matching main app)
  - Offline support with sync queue
  - Conflict resolution for simultaneous edits
- **Priority**: P0 (Must Have)

#### FR6: Widget Customization
- **Requirement**: Customizable widget appearance and behavior
- **Acceptance Criteria**:
  - Multiple widget sizes (2x2, 3x2, 3x3, 4x3)
  - Theme options (vintage paper, dark mode, minimal)
  - Font size adjustment (small, medium, large)
  - Toggle month navigation visibility
- **Priority**: P1 (Should Have)

### Non-Functional Requirements

#### NFR1: Performance
- Widget load time: < 2 seconds
- Text input responsiveness: < 100ms
- Memory usage: < 30MB (due to rich text)
- Smooth scrolling at 60fps

#### NFR2: Visual Design
- Vintage paper aesthetic with parchment background
- Serif typography matching Year Planner feature  
- Ink-like text rendering with subtle shadows
- Glass morphism effects for modern Android integration

#### NFR3: Accessibility
- Support TalkBack screen reader
- High contrast mode compatibility
- Large text size support
- Keyboard navigation support

#### NFR4: Data Reliability
- Local cache for offline access
- Automatic backup before major changes
- Graceful handling of sync failures
- No data loss under normal Android lifecycle

## 🎨 User Experience Design

### Widget Layouts

#### 2x2 Layout (Compact Journal)
```
┌─────────────────────────────────┐
│ December 2024            [⚙]    │
├─────────────────────────────────┤
│ Today I realized that my goals  │
│ for this month are becoming     │
│ clearer. Need to focus on...    │
│                                 │
│ ⌨ Tap to write...              │
└─────────────────────────────────┘
```

#### 3x2 Layout (Standard Journal)
```
┌─────────────────────────────────────────────┐
│ 📔 December 2024              ← → [year]    │
├─────────────────────────────────────────────┤
│ Today I realized that my goals for this     │
│ month are becoming clearer. Need to focus   │
│ on the key priorities:                      │
│ • Finish the quarterly review               │
│ • Plan next year's strategic initiatives    │
│                                             │
│ ⌨ Tap to continue writing...               │
└─────────────────────────────────────────────┘
```

#### 3x3 Layout (Full Journal)
```
┌─────────────────────────────────────────────┐
│ 📔 December 2024              ← → [2024]    │
├─────────────────────────────────────────────┤
│ # Monthly Reflection                        │
│                                             │
│ Today I realized that my goals for this     │
│ month are becoming clearer. The vision      │
│ board approach is really working.           │
│                                             │
│ ## Key Priorities                           │
│ • Finish the quarterly business review      │
│ • Plan next year's strategic initiatives    │
│ • Complete the team reorganization          │
│                                             │
│ The mountain sunrise theme continues to     │
│ inspire me daily. Each morning feels like   │
│ a new opportunity to climb higher.          │
│                                             │
│ ⌨ Tap anywhere to continue...              │
└─────────────────────────────────────────────┘
```

### Visual Design Elements

#### Paper Texture
- Subtle parchment background texture
- Aged paper color (#F4E8D0 - matching Year Planner)
- Soft drop shadows for depth
- Slightly torn/worn edges for authenticity

#### Typography
- **Headers**: Serif font, dark brown (#3D2B1F)
- **Body text**: Georgia-style serif, comfortable line height
- **Formatting**: Subtle styling for bullets, headings, quotes
- **Placeholder**: Italic, lighter opacity

#### Interactive Elements
- Ink-well style edit cursor
- Vintage arrow buttons for navigation
- Subtle hover effects on interactive areas
- Loading states with paper texture shimmer

### Interaction Patterns

#### Primary Interactions
1. **Tap Content**: Enter editing mode with cursor placement
2. **Type Text**: Real-time editing with auto-save
3. **Navigate Months**: Arrow buttons or swipe gestures
4. **Access Settings**: Gear icon in corner

#### Advanced Interactions
1. **Long Press**: Select text or access formatting options
2. **Double Tap**: Quick access to month picker
3. **Pinch Zoom**: Adjust text size (if supported)
4. **Swipe**: Navigate between months (in edit mode)

#### Formatting Shortcuts
- `# ` → Heading text
- `* ` or `- ` → Bullet points  
- `1. ` → Numbered lists
- `> ` → Quote blocks
- `---` → Horizontal dividers

## 🏗️ Technical Architecture

### Data Integration

#### MonthData Model (Reusing existing)
```kotlin
data class MonthData(
    val index: Int, // 1-12
    val name: String, // "January", "February", etc.
    val content: String, // Rich text content
    val wordCount: Int,
    val lastModified: Long,
    val isPendingSync: Boolean = false
)
```

#### Widget-Specific Models
```kotlin
data class WidgetYearPlan(
    val year: Int,
    val currentMonth: MonthData,
    val syncStatus: SyncStatus,
    val lastSynced: Long
)

data class WidgetConfiguration(
    val widgetId: Int,
    val selectedYear: Int,
    val selectedMonth: Int,
    val theme: WidgetTheme,
    val fontSize: FontSize,
    val showNavigation: Boolean
)
```

### Widget Implementation

#### Core Architecture
```
YearlyNoteWidgetProvider (AppWidgetProvider)
├── YearlyNoteUpdateService
├── YearlyNoteConfigurationActivity  
├── YearlyNoteRemoteViewsFactory
├── RichTextRenderer
├── AutoSaveManager
└── YearPlannerWidgetRepository
```

#### Auto-Save Implementation
```kotlin
class WidgetAutoSaveManager(
    private val debounceTimeMs: Long = 800L,
    private val onSave: suspend (Int, Int, String) -> Unit
) {
    private val saveScope = CoroutineScope(Dispatchers.IO)
    private var saveJob: Job? = null
    
    fun onContentChange(year: Int, month: Int, content: String) {
        saveJob?.cancel()
        saveJob = saveScope.launch {
            delay(debounceTimeMs)
            onSave(year, month, content)
        }
    }
}
```

### Rich Text Rendering

#### Supported Formatting
- **Headings**: # → Bold, larger text
- **Bullets**: *, - → • bullet points
- **Numbers**: 1. → Ordered lists  
- **Quotes**: > → Indented, italic text
- **Dividers**: --- → Horizontal lines
- **Emphasis**: Future enhancement for *italic*, **bold**

#### Text Processing Pipeline
```
Raw Text → Markdown Parser → Styled Spans → RemoteViews Rendering
```

## 📱 Android Integration

### Widget Sizes & Layouts
- **2x2**: Compact journal (4 lines preview + edit)
- **3x2**: Standard journal (6 lines preview + navigation) 
- **3x3**: Full journal (10+ lines + rich formatting)
- **4x3**: Extended journal (full content + year navigation)
- **4x4**: Premium journal (maximum content + all features)

### Performance Optimizations
- **Text Rendering**: Cache formatted spans
- **Image Loading**: Lazy load background textures  
- **Memory Management**: Release unused month data
- **Battery Efficiency**: Smart sync scheduling

### Android API Integration
- **Jetpack Glance**: Modern widget framework (Android 12+)
- **RemoteViews**: Fallback for older Android versions
- **Work Manager**: Background sync scheduling
- **Room Database**: Local caching (shared with main app)

## 🔄 User Flows

### First-Time Setup Flow
1. User adds widget to home screen
2. Configuration screen shows year/month options
3. User selects preferred size and theme
4. Widget displays current month's content
5. Onboarding tooltip shows editing capabilities

### Daily Note-Taking Flow
1. User sees widget with current month's content
2. Taps on content area to start editing
3. Types new content or edits existing text
4. Auto-save occurs after 800ms of inactivity
5. Content syncs with main app automatically

### Monthly Review Flow
1. User navigates to previous months using arrows
2. Reviews past content and progress
3. Adds reflections or updates to previous entries
4. Returns to current month for ongoing planning
5. Long-term patterns become visible over time

### Cross-Device Sync Flow
1. User edits content on widget
2. Changes sync to Firebase via main app architecture
3. Other devices receive updates via existing sync mechanism
4. Conflict resolution handles simultaneous edits
5. All devices show consistent content within seconds

## 🚀 Implementation Phases

### Phase 1: Core Widget (5 weeks)
- Basic monthly note display
- Text editing with auto-save
- Current month navigation
- Firebase sync integration
- 2x2 and 3x2 layouts

### Phase 2: Rich Features (4 weeks)
- Multi-month navigation
- Rich text formatting support
- Multiple widget sizes
- Configuration options
- Performance optimizations

### Phase 3: Advanced Experience (3 weeks)
- Multi-year support
- Advanced text formatting
- Theme customization
- Gesture interactions
- Accessibility improvements

### Phase 4: Polish & Launch (2 weeks)
- Visual refinements
- Edge case handling
- Performance testing
- Documentation and assets

## 🧪 Testing Strategy

### Unit Tests
- Auto-save debouncing logic
- Text formatting parsing
- Month/year navigation
- Configuration persistence
- Sync conflict resolution

### Integration Tests  
- Firebase Year Planner integration
- Room database consistency
- Widget lifecycle management
- Background sync reliability

### UI Tests
- Text editing workflows
- Navigation interactions
- Configuration changes
- Theme switching
- Multi-size layout rendering

### Manual Testing
- Various Android versions and devices
- Different text input methods
- Network connectivity scenarios
- Battery optimization impacts
- Long-term usage patterns

## 🎯 Success Criteria

### Adoption & Engagement
- 25%+ of Year Planner users add the widget within 60 days
- 80%+ of widget users create content within first week
- 60%+ of users keep widget active for 30+ days
- 3x increase in monthly note content creation

### Content Quality
- Average note length: 50+ words per month
- 70%+ of months have meaningful content (>10 words)
- 90%+ sync success rate between widget and app
- <2% data loss incidents under normal usage

### Performance & Reliability
- Widget load time: <2 seconds (90th percentile)
- Text input latency: <100ms (95th percentile)  
- Memory usage: <30MB average
- Crash rate: <0.5% of widget operations

### User Satisfaction
- 4.6+ star rating in widget-related reviews
- 85%+ positive sentiment in feedback
- <10% widget removal rate within first month
- 75%+ prefer widget over main app for quick notes

## 📊 Analytics & Monitoring

### Key Metrics to Track
- Widget installation and configuration patterns
- Daily, weekly, and monthly content creation
- Month navigation usage patterns
- Text formatting feature adoption
- Auto-save frequency and success rates

### Content Analysis
- Average note length per month
- Most popular months for content creation
- Formatting patterns and preferences
- Time-of-day usage distribution
- Seasonal content trends

### Performance Monitoring
- Widget render times by device type
- Auto-save latency and failure rates
- Sync success rates and retry patterns  
- Memory usage over extended periods
- Battery impact measurements

### User Journey Analytics
- Configuration completion rates
- Feature discovery and adoption
- Drop-off points in setup flow
- Long-term retention patterns
- Cross-feature usage with main app

## 🔮 Future Enhancements

### Phase 2 Features (Q2/Q3)
- **Voice Input**: Speech-to-text for note capture
- **Image Integration**: Add photos to monthly notes
- **Templates**: Pre-formatted monthly planning templates
- **Export Options**: PDF/text export of yearly content
- **Search**: Find content across all months/years

### Advanced Features (Q4/Q5)
- **AI Suggestions**: Content ideas based on past entries
- **Habit Tracking**: Integration with goal completion data
- **Collaborative Notes**: Share specific months with others
- **Advanced Formatting**: Tables, checkboxes, links
- **Integration**: Connect with calendar and task apps

### Premium Features
- **Unlimited Years**: Beyond standard 3-year limit
- **Advanced Themes**: Custom backgrounds and fonts
- **Backup & Restore**: Manual export/import capabilities
- **Analytics**: Personal writing insights and trends
- **Multi-Device**: Specialized tablet and desktop widgets

## 📝 Appendices

### A. Technical Specifications
- **Minimum Android Version**: API 23 (Android 6.0)
- **Target Android Version**: API 34 (Android 14)
- **Required Permissions**: Internet, wake lock, storage access
- **Storage Requirements**: 10MB for rich text assets
- **Network Requirements**: Background data for sync

### B. Content Guidelines
- **Maximum Content**: 10,000 characters per month
- **Supported Languages**: All languages supported by main app
- **Text Formatting**: Markdown-style shortcuts
- **Image Support**: Future enhancement (not in v1.0)
- **Backup Frequency**: Real-time with 24-hour full backup

### C. Design Assets & Resources
- Vintage paper texture backgrounds (multiple variants)
- Serif font files (Georgia, Times alternatives)
- Navigation icons (vintage arrow designs)
- Loading animations (paper texture reveals)
- Configuration screen mockups
- Widget preview images for Play Store

### D. Integration Points
- **Main App**: Shared Room database and Firebase sync
- **Year Planner**: Direct integration with existing MonthData model
- **Notification System**: Optional reminders for monthly reviews
- **Backup System**: Leverage existing user data backup

---

**Document Version**: 1.0  
**Last Updated**: [Current Date]  
**Author**: Product Team  
**Reviewers**: Engineering, Design, UX Teams  
**Approval**: Product Manager

This comprehensive PRD outlines the Yearly Note Widget as a sophisticated companion to Dingo's Year Planner feature, providing elegant and convenient access to yearly planning and reflection capabilities directly from the Android home screen.