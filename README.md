# Dingo - Goal Tracking App

Dingo is a modern, visually appealing goal tracking application built with Jetpack Compose for Android. The app helps users manage and track their personal goals with a beautiful, intuitive interface.

## Features

### Core Functionality
- Create, edit, and manage personal goals
- Mark goals as complete, failed, or archived
- Visual indicators for goal status
- Weekly progress tracking
- Drag and drop to reorder goals

### Enhanced User Experience
- **Responsive Design**: Optimized for both phones and tablets with adaptive layouts
- **Animated Interactions**: 
  - Bubble scroll animations for a lively, interactive feel
  - Pop-in animations when goals first appear
  - Enhanced confetti celebration when goals are completed
  - Smooth drag and drop with visual indicators
  - Auto-scrolling when dragging near edges
- **Rich Media Support**:
  - Custom image uploads for goals
  - Built-in icon selection
  - Support for GIFs and stickers with a hand-drawn sticker pack
- **Feedback System**:
  - Sound effects with haptic feedback
  - Snackbar notifications for status changes
  - Visual cues for interactions
  - Celebratory animations with trophy display

### Technical Features
- Modern Jetpack Compose UI
- MVVM architecture
- Material 3 design system
- Hilt dependency injection
- Custom animations and graphics
- Responsive layout system

## UI Components

### Home Screen
The main screen features a responsive layout that adapts to different screen sizes:
- **Phone Layout**: Vertical arrangement with weekly overview at the top and goal grid below
- **Tablet Layout**: Horizontal split with statistics panel on the left and goal grid on the right

### Goal Cell
Interactive cells that represent individual goals with:
- Status indicators (completed, failed, archived)
- Custom images or icons
- Animated interactions during scrolling and dragging

### Bubble Editor
A floating editor that appears when long-pressing a goal, allowing users to:
- Edit goal text (if created within the last 30 minutes)
- Upload custom images, GIFs, or select stickers
- Archive goals

### Settings Dialog
Allows users to configure app preferences:
- Sound effects toggle
- Haptic feedback toggle
- Language selection

## Animation System
The app features a rich animation system including:
- **Enhanced Confetti Celebration**: Multiple particle shapes with physics simulation
- **Bubble Scroll Animations**: Dynamic transformations based on scroll position
- **Pop-in Animations**: Staggered entrance effects for content appearance
- **Drag Indicators**: Visual feedback during drag operations
- **Goal Completion Overlay**: Celebratory card with trophy and congratulatory message

## Responsive Design
The app automatically adapts to different screen sizes:
- Adjusts grid columns based on available space
- Scales content padding and spacing
- Optimizes layouts for both portrait and landscape orientations
- Provides enhanced statistics view on larger screens

## Sticker Pack
The app includes a custom hand-drawn sticker pack with various emotions:
- Happy face
- Sad face
- Love face with heart eyes
- Cool face with sunglasses
- Angry face
- Thinking face

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Kotlin 1.5.0 or newer
- Android SDK 21+

### Installation
1. Clone the repository
2. Open the project in Android Studio
3. Build and run on an emulator or physical device

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- Icons provided by [Material Design Icons](https://materialdesignicons.com/)
- Sound effects from [Freesound](https://freesound.org/)
