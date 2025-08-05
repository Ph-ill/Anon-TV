# Theme Testing Guide

## Overview

This guide provides comprehensive testing strategies for the AnonTv theming engine, covering manual testing, automated testing, and visual verification.

## 🧪 Testing Approaches

### 1. **Manual Testing (Recommended First Step)**

#### Quick Setup
1. **Build and Install**: Build the app and install it on your device/emulator
2. **Access Test Interface**: The app will launch the `ThemeTestActivity` which provides a dedicated testing interface
3. **Test Theme Switching**: Use the buttons to switch between Light, Dark, and System themes

#### What to Test Manually

**Visual Elements:**
- [ ] Background gradients change appropriately
- [ ] Text colors are readable in all themes
- [ ] Card backgrounds use proper gradients
- [ ] Buttons have correct accent gradients
- [ ] Color palette display shows correct colors

**Theme Switching:**
- [ ] Light theme applies correctly
- [ ] Dark theme applies correctly  
- [ ] System theme follows device settings
- [ ] Theme changes are immediate
- [ ] Theme persists after app restart

**Accessibility:**
- [ ] Text contrast is sufficient in all themes
- [ ] Colors are distinguishable for colorblind users
- [ ] Focus states are clearly visible
- [ ] Touch targets are appropriately sized

### 2. **Automated Testing**

#### Running Unit Tests
```bash
# Run all theme-related tests
./gradlew testDebugUnitTest

# Run specific test classes
./gradlew testDebugUnitTest --tests "com.example.chan.ThemeManagerTest"
./gradlew testDebugUnitTest --tests "com.example.chan.ThemeUtilsTest"
```

#### Test Coverage Areas

**ThemeManager Tests:**
- Default theme mode (should be SYSTEM)
- Theme mode switching (LIGHT ↔ DARK ↔ SYSTEM)
- Theme name retrieval
- Available themes list
- Theme persistence across app restarts

**ThemeUtils Tests:**
- Color retrieval from themes
- Gradient drawable access
- Text color state list creation
- Dark theme detection
- Current theme name retrieval

### 3. **Integration Testing**

#### Test Scenarios

1. **App Launch with Different Themes**
   ```kotlin
   // Test that app launches with correct theme
   val themeManager = ThemeManager.getInstance(context)
   themeManager.setThemeMode(ThemeManager.ThemeMode.DARK)
   // Restart app and verify dark theme is applied
   ```

2. **Theme Switching During Runtime**
   ```kotlin
   // Test theme switching without app restart
   themeManager.setThemeMode(ThemeManager.ThemeMode.LIGHT)
   // Verify UI updates immediately
   ```

3. **System Theme Integration**
   ```kotlin
   // Test system theme following
   themeManager.setThemeMode(ThemeManager.ThemeMode.SYSTEM)
   // Change device theme and verify app follows
   ```

### 4. **Visual Regression Testing**

#### Screenshot Comparison
1. Take screenshots of each theme
2. Compare with baseline images
3. Verify no unintended visual changes

#### Tools for Visual Testing
- **Android Studio Layout Inspector**: Real-time theme inspection
- **Screenshot Testing**: Automated visual regression testing
- **Accessibility Scanner**: Verify contrast ratios

### 5. **Performance Testing**

#### Memory Usage
```kotlin
// Test theme switching doesn't cause memory leaks
val initialMemory = Runtime.getRuntime().totalMemory()
// Switch themes multiple times
val finalMemory = Runtime.getRuntime().totalMemory()
// Verify memory usage is reasonable
```

#### Theme Switching Speed
```kotlin
// Measure theme switching performance
val startTime = System.currentTimeMillis()
themeManager.setThemeMode(ThemeManager.ThemeMode.DARK)
val endTime = System.currentTimeMillis()
// Verify switching is under 100ms
```

## 🔍 Testing Checklist

### Theme Application
- [ ] Light theme colors are bright and readable
- [ ] Dark theme colors are easy on the eyes
- [ ] System theme follows device settings
- [ ] All UI elements respect theme colors
- [ ] Gradients render correctly
- [ ] No hardcoded colors remain

### Theme Switching
- [ ] Switching is immediate
- [ ] No crashes during switching
- [ ] State is preserved during switching
- [ ] Animations are smooth
- [ ] Focus states update correctly

### Persistence
- [ ] Theme choice persists after app restart
- [ ] Theme choice persists after device reboot
- [ ] No conflicts with system theme changes
- [ ] SharedPreferences are properly saved

### Edge Cases
- [ ] App behavior with rapid theme switching
- [ ] App behavior during theme switching with active media
- [ ] App behavior with invalid theme data
- [ ] App behavior with missing gradient resources

## 🐛 Common Issues and Solutions

### Issue: Theme Not Applying
**Symptoms:** Colors don't change when switching themes
**Solutions:**
1. Check that theme attributes are properly defined in `attrs.xml`
2. Verify colors are defined in corresponding `colors.xml`
3. Ensure gradient drawables exist and are referenced correctly
4. Check that `ThemeManager` is properly initialized

### Issue: Gradients Not Showing
**Symptoms:** Gradient backgrounds appear solid or missing
**Solutions:**
1. Verify gradient drawables are properly defined
2. Check that gradients are referenced in themes
3. Ensure views have sufficient height/width
4. Verify gradient angles and colors are correct

### Issue: Performance Problems
**Symptoms:** Slow theme switching or memory leaks
**Solutions:**
1. Use `ThemeUtils` for programmatic access
2. Cache theme colors and drawables
3. Avoid creating new drawables in frequently called methods
4. Profile memory usage during theme switching

### Issue: Text Readability
**Symptoms:** Text is hard to read in certain themes
**Solutions:**
1. Check contrast ratios between text and background
2. Verify text colors are appropriate for each theme
3. Test with accessibility tools
4. Consider adding high contrast theme option

## 📱 Device Testing

### Test on Different Devices
- [ ] Android TV (primary target)
- [ ] Phone (for development)
- [ ] Tablet (if supported)
- [ ] Different screen sizes and densities

### Test Different Android Versions
- [ ] Android 10+ (primary support)
- [ ] Android 9 (if needed)
- [ ] Latest Android version

### Test Different Display Settings
- [ ] Different screen brightness levels
- [ ] Different font sizes
- [ ] Different display scaling
- [ ] High contrast mode (if available)

## 🚀 Continuous Integration

### Automated Testing Pipeline
```yaml
# Example CI configuration
- name: Run Theme Tests
  run: |
    ./gradlew testDebugUnitTest --tests "*Theme*"
    ./gradlew connectedAndroidTest --tests "*Theme*"
```

### Pre-commit Hooks
```bash
# Run theme tests before commit
./gradlew testDebugUnitTest --tests "*Theme*"
```

## 📊 Metrics to Track

### Performance Metrics
- Theme switching time (target: < 100ms)
- Memory usage during theme switching
- App startup time with different themes

### Quality Metrics
- Test coverage for theme-related code
- Number of theme-related bugs
- User satisfaction with theme options

### Accessibility Metrics
- Contrast ratio compliance
- Colorblind-friendly design
- Screen reader compatibility

## 🎯 Best Practices

1. **Test Early and Often**: Test theme changes as you implement them
2. **Use Real Devices**: Emulators may not accurately represent theme rendering
3. **Test in Different Lighting**: Check themes in bright and dim environments
4. **Document Visual Changes**: Keep screenshots of theme changes for reference
5. **Automate Where Possible**: Use automated tests for regression prevention
6. **Get User Feedback**: Test themes with actual users when possible

## 🔧 Debugging Tools

### Android Studio Tools
- **Layout Inspector**: Real-time theme inspection
- **Memory Profiler**: Check for memory leaks
- **CPU Profiler**: Measure theme switching performance

### Command Line Tools
```bash
# Check for theme-related lint issues
./gradlew lintDebug

# Run specific theme tests
./gradlew testDebugUnitTest --tests "*ThemeManager*"
```

### Logging
```kotlin
// Add logging to theme switching
Log.d("ThemeManager", "Switching to theme: ${themeMode.name}")
```

This comprehensive testing approach ensures your theming engine is robust, performant, and provides an excellent user experience across all supported themes. 