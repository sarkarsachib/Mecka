package ai.mecka.agents

import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager
import timber.log.Timber

class ContextAgent(
    private val context: Context,
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(
    agentId = "context_agent",
    agentName = "Context Agent",
    agentDescription = "Tracks device state, app context, and publishes state changes",
    stateManager = stateManager,
    securityManager = securityManager,
    hardwareManager = hardwareManager
) {

    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    private val powerManager: PowerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override fun initialize(): Boolean {
        super.initialize()
        
        // Initialize with current device state
        updateDeviceState()
        
        Timber.d("ContextAgent initialized")
        return true
    }

    override fun start(): Boolean {
        super.start()
        
        // Start periodic state updates
        agentScope.launch {
            while (getCurrentState() == ai.mecka.runtime.AgentState.RUNNING) {
                updateDeviceState()
                kotlinx.coroutines.delay(5000) // Update every 5 seconds
            }
        }
        
        Timber.d("ContextAgent started")
        return true
    }

    override fun onMessage(message: AgentMessage): Boolean {
        super.onMessage(message)
        
        when (message.intent) {
            "WINDOW_CHANGED" -> {
                handleWindowChanged(message)
            }
            "WAKE_WORD_DETECTED" -> {
                handleWakeWordDetected()
            }
            "ACTION_EXECUTED" -> {
                handleActionExecuted(message)
            }
            "NOTIFICATION_RECEIVED" -> {
                handleNotificationReceived(message)
            }
        }
        
        return true
    }

    private fun handleWindowChanged(message: AgentMessage) {
        val packageName = message.payload["package"] as? String ?: "unknown"
        val activity = message.payload["activity"] as? String ?: "unknown"
        
        Timber.d("Window changed to: $packageName/$activity")
        
        // Update state
        stateManager.setState("current_app_package", packageName)
        stateManager.setState("current_app_activity", activity)
        
        // Broadcast context change
        broadcastContextChange("APP_CHANGED", mapOf(
            "package" to packageName,
            "activity" to activity
        ))
    }

    private fun handleWakeWordDetected() {
        Timber.d("Wake word detected - updating context")
        
        // Update state
        stateManager.setState("last_wake_word_time", System.currentTimeMillis())
        
        // Broadcast context change
        broadcastContextChange("WAKE_WORD_DETECTED", mapOf(
            "timestamp" to System.currentTimeMillis()
        ))
    }

    private fun handleActionExecuted(message: AgentMessage) {
        val status = message.payload["status"] as? String ?: "unknown"
        val success = message.payload["success"] as? Boolean ?: false
        
        Timber.d("Action executed: $status (success: $success)")
        
        // Update state
        stateManager.setState("last_action_status", status)
        stateManager.setState("last_action_success", success)
        
        // Broadcast context change
        broadcastContextChange("ACTION_EXECUTED", mapOf(
            "status" to status,
            "success" to success,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    private fun handleNotificationReceived(message: AgentMessage) {
        val packageName = message.payload["package"] as? String ?: "unknown"
        val title = message.payload["title"] as? String ?: ""
        val text = message.payload["text"] as? String ?: ""
        
        Timber.d("Notification received: $packageName - $title: $text")
        
        // Update state
        stateManager.setState("last_notification_package", packageName)
        stateManager.setState("last_notification_time", System.currentTimeMillis())
        
        // Broadcast context change
        broadcastContextChange("NOTIFICATION_RECEIVED", mapOf(
            "package" to packageName,
            "title" to title,
            "text" to text
        ))
    }

    private fun updateDeviceState() {
        // Battery state
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        
        // Screen state
        val isScreenOn = powerManager.isInteractive
        
        // Network state
        // TODO: Implement network state detection
        
        // Update states
        stateManager.setState("battery_level", batteryLevel)
        stateManager.setState("is_charging", isCharging)
        stateManager.setState("is_screen_on", isScreenOn)
        
        Timber.d("Device state updated: battery=$batteryLevel%, charging=$isCharging, screenOn=$isScreenOn")
    }

    private fun broadcastContextChange(changeType: String, details: Map<String, Any>) {
        sendMessage(
            AgentMessage(
                messageId = "context_change_${System.currentTimeMillis()}",
                sourceAgentId = agentId,
                targetAgentId = "*",
                intent = "CONTEXT_CHANGED",
                priority = MessagePriority.NORMAL,
                payload = mapOf(
                    "change_type" to changeType,
                    "details" to details,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        )
    }

    companion object {
        const val CONTEXT_UPDATE_INTERVAL = 5000L // 5 seconds
    }
}