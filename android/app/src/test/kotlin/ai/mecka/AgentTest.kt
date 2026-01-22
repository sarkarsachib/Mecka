package ai.mecka

import ai.mecka.agents.AgentBase
import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.AgentState
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AgentTest {

    private lateinit var mockStateManager: StateManager
    private lateinit var mockSecurityManager: SecurityManager
    private lateinit var mockHardwareManager: HardwareManager

    @Before
    fun setup() {
        mockStateManager = mockk(relaxed = true)
        mockSecurityManager = mockk(relaxed = true)
        mockHardwareManager = mockk(relaxed = true)
    }

    @Test
    fun testAgentLifecycle() {
        val testAgent = object : AgentBase(
            agentId = "test_agent",
            agentName = "Test Agent",
            agentDescription = "Test agent for unit testing",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {}

        // Test initialization
        assertEquals(true, testAgent.initialize())
        assertEquals(AgentState.INITIALIZING, testAgent.getCurrentState())

        // Test start
        assertEquals(true, testAgent.start())
        assertEquals(AgentState.RUNNING, testAgent.getCurrentState())

        // Test pause
        assertEquals(true, testAgent.pause())
        assertEquals(AgentState.PAUSED, testAgent.getCurrentState())

        // Test resume
        assertEquals(true, testAgent.resume())
        assertEquals(AgentState.RUNNING, testAgent.getCurrentState())

        // Test shutdown
        assertEquals(true, testAgent.shutdown())
        assertEquals(AgentState.SHUTDOWN, testAgent.getCurrentState())
    }

    @Test
    fun testAgentMessageHandling() {
        val testAgent = object : AgentBase(
            agentId = "test_agent",
            agentName = "Test Agent",
            agentDescription = "Test agent for unit testing",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {}

        val testMessage = AgentMessage(
            messageId = "test_123",
            sourceAgentId = "sender",
            targetAgentId = "test_agent",
            intent = "TEST_INTENT",
            priority = MessagePriority.NORMAL,
            payload = mapOf("key" to "value")
        )

        // Test message handling
        assertEquals(true, testAgent.onMessage(testMessage))
    }

    @Test
    fun testAgentErrorHandling() {
        val testAgent = object : AgentBase(
            agentId = "test_agent",
            agentName = "Test Agent",
            agentDescription = "Test agent for unit testing",
            stateManager = mockStateManager,
            securityManager = mockSecurityManager,
            hardwareManager = mockHardwareManager
        ) {}

        val testException = Exception("Test error")
        assertEquals(true, testAgent.onError(testException))
    }
}