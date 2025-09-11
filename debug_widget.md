# Widget Debug Guide

## Manual Debug Steps

### Step 1: Install and Check Basic App
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Check Widget Provider Registration
```bash
# Check if widget providers are registered
adb shell cmd package list packages io.sukhuat.dingo
adb shell dumpsys package io.sukhuat.dingo | grep -i widget
```

### Step 3: Monitor Logs During Widget Addition
```bash
# Clear logs first
adb logcat -c

# Start monitoring widget logs
adb logcat -s WeeklyGoalWidget WeeklyGoalWidgetProvider AndroidRuntime System.out:I

# Then add widget to home screen and watch logs
```

### Step 4: Force Widget Update
```bash
# Send broadcast to trigger widget update manually
adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE -n io.sukhuat.dingo/.widget.WeeklyGoalWidgetProvider
```

### Step 5: Check App Permissions
```bash
# Check if app has necessary permissions
adb shell dumpsys package io.sukhuat.dingo | grep -E "permissions|install"
```

## Expected Log Patterns

### Success Pattern:
```
WeeklyGoalWidgetProvider: === onUpdate called ===
WeeklyGoalWidget: === provideGlance START ===  
WeeklyGoalWidget: provideContent block started
WeeklyGoalWidget: ✅ provideContent completed successfully
```

### Failure Patterns:
- No logs → Provider not being called
- Exception logs → Code issue
- Timeout → Loading issue

## Common Issues

1. **Widget doesn't appear in widget list**
   - Check manifest registration
   - Verify widget info XML files
   - Check app permissions

2. **Widget shows but stuck on loading**
   - Check provideGlance execution
   - Verify Hilt injection
   - Check Firebase auth state

3. **Widget crashes**
   - Check exception logs
   - Verify required dependencies
   - Check thread issues

## Quick Tests

### Test 1: Basic App Function
- Open main app
- Login with Firebase
- Create some goals
- Check if goals save properly

### Test 2: Widget Provider Response  
- Add widget to home screen
- Check if onUpdate is called
- Verify logs show provider activation

### Test 3: Manual Widget Update
- Use broadcast command to trigger update
- Check if provideGlance is called
- Verify UI rendering