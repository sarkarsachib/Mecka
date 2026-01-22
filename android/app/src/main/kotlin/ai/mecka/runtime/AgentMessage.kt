package ai.mecka.runtime

data class AgentMessage(
    val messageId: String,
    val sourceAgentId: String,
    val targetAgentId: String,
    val intent: String,
    val priority: MessagePriority,
    val payload: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessagePriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

enum class AgentState {
    INITIALIZING,
    IDLE,
    RUNNING,
    PAUSED,
    SHUTDOWN
}