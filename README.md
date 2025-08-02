# Anon TV

## An Android TV Application for Browsing Imageboard Media

Anon TV is an Android TV application designed to provide a seamless and intuitive experience for browsing webm and image content from popular imageboards directly on your television. Built with a focus on performance and user experience on TV devices, it leverages modern Android development practices and robust libraries.

## Features

*   **Thread Browsing:** Easily navigate and view a list of active threads from configured imageboards.
*   **Media Playback:** Select a thread to view its associated media, including videos (webms) and images.
*   **Full-Screen Viewer:** Enjoy media in an immersive full-screen mode.
*   **Remote Navigation:** Intuitive navigation through media using standard TV remote controls (left/right for previous/next media item).
*   **Seamless Back Navigation:** Return to the thread list from the media viewer with a single press of the back button.
*   **Content Refresh:** Refresh the list of threads from the main menu to get the latest content.

## Tech Stack

*   **Language:** Kotlin - A modern, concise, and safe programming language for Android development.
*   **Build Tool:** Gradle - Powerful and flexible build automation system for Android projects.
*   **Video Player:** ExoPlayer - A highly customizable and performant media player for Android, ideal for handling various video formats.
*   **Networking:** Ktor - A flexible and asynchronous framework for building connected applications, used here for efficient API communication with imageboards.
*   **Serialization:** Kotlinx Serialization - For efficient and safe JSON parsing and object mapping with Ktor.
*   **AndroidX Libraries:** Modern Android Jetpack libraries for robust and maintainable app development.
*   **Leanback Library:** Specifically designed for building user interfaces on Android TV, ensuring an optimized and user-friendly experience.

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