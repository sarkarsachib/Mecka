package ai.mecka.runtime

import ai.mecka.agents.AgentBase
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class AgentRegistry(
    private val context: Context,
    private val stateManager: StateManager,
    private val securityManager: SecurityManager,
    private val hardwareManager: HardwareManager
) {

    private val agents = ConcurrentHashMap<String, AgentBase>()
    private val messageRouter = MessageRouter()
    private val registryScope = CoroutineScope(Dispatchers.Default + Job())

    fun registerAgent(agent: AgentBase): Boolean {
        if (agents.containsKey(agent.agentId)) {
            Timber.w("Agent ${agent.agentId} already registered")
            return false
        }

        agents[agent.agentId] = agent
        Timber.d("Registered agent: ${agent.agentId}")
        return true
    }

    fun unregisterAgent(agentId: String): Boolean {
        val agent = agents.remove(agentId)
        if (agent != null) {
            agent.shutdown()
            Timber.d("Unregistered agent: $agentId")
            return true
        }
        return false
    }

    fun getAgent(agentId: String): AgentBase? {
        return agents[agentId]
    }

    fun startAgent(agentId: String): Boolean {
        val agent = agents[agentId] ?: return false
        return agent.start()
    }

    fun stopAgent(agentId: String): Boolean {
        val agent = agents[agentId] ?: return false
        return agent.shutdown()
    }

    fun sendMessage(message: AgentMessage) {
        registryScope.launch {
            messageRouter.routeMessage(message, agents)
        }
    }

    fun broadcastMessage(message: AgentMessage) {
        registryScope.launch {
            messageRouter.broadcastMessage(message, agents)
        }
    }

    fun initializeAllAgents() {
        agents.values.forEach { agent ->
            agent.initialize()
        }
    }

    fun startAllAgents() {
        agents.values.forEach { agent ->
            agent.start()
        }
    }

    fun shutdownAllAgents() {
        agents.values.forEach { agent ->
            agent.shutdown()
        }
        agents.clear()
    }

    fun getAgentCount(): Int {
        return agents.size
    }

    fun getAllAgentIds(): List<String> {
        return agents.keys.toList()
    }

    companion object {
        private var instance: AgentRegistry? = null

        fun getInstance(
            context: Context,
            stateManager: StateManager,
            securityManager: SecurityManager,
            hardwareManager: HardwareManager
        ): AgentRegistry {
            return instance ?: synchronized(this) {
                instance ?: AgentRegistry(context, stateManager, securityManager, hardwareManager).also {
                    instance = it
                }
            }
        }
    }
}