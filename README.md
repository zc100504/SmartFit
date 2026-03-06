# SmartFit

A modern Android fitness application built with Kotlin and Compose.

## Overview

SmartFit is an Android application designed to help users track and manage their fitness activities. The project is built using modern Android development technologies and best practices.

## Tech Stack

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Minimum API Level**: Configured via gradle.properties
- **IDE**: Android Studio (IntelliJ IDEA compatible)

## Project Structure

```
SmartFit/
├── app/                          # Main application module
├── build.gradle.kts             # Root project build configuration
├── settings.gradle.kts          # Gradle settings
├── gradle.properties            # Gradle properties and dependencies
├── gradlew & gradlew.bat        # Gradle wrapper scripts
└── .gitignore                   # Git ignore rules
```

## Getting Started

### Prerequisites

- Android Studio (Latest version recommended)
- Java Development Kit (JDK 11 or later)
- Android SDK

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/zc100504/SmartFit.git
   cd SmartFit
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the app on an emulator or connected device:
   ```bash
   ./gradlew installDebug
   ```

## Building

Use the included Gradle wrapper to build the project:

```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Features

*(Add your app's key features here)*

## Contributing

Contributions are welcome! Please feel free to submit a pull request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or feedback, please open an issue on GitHub.