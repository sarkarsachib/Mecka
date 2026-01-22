package ai.mecka.services

import ai.mecka.agents.ContextAgent
import ai.mecka.agents.ExecutorAgent
import ai.mecka.agents.ListenerAgent
import ai.mecka.runtime.AgentRegistry
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import timber.log.Timber

class MeckaMainService : Service() {

    private lateinit var agentRegistry: AgentRegistry
    private lateinit var stateManager: StateManager
    private lateinit var securityManager: SecurityManager
    private lateinit var hardwareManager: HardwareManager

    private var isServiceStarted = false

    override fun onCreate() {
        super.onCreate()
        Timber.d("MeckaMainService created")
        
        // Initialize core systems
        stateManager = StateManager.getInstance(this)
        securityManager = SecurityManager.getInstance(this)
        hardwareManager = HardwareManager.getInstance(this)
        agentRegistry = AgentRegistry.getInstance(this, stateManager, securityManager, hardwareManager)
        
        // Initialize hardware
        hardwareManager.initialize()
        
        // Create notification channel for foreground service
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("MeckaMainService starting")
        
        if (!isServiceStarted) {
            startForegroundService()
            initializeAgents()
            isServiceStarted = true
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Timber.d("MeckaMainService destroying")
        shutdownAgents()
        hardwareManager.shutdown()
        isServiceStarted = false
        super.onDestroy()
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        stateManager.setState("main_service_status", "running")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mecka Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mecka background service"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Mecka")
                .setContentText("Agentic intelligence running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Mecka")
                .setContentText("Agentic intelligence running")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(Notification.PRIORITY_LOW)
                .build()
        }
    }

    private fun initializeAgents() {
        Timber.d("Initializing agents")
        
        // Create and register agents
        val listenerAgent = ListenerAgent(stateManager, securityManager, hardwareManager)
        val executorAgent = ExecutorAgent(this, stateManager, securityManager, hardwareManager)
        val contextAgent = ContextAgent(this, stateManager, securityManager, hardwareManager)
        
        agentRegistry.registerAgent(listenerAgent)
        agentRegistry.registerAgent(executorAgent)
        agentRegistry.registerAgent(contextAgent)
        
        // Start all agents
        agentRegistry.initializeAllAgents()
        agentRegistry.startAllAgents()
        
        Timber.d("All agents initialized and started")
    }

    private fun shutdownAgents() {
        Timber.d("Shutting down agents")
        agentRegistry.shutdownAllAgents()
        Timber.d("All agents shut down")
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "mecka_service_channel"
    }
}