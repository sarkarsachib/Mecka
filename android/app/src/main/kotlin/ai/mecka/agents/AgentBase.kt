package ai.mecka.agents

import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.AgentState
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class AgentBase(
    val agentId: String,
    val agentName: String,
    val agentDescription: String,
    protected val stateManager: StateManager,
    protected val securityManager: SecurityManager,
    protected val hardwareManager: HardwareManager
) {

    protected val agentScope = CoroutineScope(Dispatchers.Default + Job())
    protected var currentState: AgentState = AgentState.IDLE

    // Agent lifecycle methods
    open fun initialize(): Boolean {
        Timber.d("$agentName initializing")
        currentState = AgentState.INITIALIZING
        return true
    }

    open fun start(): Boolean {
        Timber.d("$agentName starting")
        currentState = AgentState.RUNNING
        return true
    }

    open fun pause(): Boolean {
        Timber.d("$agentName pausing")
        currentState = AgentState.PAUSED
        return true
    }

    open fun resume(): Boolean {
        Timber.d("$agentName resuming")
        currentState = AgentState.RUNNING
        return true
    }

    open fun shutdown(): Boolean {
        Timber.d("$agentName shutting down")
        currentState = AgentState.SHUTDOWN
        agentScope.coroutineContext.cancelChildren()
        return true
    }

    // Message handling
    open fun onMessage(message: AgentMessage): Boolean {
        Timber.d("$agentName received message: ${message.intent}")
        return true
    }

    // Error handling
    open fun onError(error: Exception): Boolean {
        Timber.e(error, "$agentName encountered error")
        return true
    }

    // State management
    open fun onStateChanged(stateName: String, newValue: Any) {
        // Can be overridden by subclasses
    }

    // Helper methods
    protected fun sendMessage(message: AgentMessage) {
        // Will be implemented by AgentRegistry
    }

    protected fun getCurrentState(): AgentState {
        return currentState
    }

    companion object {
        const val AGENT_PREFIX = "agent_"
    }
}