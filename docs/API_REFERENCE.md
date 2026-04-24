# Mecka Android API Reference

This document provides detailed API reference for the Mecka Android implementation, including all major classes, interfaces, and methods.

## Core Agent Framework

### AgentBase

**Base class for all Mecka agents**

```kotlin
abstract class AgentBase(
    val agentId: String,
    val agentName: String,
    val agentDescription: String,
    protected val stateManager: StateManager,
    protected val securityManager: SecurityManager,
    protected val hardwareManager: HardwareManager
)
```

#### Lifecycle Methods

```kotlin
// Initialize the agent
open fun initialize(): Boolean

// Start the agent
open fun start(): Boolean

// Pause the agent
open fun pause(): Boolean

// Resume the agent
open fun resume(): Boolean

// Shutdown the agent
open fun shutdown(): Boolean
```

#### Message Handling

```kotlin
// Handle incoming messages
open fun onMessage(message: AgentMessage): Boolean

// Handle errors
open fun onError(error: Exception): Boolean

// Handle state changes
open fun onStateChanged(stateName: String, newValue: Any)
```

#### Utility Methods

```kotlin
// Send a message to another agent
protected fun sendMessage(message: AgentMessage)

// Get current agent state
protected fun getCurrentState(): AgentState
```

### AgentRegistry

**Manages agent registration and lifecycle**

```kotlin
class AgentRegistry(
    private val context: Context,
    private val stateManager: StateManager,
    private val securityManager: SecurityManager,
    private val hardwareManager: HardwareManager
)
```

#### Agent Management

```kotlin
// Register an agent
fun registerAgent(agent: AgentBase): Boolean

// Unregister an agent
fun unregisterAgent(agentId: String): Boolean

// Get an agent by ID
fun getAgent(agentId: String): AgentBase?

// Start an agent
fun startAgent(agentId: String): Boolean

// Stop an agent
fun stopAgent(agentId: String): Boolean
```

#### Bulk Operations

```kotlin
// Initialize all agents
fun initializeAllAgents()

// Start all agents
fun startAllAgents()

// Shutdown all agents
fun shutdownAllAgents()
```

#### Messaging

```kotlin
// Send a message to a specific agent
fun sendMessage(message: AgentMessage)

// Broadcast a message to all agents
fun broadcastMessage(message: AgentMessage)
```

#### Utility Methods

```kotlin
// Get number of registered agents
fun getAgentCount(): Int

// Get all agent IDs
fun getAllAgentIds(): List<String>
```

## Inter-Agent Communication

### AgentMessage

**Data class for inter-agent communication**

```kotlin
data class AgentMessage(
    val messageId: String,
    val sourceAgentId: String,
    val targetAgentId: String,
    val intent: String,
    val priority: MessagePriority,
    val payload: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
```

### MessagePriority

**Priority levels for messages**

```kotlin
enum class MessagePriority {
    CRITICAL,  // Security, safety, immediate response
    HIGH,      // User interactions, time-sensitive
    NORMAL,    // Standard operations
    LOW        // Background updates, analytics
}
```

### MessageRouter

**Handles message routing between agents**

```kotlin
class MessageRouter
```

#### Routing Methods

```kotlin
// Route a message to the appropriate agent(s)
suspend fun routeMessage(message: AgentMessage, agents: Map<String, AgentBase>)

// Broadcast a message to all agents
suspend fun broadcastMessage(message: AgentMessage, agents: Map<String, AgentBase>)

// Prioritize messages by priority
fun prioritizeMessages(messages: List<AgentMessage>): List<AgentMessage>
```

## State Management

### StateManager

**Manages shared state across agents**

```kotlin
class StateManager(private val context: Context)
```

#### State Operations

```kotlin
// Set a state value
fun setState(key: String, value: Any)

// Get a state value
fun getState(key: String): Any?

// Remove a state value
fun removeState(key: String)

// Clear all state
fun clearAllState()
```

#### Listener Management

```kotlin
// Register a state change listener
fun registerListener(key: String, listener: StateChangeListener)

// Unregister a state change listener
fun unregisterListener(key: String, listener: StateChangeListener)
```

### StateChangeListener

**Interface for state change notifications**

```kotlin
interface StateChangeListener {
    fun onStateChanged(key: String, newValue: Any?)
}
```

## Security Components

### SecurityManager

**Handles encryption, secure storage, and authentication**

```kotlin
class SecurityManager(private val context: Context)
```

#### Key Management

```kotlin
// Initialize security manager
fun initialize(): Boolean

// Generate encryption key
fun generateEncryptionKey(alias: String): Boolean
```

#### Encryption

```kotlin
// Encrypt data
fun encryptData(alias: String, data: ByteArray): ByteArray?

// Decrypt data
fun decryptData(alias: String, encryptedData: ByteArray): ByteArray?
```

#### Secure Storage

```kotlin
// Store value securely
fun storeSecureValue(key: String, value: String): Boolean

// Retrieve secure value
fun retrieveSecureValue(key: String): String?

// Remove secure value
fun removeSecureValue(key: String): Boolean
```

#### Earbud Authentication

```kotlin
// Validate earbud authentication
fun validateEarbudAuthentication(macAddress: String): Boolean

// Derive key from earbud MAC
fun deriveKeyFromEarbudMac(macAddress: String): String
```

## Hardware Components

### HardwareManager

**Manages Bluetooth, audio, and hardware interfaces**

```kotlin
class HardwareManager(private val context: Context)
```

#### Bluetooth Management

```kotlin
// Initialize hardware manager
fun initialize(): Boolean

// Shutdown hardware manager
fun shutdown()

// Start Bluetooth discovery
fun startBluetoothDiscovery(): Boolean

// Stop Bluetooth discovery
fun stopBluetoothDiscovery(): Boolean

// Get paired devices
fun getPairedDevices(): List<BluetoothDevice>

// Check if Bluetooth is enabled
fun isBluetoothEnabled(): Boolean
```

#### Audio Management

```kotlin
// Get audio output devices
fun getAudioOutputDevices(): List<AudioDeviceInfo>

// Set audio output to Bluetooth
fun setAudioOutputToBluetooth(): Boolean
```

## Services

### GenosAccessibilityService

**Core accessibility service for UI interaction**

```kotlin
class GenosAccessibilityService : AccessibilityService()
```

#### Service Lifecycle

```kotlin
// Called when service is connected
override fun onServiceConnected()

// Called when service is interrupted
override fun onInterrupt()

// Called when service is unbound
override fun onUnbind(intent: Intent?): Boolean

// Called when service is destroyed
override fun onDestroy()
```

#### Accessibility Events

```kotlin
// Handle accessibility events
override fun onAccessibilityEvent(event: AccessibilityEvent)
```

#### Gesture Execution

```kotlin
// Perform tap gesture
fun performTap(x: Int, y: Int): Boolean

// Perform swipe gesture
fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long): Boolean

// Perform scroll gesture
fun performScroll(direction: String, amount: Int): Boolean

// Launch application
fun launchApp(packageName: String): Boolean

// Type text
fun typeText(text: String): Boolean

// Wait for duration
fun wait(duration: Long): Boolean
```

### MeckaNotificationListenerService

**Notification listener service**

```kotlin
class MeckaNotificationListenerService : NotificationListenerService()
```

#### Service Lifecycle

```kotlin
// Called when listener is connected
override fun onListenerConnected()

// Called when listener is disconnected
override fun onListenerDisconnected()
```

#### Notification Events

```kotlin
// Called when notification is posted
override fun onNotificationPosted(sbn: StatusBarNotification)

// Called when notification is removed
override fun onNotificationRemoved(sbn: StatusBarNotification)
```

### MeckaMainService

**Main foreground service**

```kotlin
class MeckaMainService : Service()
```

#### Service Lifecycle

```kotlin
// Called when service is created
override fun onCreate()

// Called when service is started
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int

// Called when service is bound
override fun onBind(intent: Intent): IBinder?

// Called when service is destroyed
override fun onDestroy()
```

## MVP Agents

### ListenerAgent

**Handles audio input and command detection**

```kotlin
class ListenerAgent(
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(...)
```

#### Audio Management

```kotlin
// Initialize audio system
override fun initialize(): Boolean

// Start audio recording
override fun start(): Boolean

// Stop audio recording
fun stopRecording()
```

#### Command Detection

```kotlin
// Process audio data
private suspend fun processAudio()

// Detect wake word
private fun detectWakeWord(audioData: ShortArray): Boolean

// Detect command
private fun detectCommand()
```

### ExecutorAgent

**Executes commands via accessibility service**

```kotlin
class ExecutorAgent(
    private val context: Context,
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(...)
```

#### Command Execution

```kotlin
// Handle command messages
override fun onMessage(message: AgentMessage): Boolean

// Handle detected commands
private fun handleCommandDetected(message: AgentMessage)

// Check command permissions
private fun checkCommandPermissions(command: String): Boolean
```

#### Specific Commands

```kotlin
// Execute open camera command
private fun executeOpenCamera()

// Execute open settings command
private fun executeOpenSettings()

// Execute go back command
private fun executeGoBack()

// Execute go home command
private fun executeGoHome()
```

#### Result Reporting

```kotlin
// Send execution result
private fun sendExecutionResult(status: String, message: String, success: Boolean)
```

### ContextAgent

**Tracks device state and context**

```kotlin
class ContextAgent(
    private val context: Context,
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(...)
```

#### State Management

```kotlin
// Initialize context agent
override fun initialize(): Boolean

// Start periodic updates
override fun start(): Boolean

// Handle messages
override fun onMessage(message: AgentMessage): Boolean
```

#### Context Updates

```kotlin
// Update device state
private fun updateDeviceState()

// Handle window changes
private fun handleWindowChanged(message: AgentMessage)

// Handle wake word detection
private fun handleWakeWordDetected()

// Handle action execution
private fun handleActionExecuted(message: AgentMessage)

// Handle notifications
private fun handleNotificationReceived(message: AgentMessage)
```

#### Broadcasting

```kotlin
// Broadcast context changes
private fun broadcastContextChange(changeType: String, details: Map<String, Any>)
```

## Data Structures

### AgentState

**Agent lifecycle states**

```kotlin
enum class AgentState {
    INITIALIZING,  // Loading dependencies, permissions
    IDLE,          // Waiting for triggers/events
    RUNNING,       // Active task execution
    PAUSED,        // Temporarily paused
    SHUTDOWN       // Clean shutdown complete
}
```

### AudioDeviceInfo

**Audio device information**

```kotlin
data class AudioDeviceInfo(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean
)
```

## Utility Classes

### BuildConfig

**Build-time configuration**

```kotlin
object BuildConfig {
    const val DEBUG = true/false
    const val DEV_MODE = true/false
    const val ROOT_ACCESS = true/false
    const val FEATURE_FULL_ACCESSIBILITY = true/false
    const val FEATURE_BLUETOOTH_EARBUD = true/false
    const val FEATURE_NOTIFICATION_LISTENER = true/false
    const val FEATURE_ADVANCED_SECURITY = true/false
}
```

## Best Practices

### Agent Development

1. **Follow Lifecycle**: Implement all lifecycle methods
2. **Handle Errors**: Provide robust error handling
3. **Clean Resources**: Release resources in shutdown()
4. **Use Coroutines**: For asynchronous operations
5. **Respect Priority**: Handle messages according to priority

### Message Handling

1. **Validate Messages**: Check message format and content
2. **Handle Unknown Intents**: Gracefully handle unexpected messages
3. **Respond Appropriately**: Send appropriate responses
4. **Log Important Messages**: For debugging and auditing
5. **Handle Timeouts**: For long-running operations

### State Management

1. **Use Descriptive Keys**: Clear, consistent naming
2. **Handle Type Safety**: Ensure proper type handling
3. **Limit State Size**: Avoid storing large objects
4. **Clean Up Unused State**: Remove obsolete state values
5. **Handle Listeners Carefully**: Prevent memory leaks

### Security

1. **Use Secure Storage**: For sensitive data
2. **Validate Inputs**: Prevent injection attacks
3. **Handle Encryption Errors**: Graceful degradation
4. **Rotate Keys**: Periodic key rotation
5. **Audit Access**: Monitor security operations

### Performance

1. **Limit Background Work**: Respect battery life
2. **Optimize Coroutines**: Proper dispatcher usage
3. **Manage Memory**: Prevent leaks and bloat
4. **Handle Large Data**: Stream or chunk large operations
5. **Monitor Performance**: Track agent execution times

This API reference provides comprehensive documentation of the Mecka Android implementation, enabling developers to understand the architecture, extend functionality, and maintain the codebase effectively.