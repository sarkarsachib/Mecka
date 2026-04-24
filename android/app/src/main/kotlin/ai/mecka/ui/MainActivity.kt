package ai.mecka.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ai.mecka.R
import ai.mecka.services.GenosAccessibilityService
import ai.mecka.services.MeckaNotificationListenerService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permissions
        checkPermissions()
        
        // Check if accessibility service is enabled
        checkAccessibilityService()
        
        // Check if notification listener is enabled
        checkNotificationListener()
    }

    private fun checkPermissions() {
        // TODO: Implement permission checking and request logic
    }

    private fun checkAccessibilityService() {
        // TODO: Check if GenosAccessibilityService is enabled
    }

    private fun checkNotificationListener() {
        // TODO: Check if MeckaNotificationListenerService is enabled
    }

    companion object {
        const val REQUEST_CODE_ACCESSIBILITY = 1001
        const val REQUEST_CODE_NOTIFICATION = 1002
        const val REQUEST_CODE_PERMISSIONS = 1003
    }
}