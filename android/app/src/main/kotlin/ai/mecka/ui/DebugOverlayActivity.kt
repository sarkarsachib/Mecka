package ai.mecka.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ai.mecka.R

class DebugOverlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_overlay)
        
        // Set up debug UI
        setupDebugUI()
    }

    private fun setupDebugUI() {
        // TODO: Implement debug UI setup
        // This will show agent states, current context, etc.
    }

    companion object {
        const val EXTRA_SHOW_LOGS = "show_logs"
        const val EXTRA_SHOW_AGENTS = "show_agents"
        const val EXTRA_SHOW_STATE = "show_state"
    }
}