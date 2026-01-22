package ai.mecka.runtime

import ai.mecka.agents.AgentBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessageRouter {

    suspend fun routeMessage(message: AgentMessage, agents: Map<String, AgentBase>) {
        withContext(Dispatchers.Default) {
            if (message.targetAgentId == "*") {
                // Broadcast to all agents
                agents.values.forEach { agent ->
                    agent.onMessage(message)
                }
            } else {
                // Route to specific agent
                val targetAgent = agents[message.targetAgentId]
                if (targetAgent != null) {
                    targetAgent.onMessage(message)
                } else {
                    Timber.w("No agent found for target: ${message.targetAgentId}")
                }
            }
        }
    }

    suspend fun broadcastMessage(message: AgentMessage, agents: Map<String, AgentBase>) {
        withContext(Dispatchers.Default) {
            agents.values.forEach { agent ->
                agent.onMessage(message)
            }
        }
    }

    fun prioritizeMessages(messages: List<AgentMessage>): List<AgentMessage> {
        return messages.sortedBy { it.priority.ordinal }
    }
}