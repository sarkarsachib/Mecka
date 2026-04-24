package ai.mecka.services

import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.AgentRegistry
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.StateManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import timber.log.Timber

class MeckaNotificationListenerService : NotificationListenerService() {

    private lateinit var agentRegistry: AgentRegistry
    private lateinit var stateManager: StateManager

    override fun onCreate() {
        super.onCreate()
        Timber.d("MeckaNotificationListenerService created")
        
        agentRegistry = AgentRegistry.getInstance(
            this,
            StateManager.getInstance(this),
            ai.mecka.security.SecurityManager.getInstance(this),
            ai.mecka.hardware.HardwareManager.getInstance(this)
        )
        stateManager = StateManager.getInstance(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("Notification listener connected")
        stateManager.setState("notification_listener_status", "connected")
        
        // Notify agents that notification listener is ready
        agentRegistry.broadcastMessage(
            AgentMessage(
                messageId = "notification_listener_ready_${System.currentTimeMillis()}",
                sourceAgentId = "system",
                targetAgentId = "*",
                intent = "NOTIFICATION_LISTENER_READY",
                priority = MessagePriority.HIGH
            )
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.d("Notification listener disconnected")
        stateManager.setState("notification_listener_status", "disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val packageName = sbn.packageName
        val notification = sbn.notification
        val title = notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val text = notification.extras.getCharSequence("android.text")?.toString() ?: ""
        val priority = notification.priority
        
        Timber.d("Notification posted: $packageName - $title: $text")
        
        // Send notification to listener agent
        agentRegistry.sendMessage(
            AgentMessage(
                messageId = "notification_${sbn.key}_${System.currentTimeMillis()}",
                sourceAgentId = "notification_listener",
                targetAgentId = "listener_agent",
                intent = "NOTIFICATION_RECEIVED",
                priority = MessagePriority.NORMAL,
                payload = mapOf(
                    "package" to packageName,
                    "title" to title,
                    "text" to text,
                    "priority" to priority,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        
        Timber.d("Notification removed: ${sbn.packageName}")
        
        // Notify agents about removed notification
        agentRegistry.sendMessage(
            AgentMessage(
                messageId = "notification_removed_${sbn.key}_${System.currentTimeMillis()}",
                sourceAgentId = "notification_listener",
                targetAgentId = "listener_agent",
                intent = "NOTIFICATION_REMOVED",
                priority = MessagePriority.LOW,
                payload = mapOf(
                    "package" to sbn.packageName,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        )
    }

    companion object {
        const val SERVICE_ID = "ai.mecka.services.MeckaNotificationListenerService"
    }
}