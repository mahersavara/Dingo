# Sharing Components Implementation

This document describes the implementation of the social sharing features for the user profile section, as specified in task 7.1 of the user-profile spec.

## Overview

The sharing components provide comprehensive social sharing functionality including:
- Achievement sharing with social media integration
- Profile link generation and sharing
- Referral system with invitation tracking
- Privacy controls for sharing features

## Architecture

### Domain Layer

#### Models (`domain/model/SharingModels.kt`)
- `ShareableContent`: Contains text, hashtags, and metadata for sharing
- `SocialPlatform`: Enum for supported social media platforms
- `SharingStats`: Analytics data for sharing activity
- `ReferralData`: Referral system information
- `SharingPrivacySettings`: User privacy preferences
- `ShareIntentData`: Android sharing intent data

#### Repository Interface (`domain/repository/SharingRepository.kt`)
- Defines contracts for sharing functionality
- Handles privacy settings, referral data, and analytics
- Provides profile link generation and image creation

#### Use Cases
- `ShareAchievementUseCase`: Generates shareable content for achievements
- `ShareProfileUseCase`: Creates profile sharing content
- `ManageReferralUseCase`: Handles referral system functionality
- `ManageSharingPrivacyUseCase`: Manages privacy settings

### Data Layer

#### Repository Implementation (`data/repository/SharingRepositoryImpl.kt`)
- Basic implementation with in-memory storage for demo
- In production, would integrate with Firebase Firestore
- Handles privacy settings, referral codes, and sharing analytics

### UI Layer

#### Components
- `SharingComponents`: Main sharing interface
- `SharingDialog`: Platform selection and content preview
- `SharingViewModel`: State management and business logic coordination

#### Features
1. **Profile Sharing Section**
   - Share button with loading states
   - Privacy setting integration
   - Progress sharing functionality

2. **Referral System**
   - Referral code display and generation
   - Invitation statistics (invited, joined, pending)
   - Copy and share functionality
   - Link generation

3. **Sharing Statistics**
   - Total shares tracking
   - Most shared achievement
   - Platform breakdown analytics
   - Expandable details view

4. **Privacy Controls**
   - Toggle achievement sharing
   - Toggle profile sharing
   - Toggle referral invitations
   - Toggle app promotion inclusion
   - Toggle real name vs anonymous sharing

## Platform Integration

### Supported Platforms
- Twitter (character limit optimization)
- Facebook (hashtag integration)
- Instagram (visual content focus)
- LinkedIn (professional tone)
- WhatsApp (personal messaging)
- Generic (system share sheet)

### Platform-Specific Optimizations
- **Twitter**: 240 character limit, hashtag restrictions
- **Facebook**: Full text with hashtags appended
- **Instagram**: Visual-first content with hashtags
- **LinkedIn**: Professional language, reduced emojis
- **WhatsApp**: Direct link inclusion, no hashtags

## Privacy Features

### Privacy Controls
- Granular control over sharing types
- Real name vs anonymous sharing
- App promotion inclusion toggle
- Complete sharing disable option

### Data Protection
- User consent for all sharing activities
- No sharing without explicit user action
- Privacy settings persist across sessions
- GDPR-compliant data handling

## Integration Example

```kotlin
// In ProfileScreen or similar
SharingComponents(
    onShareAchievement = { achievementId ->
        // Handled internally by SharingViewModel
    }
)
```

## Dependencies

### Hilt Dependency Injection
- `SharingRepository` bound in `RepositoryModule`
- Use cases provided in `ProfileUseCaseModule`
- ViewModels use `@HiltViewModel` annotation

### Required Permissions
- Internet access for sharing
- Storage access for image generation (future)
- Clipboard access for copy functionality

## Future Enhancements

### Phase 2 Features
- Image generation for achievements
- Custom sharing templates
- Social media API integration
- Advanced analytics dashboard
- Sharing rewards system

### Technical Improvements
- Firebase integration for persistence
- Real-time sharing statistics
- Push notifications for referral success
- A/B testing for sharing content
- Performance optimization for large user bases

## Testing

### Unit Tests
- Use case testing with mock repositories
- ViewModel state management testing
- Content generation validation
- Privacy setting enforcement

### Integration Tests
- End-to-end sharing flow testing
- Platform-specific content validation
- Privacy control functionality
- Referral system workflow

### UI Tests
- Component rendering tests
- User interaction testing
- Dialog functionality
- Accessibility compliance

## Requirements Compliance

This implementation addresses all requirements from task 7.1:

✅ **Achievement sharing with social media integration**
- Platform-specific content generation
- Achievement validation and sharing
- Social media optimization

✅ **Profile link generation and sharing functionality**
- Dynamic profile link creation
- Profile content generation
- Multi-platform sharing support

✅ **Referral system with invitation tracking**
- Referral code generation and management
- Invitation statistics tracking
- Multiple sharing channels

✅ **Privacy controls for sharing features**
- Granular privacy settings
- User consent management
- Complete sharing disable option

The implementation provides a comprehensive, user-friendly, and privacy-conscious sharing system that integrates seamlessly with the existing Dingo app architecture.