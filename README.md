# Anon TV

<img src="images/icon.png" alt="Anon TV Logo" width="20%"/>

An Android TV application for browsing imageboard media.

Anon TV provides a seamless experience for viewing webm and image content from popular imageboards directly on your television. It's built with modern Android development practices, focusing on performance and user experience on TV devices.

## Features

- Thread Browsing: Navigate and view active threads from imageboards.
- Media Playback: View videos (webms) and images from selected threads.
- Full-Screen Viewer: Immersive full-screen media experience.
- Remote Navigation: Use TV remote (left/right) for media navigation.
- Back Navigation: Easily return to the thread list.
- Content Refresh: Refresh threads from the main menu.

## Tech Stack

- **Language:** Kotlin
- **Build Tool:** Gradle
- **Video Player:** ExoPlayer
- **Networking:** Ktor
- **Serialization:** Kotlinx Serialization
- **Android Libraries:** AndroidX & Leanback

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

*   Android Studio (recommended for development and emulator setup)
*   Android SDK (API 34 or higher)
*   Java Development Kit (JDK) 1.8 or higher
*   Git

### Installation and Build

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

### Running on an Emulator or Device

1.  **Start an Android TV Emulator** via Android Studio's Device Manager, or connect a physical Android TV device with USB debugging enabled.
2.  **Install the APK:**
    ```bash
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```
3.  Launch "Anon TV" from your Android TV launcher.

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