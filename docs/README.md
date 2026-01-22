# Mecka Android Baseline Implementation

This is the Android reference implementation for Mecka agentic operating intelligence. This baseline validates the core architecture and serves as the stress-test for the Platform Abstraction Layer (PAL) design.

## Quick Start

### Prerequisites
- Android Studio (latest stable version)
- Android SDK 24+ (minSdk 24, targetSdk 34)
- Java 17
- Kotlin 1.9.0+

### Building the Project

1. Clone the repository:
```bash
git clone https://github.com/mecka-ai/mecka-android.git
cd mecka-android
```

2. Open in Android Studio:
- File > Open > Select the `android` directory
- Let Android Studio sync Gradle

3. Build the project:
- Build > Make Project
- Or use command line: `./gradlew assembleDebug`

### Running the App

1. Connect an Android device (API 24+) or start an emulator
2. Run > Run 'app'
3. Grant required permissions when prompted

### Enabling Required Services

#### Accessibility Service
1. Go to Settings > Accessibility > Installed Services
2. Find "Mecka Accessibility Service"
3. Enable the service

#### Notification Listener
1. Go to Settings > Apps & notifications > Special app access > Notification access
2. Find "Mecka" and enable notification access

## Feature Flags

The project supports several build variants with different feature sets:

- **debug**: Standard debug build with all features
- **devMode**: Development build with additional logging
- **rootAccess**: Build with root access features (if available)

Feature flags are defined in `build.gradle.kts`:

```kotlin
productFlavors {
    standard {
        buildConfigField("boolean", "FEATURE_FULL_ACCESSIBILITY", "true")
        buildConfigField("boolean", "FEATURE_BLUETOOTH_EARBUD", "true")
        buildConfigField("boolean", "FEATURE_NOTIFICATION_LISTENER", "true")
        buildConfigField("boolean", "FEATURE_ADVANCED_SECURITY", "true")
    }
    
    limited {
        buildConfigField("boolean", "FEATURE_FULL_ACCESSIBILITY", "false")
        buildConfigField("boolean", "FEATURE_BLUETOOTH_EARBUD", "true")
        buildConfigField("boolean", "FEATURE_NOTIFICATION_LISTENER", "false")
        buildConfigField("boolean", "FEATURE_ADVANCED_SECURITY", "false")
    }
}
```

## Core Components

### Agent Framework
- **AgentBase**: Base class for all agents with lifecycle management
- **AgentRegistry**: Manages agent registration, lifecycle, and messaging
- **MessageRouter**: Handles inter-agent communication with priority routing

### Services
- **GenosAccessibilityService**: Core interaction service using Android Accessibility APIs
- **MeckaNotificationListenerService**: Notification interception and parsing
- **MeckaMainService**: Main foreground service coordinating all agents

### MVP Agents
- **ListenerAgent**: Audio input, wake word detection, command extraction
- **ExecutorAgent**: Command execution via Accessibility Service
- **ContextAgent**: Device state tracking and context management

### Security
- **SecurityManager**: Encryption, secure storage, earbud authentication
- **HardwareManager**: Bluetooth, audio, and hardware interface management

## Testing

### Unit Tests
Run unit tests with:
```bash
./gradlew test
```

### Instrumented Tests
Run on-device tests with:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
Minimum 70% coverage required for core agent runtime components.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md): Detailed architecture overview
- [PAL_INTERFACES.md](PAL_INTERFACES.md): Platform Abstraction Layer interfaces
- [ANDROID_NOTES.md](ANDROID_NOTES.md): Android-specific implementation notes
- [API_REFERENCE.md](API_REFERENCE.md): Agent and runtime API reference
- [SECURITY.md](SECURITY.md): Security model and implementation details

## Building for Release

1. Generate signed APK:
```bash
./gradlew assembleRelease
```

2. The signed APK will be in `app/build/outputs/apk/release/`

## Contributing

Please follow the existing code style and architecture patterns. All contributions should:
- Maintain 70%+ test coverage
- Follow Kotlin coding conventions
- Include appropriate documentation
- Respect the agentic architecture principles

## License

This project is licensed under CC0 (Public Domain). See LICENSE for details.