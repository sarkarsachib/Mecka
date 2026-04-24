package ai.mecka

import android.app.Application
import ai.mecka.runtime.AgentRegistry
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import ai.mecka.services.MeckaMainService
import timber.log.Timber

class MeckaApplication : Application() {

    lateinit var agentRegistry: AgentRegistry
    lateinit var stateManager: StateManager
    lateinit var securityManager: SecurityManager
    lateinit var hardwareManager: HardwareManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize core systems
        stateManager = StateManager(this)
        securityManager = SecurityManager(this)
        hardwareManager = HardwareManager(this)
        agentRegistry = AgentRegistry(this, stateManager, securityManager, hardwareManager)

        Timber.d("MeckaApplication initialized")
    }

    companion object {
        lateinit var instance: MeckaApplication
            private set
    }
}