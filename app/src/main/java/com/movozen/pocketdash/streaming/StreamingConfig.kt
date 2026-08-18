package com.movozen.pocketdash.streaming

enum class StreamEndpoint(val suffix: String) {
    FRONT("front"),
    BACK("back")
}

data class StreamingConfig(
    val width: Int = 1280,
    val height: Int = 720,
    val fps: Int = 25,
    val videoBitrate: Int = 1_500_000, // 1.5 Mbps
    val iFrameInterval: Int = 2, // GOP 2 seconds
    val rotation: Int = 90, // Default orientation
    val audioBitrate: Int = 128 * 1024, // 128 kbps
    val sampleRate: Int = 44100,
    val isStereo: Boolean = true,
    val echoCanceler: Boolean = false,
    val noiseSuppressor: Boolean = false,
    val endpoint: StreamEndpoint = StreamEndpoint.FRONT
) {
    fun buildRtmpUrl(rollNumber: String): String {
        val sanitizedRoll = rollNumber.uppercase().trim().replace(" ", "")
        return "rtmp://15.207.177.194:1936/hackathon/${sanitizedRoll}_${endpoint.suffix}"
    }

    companion object {
        const val BASE_SERVER_HOST = "15.207.177.194:1936"
        const val VIEWER_URL = "http://15.207.177.194:8081/web/player.html"
    }
}
