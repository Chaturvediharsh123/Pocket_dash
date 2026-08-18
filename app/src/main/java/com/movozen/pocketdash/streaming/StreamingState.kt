package com.movozen.pocketdash.streaming

enum class ConnectionStatus {
    IDLE,
    CONNECTING,
    LIVE,
    STOPPING,
    ERROR
}

data class StreamingState(
    val status: ConnectionStatus = ConnectionStatus.IDLE,
    val cameraActive: Boolean = false,
    val microphoneActive: Boolean = false,
    val rtmpUrl: String = "",
    val durationSeconds: Long = 0,
    val currentBitrateBps: Long = 0,
    val errorMessage: String? = null,
    val isFrontCamera: Boolean = true
) {
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val secs = durationSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, secs)
        }

    val formattedBitrateKbps: String
        get() = "${currentBitrateBps / 1000} kbps"
}
