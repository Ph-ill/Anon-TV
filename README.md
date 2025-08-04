# Anon TV

<p align="center">
  <img src="images/icon.png" alt="Anon TV Logo" width="20%"/>
</p>

A Google TV application for browsing imageboard media.

Anon TV provides a seamless experience for viewing webm and image content from popular imageboards directly on your television. It's built with modern Android development practices, focusing on performance and user experience on TV devices.

## Features

- Thread Browsing: Navigate and view active threads from imageboards.
- Media Playback: View videos (webms) and images from selected threads.
- Full-Screen Viewer: Immersive full-screen media experience.
- Remote Navigation: Use TV remote (left/right) for media navigation.
- Back Navigation: Easily return to the thread list.
- Content Refresh: Refresh threads from the main menu.

## Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center">
        <a href="Docs/Images/Menu_Sidebar.png">
          <img src="Docs/Images/Menu_Sidebar.png" alt="Menu Sidebar" width="300"/>
        </a>
        <br/>
        <em>Main menu with sidebar navigation</em>
      </td>
      <td align="center">
        <a href="Docs/Images/Menu_Threads.png">
          <img src="Docs/Images/Menu_Threads.png" alt="Menu Threads" width="300"/>
        </a>
        <br/>
        <em>Thread browsing interface</em>
      </td>
    </tr>
    <tr>
      <td align="center">
        <a href="Docs/Images/Video_Controls.png">
          <img src="Docs/Images/Video_Controls.png" alt="Video with Controls" width="300"/>
        </a>
        <br/>
        <em>Video player with controls visible</em>
      </td>
      <td align="center">
        <a href="Docs/Images/Video_NoControls.png">
          <img src="Docs/Images/Video_NoControls.png" alt="Video without Controls" width="300"/>
        </a>
        <br/>
        <em>Immersive video viewing experience</em>
      </td>
    </tr>
  </table>
</div>

## Tech Stack

- **Language:** Kotlin
- **Build Tool:** Gradle
- **Video Player:** ExoPlayer
- **Networking:** Ktor
- **Serialization:** Kotlinx Serialization
- **Android Libraries:** AndroidX & Leanback

## Getting Started

There are two ways to get Anon TV running on your Android TV device: building from source or sideloading the pre-built APK.

### Option 1: Sideloading the APK (Recommended for most users)


#### 1. Download the APK

1. Visit the [Anon TV GitHub Releases Page](https://github.com/Ph-ill/Anon-TV/releases).
2. Download the latest `.apk` file under the most recent release (e.g., `AnonTV-vX.X.X.apk`).

#### 2. Prepare Your Google TV for Sideloading

1. On your Google TV:

   * Go to **Settings > System > About**
   * Scroll down to **Android TV OS build** and tap it **7 times** to enable **Developer Options**.
2. Go back to **Settings > Apps > Security & Restrictions**:

   * Enable **Unknown Sources** for the file manager or sideload app you’ll use (e.g., *Send Files to TV*, *Downloader*, or *X-plore File Manager*).

#### 3. Transfer the APK to Your Google TV

Choose one of these methods:

##### Option A: Using "Send Files to TV"

1. Install [Send Files to TV](https://play.google.com/store/apps/details?id=com.yablio.sendfilestotv) on both your phone and Google TV.
2. Send the `.apk` file from your phone to your Google TV.
3. Use a file manager on your TV (like **FX File Explorer** or **X-plore**) to locate and open the APK.

##### Option B: Using "Downloader" App

1. Install the **Downloader** app from the Play Store on your Google TV.
2. Open it and enter the direct link to your APK (host it elsewhere or use a GitHub raw link).
3. Download and install the APK directly.

#### 4. Install Anon TV

1. Locate the transferred APK on your TV.
2. Open it and follow the on-screen prompts to install.
3. Launch **Anon TV** from your app drawer.


### Option 2: Building from Source (For Developers)

This approach is for developers who want to modify the code or contribute to the project.

#### Prerequisites

*   Android Studio (recommended for development and emulator setup)
*   Android SDK (API 34 or higher)
*   Java Development Kit (JDK) 1.8 or higher
*   Git

#### Build Instructions

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Ph-ill/Anon-TV.git
    cd Anon-TV
    ```

2.  **Set up Android SDK (if not already configured):**
    Create a `local.properties` file in the project root and add your SDK path:
    ```properties
    sdk.dir=/path/to/your/android/sdk
    ```
    (e.g., `/home/youruser/Android/Sdk`)

3.  **Build the project:**
    ```bash
    ./gradlew assembleDebug
    ```
    The debug APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

4.  **Install on device/emulator:**
    ```bash
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```

### Troubleshooting

#### Common Issues

- **"App not installed" error**: Make sure USB debugging is enabled and you've allowed installation from unknown sources
- **ADB not recognized**: Install Android SDK Platform Tools or use Android Studio's built-in ADB
- **Build errors**: Ensure you have the correct JDK version and Android SDK installed
- **Emulator issues**: Use Android Studio's Device Manager to create and manage Android TV emulators

#### Getting ADB

- **Windows**: Download [Android SDK Platform Tools](https://developer.android.com/studio/releases/platform-tools)
- **macOS**: `brew install android-platform-tools`
- **Linux**: `sudo apt install adb` (Ubuntu/Debian) or equivalent for your distribution

## Future Enhancements (Roadmap)

*   **Multi-Imageboard Support:** Allow users to configure and browse content from various imageboards beyond the initial implementation.
*   **Search Functionality:** Implement search capabilities for threads and posts.
*   **User Interface Improvements:** Enhance the visual design and add more customization options.
*   **Caching:** Implement local caching for faster loading times and offline browsing.
*   **Settings Screen:** Add a dedicated settings screen for user preferences.
*   **Error Handling & Feedback:** More robust error handling and user-friendly feedback mechanisms.

## Contributing

Contributions are welcome! If you have suggestions for improvements or new features, please open an issue or submit a pull request.

## License

Distributed under the MIT License. See `LICENSE` for more information.