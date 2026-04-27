# Urban Canopy Android App

The mobile application for the Urban Canopy ecosystem. This Android app empowers citizens to easily report and document urban safety violations and construction issues directly from their smartphones, seamlessly syncing data with the Urban Canopy Backend and Web Dashboard.

## Features

- **Real-Time Reporting:** Submit construction safety complaints instantly.
- **Media Capture:** Use the device camera and microphone to capture photos and audio evidence of violations.
- **Location Tagging:** Automatically attach GPS coordinates to reports to accurately pinpoint issues.
- **REST API Integration:** Securely communicates with the central Node.js/Express backend for data synchronization.
- **Native Android Experience:** Built with Kotlin, providing a fast and responsive user interface.

## Tech Stack

- **Platform:** Android
- **Language:** Kotlin
- **Build System:** Gradle
- **Key Libraries/Permissions:**
  - Camera & Audio Recording (Evidence capture)
  - Location Services (GPS tagging)
  - Google Maps API

## Prerequisites

- Android Studio (latest version recommended)
- Android SDK (API level 24 or higher)
- JDK 17 (or compatible version)

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Urban-Canopy-App
```

### 2. Configuration
- Open the project in Android Studio.
- Ensure the `google-services.json` file is placed in the `app/` directory if you are using Firebase services.
- Provide your Google Maps API key in the `AndroidManifest.xml` or via `local.properties` depending on your current setup. The current manifest uses a `com.google.android.geo.API_KEY` meta-data tag.

### 3. Build & Run
- Sync the project with Gradle files.
- Build the APK or run the app directly on an emulator or physical device.

## Project Structure

- `app/src/main/java/`: Contains the Kotlin source code (Activities, Fragments, ViewModels, etc.).
- `app/src/main/res/`: Contains Android resources (layouts, strings, drawables, and themes).
- `app/src/main/AndroidManifest.xml`: Application configuration, permissions, and manifest details.
- `build.gradle.kts`: Project and module-level build configurations.

## Architecture

This application communicates directly with the centralized backend REST API, deprecating direct Firebase SDK interactions for data flow to ensure a unified platform experience.

## License

This project is licensed under the MIT License.
