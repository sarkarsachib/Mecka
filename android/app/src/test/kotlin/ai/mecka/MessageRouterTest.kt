package ai.mecka

import ai.mecka.agents.AgentBase
import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.MessageRouter
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MessageRouterTest {

    private lateinit var messageRouter: MessageRouter
    private lateinit var mockStateManager: StateManager
    private lateinit var mockSecurityManager: SecurityManager
    private lateinit var mockHardwareManager: HardwareManager

    @Before
    fun setup() {
        messageRouter = MessageRouter()
        mockStateManager = mockk(relaxed = true)
        mockSecurityManager = mockk(relaxed = true)
        mockHardwareManager = mockk(relaxed = true)
    }

    @Test
    fun testMessagePrioritization() {
        val messages = listOf(
            AgentMessage("msg3", "sender", "receiver", "INTENT3", MessagePriority.LOW),
            AgentMessage("msg1", "sender", "receiver", "INTENT1", MessagePriority.CRITICAL),
            AgentMessage("msg2", "sender", "receiver", "INTENT2", MessagePriority.HIGH),
            AgentMessage("msg4", "sender", "receiver", "INTENT4", MessagePriority.NORMAL)
        )

        val prioritized = messageRouter.prioritizeMessages(messages)

        // Should be ordered by priority: CRITICAL, HIGH, NORMAL, LOW
        assertEquals(MessagePriority.CRITICAL, prioritized[0].priority)
        assertEquals(MessagePriority.HIGH, prioritized[1].priority)
        assertEquals(MessagePriority.NORMAL, prioritized[2].priority)
        assertEquals(MessagePriority.LOW, prioritized[3].priority)
    }

    @Test
    fun testMessageRouting() = runBlocking {
        val testAgent = object : AgentBase(
            agentId = "test_agent",
            agentName = "Test Agent",
            agentDescription = "Test agent",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {
            var receivedMessage: AgentMessage? = null
            
            override fun onMessage(message: AgentMessage): Boolean {
                receivedMessage = message
                return true
            }
        }

        val agents = mapOf("test_agent" to testAgent)
        val testMessage = AgentMessage(
            messageId = "test_123",
            sourceAgentId = "sender",
            targetAgentId = "test_agent",
            intent = "TEST_INTENT",
            priority = MessagePriority.NORMAL
        )

        messageRouter.routeMessage(testMessage, agents)

        // Verify message was received
        assertEquals("test_123", (testAgent as Any).javaClass.getDeclaredField("receivedMessage").apply { isAccessible = true }.get(testAgent)?.messageId)
    }

    @Test
    fun testBroadcastMessage() = runBlocking {
        val agent1 = object : AgentBase(
            agentId = "agent1",
            agentName = "Agent 1",
            agentDescription = "Test agent 1",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {
            var messageCount = 0
            
            override fun onMessage(message: AgentMessage): Boolean {
                messageCount++
                return true
            }
        }

        val agent2 = object : AgentBase(
            agentId = "agent2",
            agentName = "Agent 2",
            agentDescription = "Test agent 2",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {
            var messageCount = 0
            
            override fun onMessage(message: AgentMessage): Boolean {
                messageCount++
                return true
            }
        }

        val agents = mapOf("agent1" to agent1, "agent2" to agent2)
        val broadcastMessage = AgentMessage(
            messageId = "broadcast_123",
            sourceAgentId = "system",
            targetAgentId = "*",
            intent = "BROADCAST_TEST",
            priority = MessagePriority.HIGH
        )

        messageRouter.broadcastMessage(broadcastMessage, agents)

        // Verify both agents received the message
        val agent1Count = agent1.javaClass.getDeclaredField("messageCount").apply { isAccessible = true }.get(agent1) as Int
        val agent2Count = agent2.javaClass.getDeclaredField("messageCount").apply { isAccessible = true }.get(agent2) as Int
        
        assertEquals(1, agent1Count)
        assertEquals(1, agent2Count)
    }
}