# Anon TV

<p align="center">
  <img src="images/icon.png" alt="Anon TV Logo" width="20%"/>
</p>

An Android TV application for browsing and viewing media content from web forums.

## Features

- Browse threads with media content
- Video playback (H.264, AAC, MP4, WebM)
- Dark theme support
- Favorites system with persistent storage
- Hide threads functionality
- Thread data caching

## Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center">
        <a href="Docs/Images/Menu_Sidebar_v2.png">
          <img src="Docs/Images/Menu_Sidebar_v2.png" alt="Menu Sidebar" width="300"/>
        </a>
        <br/>
        <em>Main interface with sidebar navigation</em>
      </td>
      <td align="center">
        <a href="Docs/Images/Menu_Threads_v2.png">
          <img src="Docs/Images/Menu_Threads_v2.png" alt="Menu Threads" width="300"/>
        </a>
        <br/>
        <em>Thread browsing with high-resolution icons</em>
      </td>
    </tr>
    <tr>
      <td align="center">
        <a href="Docs/Images/Video_Controls_v2.png">
          <img src="Docs/Images/Video_Controls_v2.png" alt="Video with Controls" width="300"/>
        </a>
        <br/>
        <em>Video player with overlay controls</em>
      </td>
      <td align="center">
        <a href="Docs/Images/Video_NoControls_v2.png">
          <img src="Docs/Images/Video_NoControls_v2.png" alt="Video without Controls" width="300"/>
        </a>
        <br/>
        <em>Full-screen video playback</em>
      </td>
    </tr>
  </table>
</div>

## Requirements

- Android TV device or emulator
- Android 5.0 (API 21) or higher
- Internet connection

## Installation

### Download Pre-built APK

1. Download the latest APK from [Releases](https://github.com/Ph-ill/Anon-TV/releases)
2. Install on your Android TV device

### Sideloading Instructions

#### Enable Developer Options
```
Settings > System > About > Android TV OS build (tap 7 times)
Settings > Apps > Security & Restrictions > Unknown sources (enable)
```

#### Installation Methods

**Method A: Send Files to TV**
1. Install [Send Files to TV](https://play.google.com/store/apps/details?id=com.yablio.sendfilestotv)
2. Transfer APK from phone to TV
3. Install using file manager

**Method B: ADB Installation**
```bash
adb install app-debug.apk
```

**Method C: Downloader App**
1. Install Downloader from Google Play Store
2. Enter APK URL from GitHub releases
3. Download and install directly

## Usage

- Navigate using your TV remote or controller
- Browse threads in the main section
- Long-press on thread cards to access options (favorite, hide)
- Access favorites and settings from the sidebar
- Use the back button to return to previous screens

## Development

Built with:
- Kotlin
- Android Leanback framework
- Ktor HTTP client
- Kotlinx Serialization

### Building from Source

```bash
git clone https://github.com/Ph-ill/Anon-TV.git
cd Anon-TV
./gradlew assembleDebug
```

## License

This project is open source and available under the [MIT License](LICENSE).