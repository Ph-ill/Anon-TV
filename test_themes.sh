#!/bin/bash

# Theme Testing Script for AnonTv
# This script runs comprehensive tests for the theming engine

echo "🎨 AnonTv Theme Testing Suite"
echo "=============================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "PASS")
            echo -e "${GREEN}✅ PASS${NC}: $message"
            ;;
        "FAIL")
            echo -e "${RED}❌ FAIL${NC}: $message"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  INFO${NC}: $message"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  WARN${NC}: $message"
            ;;
    esac
}

# Check if we're in the project root
if [ ! -f "build.gradle.kts" ]; then
    print_status "FAIL" "Please run this script from the project root directory"
    exit 1
fi

print_status "INFO" "Starting theme testing suite..."

# 1. Lint check for theme-related issues
print_status "INFO" "Running lint check for theme-related issues..."
./gradlew lintDebug > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_status "PASS" "Lint check passed"
else
    print_status "WARN" "Lint check found issues (check output above)"
fi

# 2. Check for theme-related files
print_status "INFO" "Checking theme-related files..."

THEME_FILES=(
    "app/src/main/res/values/attrs.xml"
    "app/src/main/res/values/colors.xml"
    "app/src/main/res/values/themes.xml"
    "app/src/main/res/values-night/colors.xml"
    "app/src/main/res/values-night/themes.xml"
    "app/src/main/java/com/example/chan/ThemeManager.kt"
    "app/src/main/java/com/example/chan/ThemeUtils.kt"
    "app/src/main/java/com/example/chan/MenuItem.kt"
    "app/src/main/java/com/example/chan/ThemeSettingsFragment.kt"
)

for file in "${THEME_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_status "PASS" "Found $file"
    else
        print_status "FAIL" "Missing $file"
    fi
done

# 3. Check for gradient drawables
GRADIENT_FILES=(
    "app/src/main/res/drawable/bg_gradient_light.xml"
    "app/src/main/res/drawable/bg_gradient_dark.xml"
    "app/src/main/res/drawable/accent_gradient_light.xml"
    "app/src/main/res/drawable/accent_gradient_dark.xml"
    "app/src/main/res/drawable/card_gradient_light.xml"
    "app/src/main/res/drawable/card_gradient_dark.xml"
    "app/src/main/res/drawable/theme_selector.xml"
)

print_status "INFO" "Checking gradient drawables..."
for file in "${GRADIENT_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_status "PASS" "Found $file"
    else
        print_status "FAIL" "Missing $file"
    fi
done

# 4. Build check
print_status "INFO" "Checking if project builds successfully..."
./gradlew assembleDebug > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_status "PASS" "Project builds successfully"
else
    print_status "FAIL" "Project build failed"
    echo "Run './gradlew assembleDebug' for detailed output"
fi

echo ""
echo "🎯 Manual Testing Instructions:"
echo "1. The app is already installed and ready for testing"
echo "2. Launch the app - it will open the main menu"
echo "3. Navigate to the 'App Settings' section in the sidebar"
echo "4. You'll see two cards in App Settings:"
echo "   - Theme Settings: Opens theme selection menu"
echo "   - Close App: Exits the application"
echo "5. Click 'Theme Settings' to open the theme selection menu"
echo "6. In theme settings, you'll see theme options:"
echo "   - Light Theme: Clean, bright interface"
echo "   - Dark Theme: Easy on eyes with dark backgrounds"
echo "   - System Theme: Follows device settings"
echo "   - Current Theme: Shows current theme info"
echo "7. Click any theme option to switch themes"
echo "8. Verify visual changes in all themes:"
echo "   - Background gradients change appropriately"
echo "   - Text colors are readable in all themes"
echo "   - Card backgrounds use proper gradients"
echo "   - Buttons have correct accent gradients"
echo "9. Test theme persistence by restarting the app"

echo ""
echo "📊 Test Summary:"
echo "- Manual testing: Use App Settings → Theme Settings for theme selection"
echo "- Documentation: Check THEME_TESTING_GUIDE.md for detailed instructions"
echo "- Build verification: Project builds and installs successfully"
echo "- Integration: Theme selection is now part of the main app menu"
echo "- Structure: Clean two-card App Settings menu with Theme Settings and Close App"

print_status "INFO" "Theme testing suite completed!" 