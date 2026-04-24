package ai.mecka

import ai.mecka.runtime.StateChangeListener
import ai.mecka.runtime.StateManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class StateManagerTest {

    private lateinit var mockContext: Context
    private lateinit var stateManager: StateManager

    @Before
    fun setup() {
        mockContext = mockk()
        every { mockContext.getSharedPreferences(any(), any()) } returns mockk(relaxed = true)
        stateManager = StateManager(mockContext)
    }

    @Test
    fun testStateOperations() {
        // Test setting and getting state
        stateManager.setState("test_key", "test_value")
        assertEquals("test_value", stateManager.getState("test_key"))

        // Test updating state
        stateManager.setState("test_key", "updated_value")
        assertEquals("updated_value", stateManager.getState("test_key"))

        // Test removing state
        stateManager.removeState("test_key")
        assertNull(stateManager.getState("test_key"))
    }

    @Test
    fun testStateListeners() {
        var listenerCalled = false
        var receivedKey: String? = null
        var receivedValue: Any? = null

        val testListener = object : StateChangeListener {
            override fun onStateChanged(key: String, newValue: Any?) {
                listenerCalled = true
                receivedKey = key
                receivedValue = newValue
            }
        }

        // Register listener
        stateManager.registerListener("test_key", testListener)

        // Set state - should trigger listener
        stateManager.setState("test_key", "listener_value")

        assertEquals(true, listenerCalled)
        assertEquals("test_key", receivedKey)
        assertEquals("listener_value", receivedValue)

        // Unregister listener
        stateManager.unregisterListener("test_key", testListener)
        listenerCalled = false

        // Set state again - should not trigger listener
        stateManager.setState("test_key", "another_value")
        assertEquals(false, listenerCalled)
    }

    @Test
    fun testMultipleListeners() {
        var listener1Called = false
        var listener2Called = false

        val listener1 = object : StateChangeListener {
            override fun onStateChanged(key: String, newValue: Any?) {
                listener1Called = true
            }
        }

        val listener2 = object : StateChangeListener {
            override fun onStateChanged(key: String, newValue: Any?) {
                listener2Called = true
            }
        }

        // Register both listeners
        stateManager.registerListener("multi_key", listener1)
        stateManager.registerListener("multi_key", listener2)

        // Set state - should trigger both listeners
        stateManager.setState("multi_key", "multi_value")

        assertEquals(true, listener1Called)
        assertEquals(true, listener2Called)
    }

    @Test
    fun testClearAllState() {
        // Set multiple states
        stateManager.setState("key1", "value1")
        stateManager.setState("key2", "value2")
        stateManager.setState("key3", "value3")

        // Clear all state
        stateManager.clearAllState()

        // Verify all states are cleared
        assertNull(stateManager.getState("key1"))
        assertNull(stateManager.getState("key2"))
        assertNull(stateManager.getState("key3"))
    }
}