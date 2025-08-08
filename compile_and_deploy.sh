#!/bin/bash

# Compile, install, and launch APK on Google TV device
# Usage: ./compile_and_deploy.sh

set -e

# Configuration
DEVICE_IP="192.168.1.148"
DEVICE_PORT="5555"
DEVICE_SERIAL="${DEVICE_IP}:${DEVICE_PORT}"
PACKAGE_NAME="com.example.chan"
MAIN_ACTIVITY="com.example.chan/.MainActivity"

echo "🚀 Starting compile and deploy process..."

# Step 1: Compile the debug APK
echo "📦 Compiling debug APK..."
./gradlew assembleDebug --no-daemon

# Step 2: Find the compiled APK
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" -type f -printf "%T@ %p\n" | sort -nr | head -1 | cut -d' ' -f2-)
if [ -z "$APK_PATH" ]; then
    echo "❌ No APK found after compilation"
    exit 1
fi
echo "📱 Found APK: $APK_PATH"

# Step 3: Start ADB server
echo "🔌 Starting ADB server..."
adb start-server >/dev/null

# Step 4: Connect to device
echo "🔗 Connecting to $DEVICE_SERIAL..."
adb connect "$DEVICE_SERIAL" >/dev/null

# Step 5: Check device status
DEVICE_STATE=$(adb devices | awk -v s="$DEVICE_SERIAL" '$1==s {print $2}')
if [ "$DEVICE_STATE" != "device" ]; then
    echo "❌ Device not connected or not authorized. State: ${DEVICE_STATE:-none}"
    echo "   Please ensure wireless debugging is enabled and accept the ADB prompt on the TV."
    exit 1
fi
echo "✅ Device connected: $DEVICE_SERIAL"

# Step 6: Install APK
echo "📥 Installing APK..."
if ! adb -s "$DEVICE_SERIAL" install -r "$APK_PATH"; then
    echo "⚠️  Install failed, trying uninstall + reinstall..."
    adb -s "$DEVICE_SERIAL" uninstall "$PACKAGE_NAME" || true
    adb -s "$DEVICE_SERIAL" install -r "$APK_PATH"
fi

# Step 7: Launch app
echo "🚀 Launching app..."
adb -s "$DEVICE_SERIAL" shell am start -n "$MAIN_ACTIVITY" >/dev/null || true

echo "✅ Deploy complete! App should be running on your Google TV."
echo "   You can now test the popup menu via App Settings → Test Popup Menu"
