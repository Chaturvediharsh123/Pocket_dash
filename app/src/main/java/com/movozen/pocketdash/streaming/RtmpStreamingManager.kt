package com.movozen.pocketdash.streaming

import android.content.Context
import android.util.Log
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.view.OpenGlView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RtmpStreamingManager(
    private val context: Context,
    private val openGlView: OpenGlView,
    private val config: StreamingConfig = StreamingConfig()
) : ConnectChecker {

    private val TAG = "PocketDash"

    private var rtmpCamera2: RtmpCamera2? = null

    private val _state = MutableStateFlow(StreamingState())
    val state: StateFlow<StreamingState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    init {
        try {
            rtmpCamera2 = RtmpCamera2(openGlView, this)
            Log.d(TAG, "RtmpCamera2 initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing RtmpCamera2", e)
            _state.update {
                it.copy(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Failed to initialize camera engine: ${e.localizedMessage}"
                )
            }
        }
    }

    fun startPreview() {
        try {
            val camera = rtmpCamera2 ?: return
            if (!camera.isOnPreview) {
                camera.startPreview()
                Log.d(TAG, "Camera preview started")
                _state.update { it.copy(cameraActive = true, microphoneActive = false) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start camera preview", e)
            _state.update {
                it.copy(
                    cameraActive = false,
                    errorMessage = "Camera preview error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun stopPreview() {
        try {
            val camera = rtmpCamera2 ?: return
            if (camera.isOnPreview) {
                camera.stopPreview()
                Log.d(TAG, "Camera preview stopped")
                _state.update { it.copy(cameraActive = false) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping preview", e)
        }
    }

    fun startStream(rollNumber: String) {
        val camera = rtmpCamera2
        if (camera == null) {
            _state.update {
                it.copy(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Camera engine not ready"
                )
            }
            return
        }

        val sanitizedRoll = rollNumber.uppercase().trim().replace(" ", "")
        if (sanitizedRoll.isEmpty()) {
            _state.update {
                it.copy(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Valid Roll Number required"
                )
            }
            return
        }

        val streamUrl = config.buildRtmpUrl(sanitizedRoll)
        Log.i(TAG, "Preparing to start stream to: $streamUrl")

        _state.update {
            it.copy(
                status = ConnectionStatus.CONNECTING,
                rtmpUrl = streamUrl,
                errorMessage = null
            )
        }

        try {
            // Prepare Audio (AAC)
            val audioPrepared = camera.prepareAudio(
                config.audioBitrate,
                config.sampleRate,
                config.isStereo,
                config.echoCanceler,
                config.noiseSuppressor
            )

            // Prepare Video (H.264 1280x720 25fps)
            val videoPrepared = camera.prepareVideo(
                config.width,
                config.height,
                config.fps,
                config.videoBitrate,
                config.iFrameInterval,
                config.rotation
            )

            if (audioPrepared && videoPrepared) {
                Log.d(TAG, "Audio and Video encoders prepared successfully. Starting RTMP stream...")
                camera.startStream(streamUrl)
            } else {
                val errorReason = when {
                    !audioPrepared && !videoPrepared -> "Microphone and Camera encoding setup failed"
                    !audioPrepared -> "Microphone audio encoding setup failed"
                    else -> "Camera H.264 video encoding setup failed"
                }
                Log.e(TAG, errorReason)
                _state.update {
                    it.copy(
                        status = ConnectionStatus.ERROR,
                        errorMessage = errorReason
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during stream preparation", e)
            _state.update {
                it.copy(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Stream start failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun stopStream() {
        val camera = rtmpCamera2 ?: return
        if (camera.isStreaming) {
            Log.i(TAG, "Stopping RTMP stream...")
            _state.update { it.copy(status = ConnectionStatus.STOPPING) }
            try {
                camera.stopStream()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping stream", e)
            }
        }
        stopTimer()
        _state.update {
            it.copy(
                status = ConnectionStatus.IDLE,
                microphoneActive = false,
                currentBitrateBps = 0,
                durationSeconds = 0
            )
        }
    }

    fun switchCamera() {
        try {
            val camera = rtmpCamera2 ?: return
            camera.switchCamera()
            val newIsFront = !state.value.isFrontCamera
            Log.d(TAG, "Switched camera. Front facing: $newIsFront")
            _state.update { it.copy(isFrontCamera = newIsFront) }
        } catch (e: Exception) {
            Log.e(TAG, "Error switching camera", e)
        }
    }

    fun release() {
        stopTimer()
        try {
            val camera = rtmpCamera2
            if (camera != null) {
                if (camera.isStreaming) {
                    camera.stopStream()
                }
                if (camera.isOnPreview) {
                    camera.stopPreview()
                }
            }
            Log.d(TAG, "RtmpStreamingManager released cleanly")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing RtmpStreamingManager", e)
        }
    }

    // ConnectChecker Implementation Callbacks
    override fun onConnectionStarted(url: String) {
        Log.i(TAG, "RTMP connection started: $url")
        _state.update {
            it.copy(
                status = ConnectionStatus.CONNECTING,
                errorMessage = null
            )
        }
    }

    override fun onConnectionSuccess() {
        Log.i(TAG, "RTMP connection established and publishing LIVE!")
        _state.update {
            it.copy(
                status = ConnectionStatus.LIVE,
                cameraActive = true,
                microphoneActive = true,
                errorMessage = null
            )
        }
        startTimer()
    }

    override fun onConnectionFailed(reason: String) {
        Log.e(TAG, "RTMP connection failed: $reason")
        stopTimer()
        val userFriendlyReason = if (reason.contains("refused") || reason.contains("unreachable")) {
            "Unable to connect to RTMP server (15.207.177.194). Check your internet connection."
        } else {
            "Connection failed: $reason"
        }
        _state.update {
            it.copy(
                status = ConnectionStatus.ERROR,
                microphoneActive = false,
                errorMessage = userFriendlyReason
            )
        }
    }

    override fun onDisconnect() {
        Log.i(TAG, "RTMP disconnected")
        stopTimer()
        _state.update {
            it.copy(
                status = ConnectionStatus.IDLE,
                microphoneActive = false,
                currentBitrateBps = 0
            )
        }
    }

    override fun onAuthError() {
        Log.e(TAG, "RTMP Auth Error")
        stopTimer()
        _state.update {
            it.copy(
                status = ConnectionStatus.ERROR,
                errorMessage = "Authentication error on RTMP server"
            )
        }
    }

    override fun onAuthSuccess() {
        Log.i(TAG, "RTMP Auth Success")
    }

    override fun onNewBitrate(bitrate: Long) {
        _state.update { it.copy(currentBitrateBps = bitrate) }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = scope.launch {
            _state.update { it.copy(durationSeconds = 0) }
            while (true) {
                delay(1000)
                _state.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
