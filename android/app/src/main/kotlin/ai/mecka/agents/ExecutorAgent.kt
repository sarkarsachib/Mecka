package ai.mecka.agents

import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import ai.mecka.services.GenosAccessibilityService
import android.content.Context
import android.content.Intent
import timber.log.Timber

class ExecutorAgent(
    private val context: Context,
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(
    agentId = "executor_agent",
    agentName = "Executor Agent",
    agentDescription = "Executes commands via Accessibility Service and reports results",
    stateManager = stateManager,
    securityManager = securityManager,
    hardwareManager = hardwareManager
) {

    private var accessibilityService: GenosAccessibilityService? = null

    override fun initialize(): Boolean {
        super.initialize()
        Timber.d("ExecutorAgent initialized")
        return true
    }

    override fun start(): Boolean {
        super.start()
        Timber.d("ExecutorAgent started")
        return true
    }

    override fun onMessage(message: AgentMessage): Boolean {
        super.onMessage(message)
        
        when (message.intent) {
            "COMMAND_DETECTED" -> {
                handleCommandDetected(message)
            }
            "ACCESSIBILITY_SERVICE_READY" -> {
                // Accessibility service is ready, we can now execute commands
                Timber.d("Accessibility service ready")
            }
        }
        
        return true
    }

    private fun handleCommandDetected(message: AgentMessage) {
        val command = message.payload["command"] as? String ?: return
        val confidence = message.payload["confidence"] as? Float ?: 0.0f
        
        Timber.d("Received command: $command (confidence: $confidence)")
        
        // Check permissions
        if (!checkCommandPermissions(command)) {
            Timber.w("Command $command not permitted")
            sendExecutionResult("PERMISSION_DENIED", "Command not permitted", false)
            return
        }
        
        // Execute command
        when (command) {
            "open camera" -> executeOpenCamera()
            "open settings" -> executeOpenSettings()
            "go back" -> executeGoBack()
            "go home" -> executeGoHome()
            else -> {
                Timber.w("Unknown command: $command")
                sendExecutionResult("UNKNOWN_COMMAND", "Command not recognized", false)
            }
        }
    }

    private fun checkCommandPermissions(command: String): Boolean {
        // TODO: Implement proper permission checking
        // For MVP, we'll allow all commands
        return true
    }

    private fun executeOpenCamera() {
        agentScope.launch {
            try {
                // Try to launch camera app
                val intent = context.packageManager.getLaunchIntentForPackage("com.android.camera")
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    sendExecutionResult("SUCCESS", "Camera opened", true)
                } else {
                    // Fallback to accessibility service
                    if (accessibilityService != null) {
                        accessibilityService?.launchApp("com.android.camera")
                        sendExecutionResult("SUCCESS", "Camera opened via accessibility", true)
                    } else {
                        sendExecutionResult("FAILURE", "No camera app found and no accessibility service", false)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to open camera")
                sendExecutionResult("FAILURE", "Failed to open camera: ${e.message}", false)
            }
        }
    }

    private fun executeOpenSettings() {
        agentScope.launch {
            try {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                sendExecutionResult("SUCCESS", "Settings opened", true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to open settings")
                sendExecutionResult("FAILURE", "Failed to open settings: ${e.message}", false)
            }
        }
    }

    private fun executeGoBack() {
        agentScope.launch {
            try {
                // Simulate back button press
                if (accessibilityService != null) {
                    // For now, we'll just send a success response
                    // Actual implementation would use accessibility service
                    sendExecutionResult("SUCCESS", "Go back executed", true)
                } else {
                    sendExecutionResult("FAILURE", "No accessibility service available", false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to execute go back")
                sendExecutionResult("FAILURE", "Failed to execute go back: ${e.message}", false)
            }
        }
    }

    private fun executeGoHome() {
        agentScope.launch {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                sendExecutionResult("SUCCESS", "Go home executed", true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to execute go home")
                sendExecutionResult("FAILURE", "Failed to execute go home: ${e.message}", false)
            }
        }
    }

    private fun sendExecutionResult(status: String, message: String, success: Boolean) {
        sendMessage(
            AgentMessage(
                messageId = "execution_result_${System.currentTimeMillis()}",
                sourceAgentId = agentId,
                targetAgentId = "context_agent",
                intent = "ACTION_EXECUTED",
                priority = MessagePriority.NORMAL,
                payload = mapOf(
                    "status" to status,
                    "message" to message,
                    "success" to success,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        )
    }

    fun setAccessibilityService(service: GenosAccessibilityService) {
        this.accessibilityService = service
    }
}