# AnonTv Theming Engine Guide

## Overview

The AnonTv app now includes a comprehensive theming engine that allows users to choose between different visual themes. The system supports:

- **Light Theme**: Clean, bright interface with subtle gradients
- **Dark Theme**: Easy on the eyes with dark backgrounds and light text
- **System Theme**: Automatically follows the device's system theme setting

## Architecture

### 1. Theme Structure

```
res/
├── values/
│   ├── attrs.xml (custom theme attributes)
│   ├── colors.xml (base colors)
│   └── themes.xml (base theme)
├── values-night/
│   ├── colors.xml (dark theme colors)
│   └── themes.xml (dark theme)
└── drawable/
    ├── gradients/
    │   ├── bg_gradient_light.xml
    │   ├── bg_gradient_dark.xml
    │   ├── accent_gradient_light.xml
    │   ├── accent_gradient_dark.xml
    │   ├── card_gradient_light.xml
    │   └── card_gradient_dark.xml
    └── theme_selector.xml
```

### 2. Key Components

#### ThemeManager.kt
- Singleton class for managing theme preferences
- Handles theme switching and persistence
- Provides theme state information

#### ThemeUtils.kt
- Utility class for accessing theme colors and drawables
- Helper methods for theme-aware operations
- Color state list creation

#### ThemeSettingsFragment.kt
- UI for theme selection
- Allows users to switch between themes
- Provides immediate theme application

### 3. Custom Theme Attributes

The theming system defines custom attributes in `attrs.xml`:

```xml
<!-- Color attributes -->
<attr name="colorPrimary" format="color" />
<attr name="colorSecondary" format="color" />
<attr name="colorBackground" format="color" />
<attr name="colorTextPrimary" format="color" />

<!-- Gradient attributes -->
<attr name="backgroundGradient" format="reference" />
<attr name="accentGradient" format="reference" />
<attr name="cardGradient" format="reference" />
```

## Usage

### 1. In Layouts

Use theme attributes in your layouts:

```xml
<TextView
    android:textColor="?attr/colorTextPrimary"
    android:background="?attr/cardBackgroundDrawable" />
```

### 2. In Code

Access theme colors programmatically:

```kotlin
// Get theme colors
val primaryColor = ThemeUtils.getPrimaryColor(context)
val textColor = ThemeUtils.getTextPrimaryColor(context)

// Get theme drawables
val backgroundGradient = ThemeUtils.getBackgroundGradient(context)

// Check current theme
val isDark = ThemeUtils.isDarkTheme(context)
```

### 3. Theme Switching

```kotlin
val themeManager = ThemeManager.getInstance(context)

// Switch to dark theme
themeManager.setThemeMode(ThemeManager.ThemeMode.DARK)

// Switch to light theme
themeManager.setThemeMode(ThemeManager.ThemeMode.LIGHT)

// Follow system theme
themeManager.setThemeMode(ThemeManager.ThemeMode.SYSTEM)
```

## Adding New Themes

### 1. Create Theme Colors

Add a new `values-{theme-name}/colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorPrimary">#YOUR_PRIMARY_COLOR</color>
    <color name="colorSecondary">#YOUR_SECONDARY_COLOR</color>
    <!-- ... other colors -->
</resources>
```

### 2. Create Theme Styles

Add a new `values-{theme-name}/themes.xml`:

```xml
<resources>
    <style name="Theme.AnonTv" parent="Theme.Leanback">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="backgroundGradient">@drawable/bg_gradient_{theme-name}</item>
        <!-- ... other attributes -->
    </style>
</resources>
```

### 3. Create Gradient Drawables

Create gradient drawables in `drawable/`:

```xml
<!-- drawable/bg_gradient_{theme-name}.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:type="linear"
        android:angle="135"
        android:startColor="#START_COLOR"
        android:centerColor="#CENTER_COLOR"
        android:endColor="#END_COLOR" />
</shape>
```

### 4. Update ThemeManager

Add the new theme to the `ThemeMode` enum:

```kotlin
enum class ThemeMode(val value: Int) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    CUSTOM(AppCompatDelegate.MODE_NIGHT_NO) // Your new theme
}
```

## Best Practices

### 1. Always Use Theme Attributes

Instead of hardcoded colors, use theme attributes:

```xml
<!-- Good -->
android:textColor="?attr/colorTextPrimary"

<!-- Bad -->
android:textColor="#000000"
```

### 2. Use Gradient Drawables

For backgrounds and accent elements, use gradient drawables:

```xml
android:background="?attr/backgroundGradient"
```

### 3. Test All Themes

Always test your UI changes across all available themes to ensure consistency.

### 4. Consider Accessibility

Ensure sufficient contrast ratios between text and background colors in all themes.

## Troubleshooting

### Theme Not Applying

1. Check that the theme is properly defined in `themes.xml`
2. Verify that colors are defined in the corresponding `colors.xml`
3. Ensure gradient drawables exist and are referenced correctly

### Gradients Not Showing

1. Check that gradient drawables are properly defined
2. Verify that the gradient is referenced in the theme
3. Ensure the view has sufficient height/width to display the gradient

### Performance Issues

1. Use `ThemeUtils` for programmatic access instead of repeated `obtainStyledAttributes` calls
2. Cache theme colors and drawables when possible
3. Avoid creating new drawables in frequently called methods

## Future Enhancements

1. **Custom Theme Creator**: Allow users to create custom themes
2. **Theme Presets**: Pre-defined theme collections (e.g., "Ocean", "Sunset", "Forest")
3. **Dynamic Gradients**: Animated gradient backgrounds
4. **Theme Import/Export**: Share custom themes between users
5. **Per-Screen Themes**: Different themes for different screens or content types 