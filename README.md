# SmartFit

A modern Android fitness application built with Kotlin and Jetpack Compose.

## Overview

SmartFit is an Android fitness tracking application that helps users:
- Track daily activities (steps, workouts, calorie intake)
- View daily and weekly activity summaries
- Receive personalized fitness suggestions and tips from online resources
- Manage activity logs with add, edit, and delete capabilities
- Monitor progress towards daily fitness goals

The project is built using modern Android development technologies and best practices, demonstrating professional-grade mobile application architecture.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository Pattern
- **Local Storage**: Room Database & DataStore
- **Networking**: Retrofit/OkHttp for REST API calls
- **Concurrency**: Kotlin Coroutines
- **Navigation**: Jetpack Compose Navigation
- **Testing**: JUnit, Espresso, UI testing frameworks
- **Build System**: Gradle (Kotlin DSL)
- **Minimum API Level**: Configured via gradle.properties
- **IDE**: Android Studio (IntelliJ IDEA compatible)

## Project Structure

```
SmartFit/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/           # Kotlin source code
│   │   │   ├── res/              # Resources (layouts, drawables, strings)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # UI/Integration tests
│   └── build.gradle.kts
├── build.gradle.kts              # Root project build configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties and dependencies
├── gradlew & gradlew.bat         # Gradle wrapper scripts
└── .gitignore                    # Git ignore rules
```

## Features

### ✨ User Interface
- Material Design 3 implementation with Jetpack Compose
- Light and Dark theme support with seamless switching
- Accessibility features (proper contrast, content descriptions for images, readable text)
- Smooth animations for enhanced user experience

### 📊 Core Functionality
- **Activity Tracking**: Add, view, edit, and delete activity logs
- **Summaries**: Daily and weekly activity summaries with visual representations
- **Step Tracking**: Real-time step counting and progress visualization
- **Calorie Management**: Track calorie intake and expenditure
- **Fitness Tips**: Fetched suggestions from REST API endpoints

### 💾 Data Management
- **Local Persistence**: Room Database for offline activity log storage
- **User Preferences**: DataStore for theme mode, daily goals, and settings
- **Data Synchronization**: Seamless reading, writing, and updating of persisted data

### 📱 Design & Navigation
- **Multi-screen Navigation**: Home, Activity Log, Profile, and Settings screens
- **Data Passing**: Transfer selected activity details between screens
- **Dynamic Navigation**: Redirect to welcome or dashboard based on user state
- **Adaptive Layouts**: Responsive design for both phone and tablet orientations

## Architecture

This project follows clean architecture principles with:

- **Data Layer**: Repository pattern for abstracted data access (both local and remote)
- **Domain Layer**: Business logic and use cases (calorie calculations, goal progress)
- **Presentation Layer**: Jetpack Compose UI with MVVM pattern

**Dependency Injection**: Manual dependency injection is used for loose coupling and improved testability.

## API Integration

- Fetches workout suggestions and nutrition data from REST API endpoints
- Loads exercise images dynamically from internet sources
- Implements Kotlin Coroutines for non-blocking network calls
- Error handling and retry logic for network failures

## Getting Started

### Prerequisites

- Android Studio (2023.1 or later recommended)
- Java Development Kit (JDK 11 or later)
- Android SDK (API Level 24 and above)
- Gradle 8.0+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/zc100504/SmartFit.git
   cd SmartFit
   ```

2. Open in Android Studio and sync Gradle files

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the app on an emulator or connected device:
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

# Run all tests
./gradlew test

# Run UI tests on connected device
./gradlew connectedAndroidTest
```

## Testing

The project includes comprehensive testing coverage:

### Unit Tests
- Core feature verification (calorie calculations, step goal progress)
- Business logic validation
- Data layer operations

### UI Tests
- Navigation flow verification
- Correct display of fetched data from API
- User interaction scenarios

### Logging & Debugging
- Comprehensive logging statements for network requests
- Database operation monitoring
- Navigation flow tracking

Each group member has completed at least two unit tests and two UI tests as per assignment requirements.

Run tests with:
```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # UI tests (requires device/emulator)
```

## Group Members & Contributions

| Member | Key Responsibilities |
|--------|----------------------|
| [Member 1] | [Features/Components developed] |
| [Member 2] | [Features/Components developed] |
| [Member 3] | [Features/Components developed] |

**Debugging & Testing**: See the detailed report for individual contributions to debugging and testing.

## Documentation

- **User Manual**: Comprehensive guide with screenshots included in the report (TASKS section)
- **Video Demo**: Feature walkthrough demonstrating all functionality (TASKS section)
- **Code Documentation**: Inline comments explaining complex logic and API usage

## Troubleshooting

- **Gradle build fails**: Update Android Studio to the latest version and ensure JDK 11+ is installed
- **API not responding**: Check internet connection and verify API endpoint availability
- **Database migration errors**: Clear app data and rebuild the project
- **Emulator performance**: Allocate sufficient RAM and enable hardware acceleration

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for bug reports and feature requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or feedback, please open an issue on GitHub or contact the development team through the project repository.