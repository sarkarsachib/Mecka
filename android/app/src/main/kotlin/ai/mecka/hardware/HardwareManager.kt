package ai.mecka.hardware

import ai.mecka.runtime.StateManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import timber.log.Timber

class HardwareManager(private val context: Context) {

    private val stateManager: StateManager by lazy { StateManager.getInstance(context) }
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { onBluetoothDeviceFound(it) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Timber.d("Bluetooth discovery finished")
                    stateManager.setState("bluetooth_discovery_status", "finished")
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_ON -> {
                            Timber.d("Bluetooth turned on")
                            stateManager.setState("bluetooth_status", "on")
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            Timber.d("Bluetooth turned off")
                            stateManager.setState("bluetooth_status", "off")
                        }
                    }
                }
            }
        }
    }

    fun initialize(): Boolean {
        Timber.d("HardwareManager initializing")
        
        // Register Bluetooth receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)
        
        // Initialize state
        stateManager.setState("bluetooth_status", if (bluetoothAdapter.isEnabled) "on" else "off")
        
        return true
    }

    fun shutdown() {
        context.unregisterReceiver(bluetoothReceiver)
        Timber.d("HardwareManager shutdown")
    }

    fun startBluetoothDiscovery(): Boolean {
        return if (bluetoothAdapter.isEnabled) {
            bluetoothAdapter.startDiscovery()
            stateManager.setState("bluetooth_discovery_status", "discovering")
            true
        } else {
            Timber.w("Bluetooth is not enabled")
            false
        }
    }

    fun stopBluetoothDiscovery(): Boolean {
        return bluetoothAdapter.cancelDiscovery()
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices.toList()
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun getAudioOutputDevices(): List<AudioDeviceInfo> {
        // TODO: Implement audio device enumeration
        return emptyList()
    }

    fun setAudioOutputToBluetooth(): Boolean {
        // TODO: Implement Bluetooth audio routing
        return true
    }

    private fun onBluetoothDeviceFound(device: BluetoothDevice) {
        Timber.d("Found Bluetooth device: ${device.name} (${device.address})")
        stateManager.setState("bluetooth_device_found", device.address)
    }

    companion object {
        private var instance: HardwareManager? = null

        fun getInstance(context: Context): HardwareManager {
            return instance ?: synchronized(this) {
                instance ?: HardwareManager(context).also {
                    instance = it
                }
            }
        }
    }
}

data class AudioDeviceInfo(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean
)