# Android-Specific Implementation Notes

This document covers Android-specific considerations, limitations, and best practices for the Mecka implementation.

## Android API Requirements

### Minimum SDK Version: 24 (Android 7.0 Nougat)

**Rationale:**
- Required for modern AccessibilityService APIs
- Supports Bluetooth LE and classic Bluetooth
- Provides necessary security APIs
- Ensures broad device compatibility

### Target SDK Version: 34 (Android 14)

**Rationale:**
- Latest stable Android version
- Includes recent security improvements
- Supports modern permission models
- Required for Google Play Store

## Permission Handling

### Required Permissions

```xml
<!-- Accessibility Service -->
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

<!-- Notification Listener -->
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />

<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Location (required for Bluetooth scanning on Android 12+) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Audio -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- System -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Runtime Permission Requests

Android 6.0+ requires runtime permission requests for dangerous permissions:

```kotlin
val permissionsToRequest = mutableListOf<String>().apply {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        add(Manifest.permission.RECORD_AUDIO)
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    // Add other permissions as needed
}

if (permissionsToRequest.isNotEmpty()) {
    ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), REQUEST_CODE_PERMISSIONS)
}
```

### Special Permission Handling

#### Accessibility Service
- Requires user to manually enable in Settings > Accessibility
- Cannot be enabled programmatically
- Must provide clear instructions to users

#### Notification Listener
- Requires user to manually enable in Settings
- Must provide clear instructions to users
- Check with `NotificationManagerCompat.getEnabledListenerPackages()`

## Background Execution Limits

### Android 8.0+ Background Restrictions

1. **Background Service Limits**: Services cannot run indefinitely in background
2. **Foreground Service Requirement**: Use `startForeground()` with notification
3. **Broadcast Receiver Limits**: Most implicit broadcasts don't work in background

### Workarounds Implemented

1. **Foreground Service**: `MeckaMainService` runs as foreground service
2. **JobScheduler**: For periodic tasks (not yet implemented)
3. **WorkManager**: For deferred tasks (not yet implemented)

### Battery Optimization

Users may need to disable battery optimization for Mecka:

```kotlin
val intent = Intent().apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
    }
}
context.startActivity(intent)
```

## Accessibility Service Quirks

### Gesture Dispatch Limitations

1. **API Level Requirements**: `dispatchGesture()` requires API 24+
2. **Permission Requirements**: Must be enabled in Accessibility settings
3. **Gesture Types Supported**:
   - Tap
   - Swipe
   - Long press
   - Custom paths

### Known Issues

1. **Gesture Timing**: Some apps may not respond to rapid gestures
2. **App Compatibility**: Some apps block accessibility interactions
3. **Performance**: Gesture dispatch can be slow on some devices

### Best Practices

1. **Add Delays**: Between gestures to ensure proper execution
2. **Error Handling**: Check gesture execution results
3. **Fallback Mechanisms**: Provide alternative interaction methods

## Bluetooth Implementation Notes

### Bluetooth Classic vs BLE

- **Classic Bluetooth**: Used for audio streaming
- **BLE**: Used for low-power device communication
- **Dual Mode**: Some earbuds support both

### Discovery Process

1. **Classic Discovery**: `BluetoothAdapter.startDiscovery()`
2. **BLE Scan**: `BluetoothLeScanner.startScan()`
3. **Permission Requirements**: Location permission required for scanning

### Connection Management

1. **Bonding**: Some devices require pairing/bonding
2. **Connection States**: Monitor connection state changes
3. **Audio Routing**: Handle audio focus and routing

### Known Issues

1. **Android 12+**: Stricter Bluetooth permission requirements
2. **Device Compatibility**: Some earbuds have proprietary protocols
3. **Latency**: Audio latency can vary by device

## Audio System Considerations

### Audio Focus Management

```kotlin
val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
val result = audioManager.requestAudioFocus(
    audioFocusChangeListener,
    AudioManager.STREAM_MUSIC,
    AudioManager.AUDIOFOCUS_GAIN
)
```

### Audio Routing

1. **Bluetooth Audio**: Prefer Bluetooth headset when available
2. **Speaker Fallback**: Use device speaker if Bluetooth unavailable
3. **Volume Control**: Respect system volume settings

### Known Issues

1. **Audio Latency**: Can be high on some devices
2. **Bluetooth Audio**: May require special handling
3. **Background Audio**: May be interrupted by other apps

## Notification Listener Limitations

### What Can Be Accessed

1. **Basic Info**: Package name, title, text
2. **Actions**: Notification actions (reply, dismiss, etc.)
3. **Priority**: Notification importance level

### What Cannot Be Accessed

1. **Sensitive Content**: Some apps hide notification content
2. **Media Content**: Images, videos in notifications
3. **Secure Notifications**: Banking, messaging apps may block access

### Best Practices

1. **Error Handling**: Gracefully handle missing data
2. **Permission Checks**: Verify listener is enabled
3. **User Education**: Explain notification access requirements

## Security Considerations

### Android Keystore

- **Hardware-Backed**: Keys stored in secure hardware when available
- **Key Attestation**: Verify key integrity
- **User Authentication**: Support for biometric authentication

### Encrypted Storage

- **EncryptedSharedPreferences**: For simple key-value storage
- **SQLCipher**: For encrypted database (future implementation)
- **Key Rotation**: Periodic key rotation for enhanced security

### Known Issues

1. **API Level**: Some security features require newer Android versions
2. **Device Support**: Not all devices have hardware-backed keystore
3. **Performance**: Encryption/decryption can impact performance

## Performance Optimization

### Memory Management

1. **Agent Isolation**: Each agent runs in its own coroutine scope
2. **Resource Cleanup**: Mandatory shutdown() implementation
3. **Leak Detection**: Use LeakCanary for memory leak detection

### CPU Usage

1. **Background Throttling**: Reduce CPU usage when app in background
2. **Priority Scheduling**: Critical agents get higher priority
3. **Battery Optimization**: Adapt to device power state

### Network Usage

1. **Offline-First**: All core functionality works without network
2. **Bandwidth Limits**: Configurable network usage
3. **Connection Management**: Handle network changes gracefully

## Testing Strategies

### Unit Testing

- **Mockito**: For mocking Android dependencies
- **Kotlin Coroutines Test**: For coroutine testing
- **JUnit**: Standard test framework

### Instrumented Testing

- **Espresso**: UI testing
- **UI Automator**: Cross-app testing
- **Accessibility Service Testing**: Special handling required

### Known Testing Challenges

1. **Accessibility Service**: Cannot be fully tested in emulators
2. **Bluetooth**: Requires physical devices
3. **Background Execution**: Hard to test reliably

## Debugging Tips

### Logging

```kotlin
// Initialize Timber
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

// Use structured logging
Timber.d("Agent %s: %s", agentId, message)
Timber.e(exception, "Error in agent %s", agentId)
```

### Debug Overlay

- Shows real-time agent states
- Displays current context
- Provides logging output
- Accessible via MainActivity

### ADB Commands

```bash
# Check running services
adb shell dumpsys activity services

# Check accessibility services
adb shell settings get secure enabled_accessibility_services

# Check notification listener
adb shell dumpsys notification

# Check Bluetooth state
adb shell dumpsys bluetooth_manager
```

## Common Pitfalls and Solutions

### 1. Accessibility Service Not Working

**Symptoms:**
- Gestures not executing
- No accessibility events received

**Solutions:**
- Verify service is enabled in Settings
- Check `onServiceConnected()` is called
- Verify manifest configuration
- Test on real device (emulators may have issues)

### 2. Bluetooth Discovery Failing

**Symptoms:**
- No devices found
- Discovery times out

**Solutions:**
- Check location permissions
- Verify Bluetooth is enabled
- Test with different devices
- Check for interference

### 3. Notification Listener Not Receiving Events

**Symptoms:**
- No notifications received
- `onNotificationPosted()` not called

**Solutions:**
- Verify listener is enabled in Settings
- Check manifest configuration
- Test with different apps
- Verify app is not in battery optimization

### 4. Audio Recording Issues

**Symptoms:**
- No audio input
- Recording fails silently

**Solutions:**
- Check microphone permission
- Verify audio source configuration
- Test with different sample rates
- Check for other apps using microphone

## Future Android-Specific Enhancements

1. **WorkManager Integration**: For reliable background tasks
2. **Jetpack Compose**: Modern UI implementation
3. **CameraX**: For potential computer vision features
4. **ML Kit**: On-device machine learning
5. **Health Connect**: For health data integration
6. **App Actions**: For better app integration
7. **Slices**: For quick access to features
8. **Bubbles**: For persistent UI elements

## Android Version-Specific Notes

### Android 10 (API 29)

- **Scoped Storage**: Changes to file access
- **Background Location**: Stricter permission requirements
- **Gesture Navigation**: May affect accessibility service

### Android 11 (API 30)

- **Package Visibility**: Restrictions on querying installed apps
- **One-Time Permissions**: Users can grant temporary permissions
- **Foreground Service Types**: Must specify service type

### Android 12 (API 31)

- **Bluetooth Permissions**: New BLE permission requirements
- **Exact Alarm Restrictions**: Limits on exact alarms
- **Foreground Service Launch Restrictions**: Stricter foreground service rules

### Android 13 (API 33)

- **Notification Permission**: Runtime permission for notifications
- **Media Permission**: Separate permission for media files
- **Background Bluetooth**: Stricter background Bluetooth rules

### Android 14 (API 34)

- **Foreground Service Types**: Additional service type requirements
- **Background Activity Launches**: More restrictions
- **Health Data Access**: New permissions for health data

## Best Practices for Android Development

1. **Follow Material Design**: For consistent UI/UX
2. **Handle Configuration Changes**: Screen rotation, locale changes
3. **Support Multiple Screen Sizes**: From phones to tablets
4. **Optimize for Battery**: Minimize wake locks and background work
5. **Handle Low Memory**: Gracefully handle memory pressure
6. **Test on Multiple Devices**: Different manufacturers and Android versions
7. **Follow Privacy Guidelines**: Respect user data and permissions
8. **Implement Proper Error Handling**: Graceful degradation
9. **Use Modern Android APIs**: Jetpack libraries, Kotlin coroutines
10. **Keep Up with Android Updates**: Stay current with new requirements

This document provides essential guidance for Android-specific implementation details, helping developers navigate the complexities of the Android platform while maintaining consistency with the cross-platform Mecka architecture.