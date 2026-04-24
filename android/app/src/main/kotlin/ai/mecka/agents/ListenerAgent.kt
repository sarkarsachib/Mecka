package ai.mecka.agents

import ai.mecka.runtime.AgentMessage
import ai.mecka.runtime.MessagePriority
import ai.mecka.runtime.StateManager
import ai.mecka.security.SecurityManager
import ai.mecka.hardware.HardwareManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ListenerAgent(
    stateManager: StateManager,
    securityManager: SecurityManager,
    hardwareManager: HardwareManager
) : AgentBase(
    agentId = "listener_agent",
    agentName = "Listener Agent",
    agentDescription = "Handles audio input, wake word detection, and command extraction",
    stateManager = stateManager,
    securityManager = securityManager,
    hardwareManager = hardwareManager
) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var bufferSize: Int = 0

    override fun initialize(): Boolean {
        super.initialize()
        
        // Calculate buffer size
        bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            Timber.e("Failed to calculate audio buffer size")
            return false
        }
        
        Timber.d("ListenerAgent initialized with buffer size: $bufferSize")
        return true
    }

    override fun start(): Boolean {
        super.start()
        
        if (isRecording) {
            Timber.w("Already recording")
            return true
        }
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            isRecording = true
            
            // Start audio processing
            agentScope.launch(Dispatchers.IO) {
                processAudio()
            }
            
            Timber.d("ListenerAgent started recording")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to start audio recording")
            return false
        }
    }

    override fun shutdown(): Boolean {
        stopRecording()
        super.shutdown()
        return true
    }

    private fun stopRecording() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            isRecording = false
            Timber.d("ListenerAgent stopped recording")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping audio recording")
        }
    }

    private suspend fun processAudio() {
        val audioBuffer = ShortArray(bufferSize / 2)
        
        while (isRecording && !agentScope.isActive) {
            val bytesRead = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
            
            if (bytesRead > 0) {
                // Process audio data
                val audioData = audioBuffer.copyOf(bytesRead)
                
                // Check for wake word
                if (detectWakeWord(audioData)) {
                    Timber.d("Wake word detected!")
                    
                    // Notify context agent
                    sendMessage(
                        AgentMessage(
                            messageId = "wake_word_${System.currentTimeMillis()}",
                            sourceAgentId = agentId,
                            targetAgentId = "context_agent",
                            intent = "WAKE_WORD_DETECTED",
                            priority = MessagePriority.HIGH
                        )
                    )
                    
                    // Start command detection
                    detectCommand()
                }
            }
        }
    }

    private fun detectWakeWord(audioData: ShortArray): Boolean {
        // TODO: Implement actual wake word detection
        // For MVP, we'll use a simple pattern or just return true for testing
        return audioData.isNotEmpty() && System.currentTimeMillis() % 10000 < 100 // 1% chance for testing
    }

    private fun detectCommand() {
        // TODO: Implement command detection logic
        // For MVP, we'll simulate a command
        agentScope.launch {
            // Simulate command detection delay
            kotlinx.coroutines.delay(1000)
            
            // Send detected command to executor
            sendMessage(
                AgentMessage(
                    messageId = "command_${System.currentTimeMillis()}",
                    sourceAgentId = agentId,
                    targetAgentId = "executor_agent",
                    intent = "COMMAND_DETECTED",
                    priority = MessagePriority.HIGH,
                    payload = mapOf(
                        "command" to "open camera",
                        "confidence" to 0.95f,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
        }
    }

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val WAKE_WORD = "hey mecka"
    }
}