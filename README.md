# PocketDash

PocketDash is an Android application built for the Movozen Pocket Dashcam Challenge. It turns an Android smartphone into a connected live dashcam that streams real-time camera video and microphone audio over RTMP.

## Features

- **Live RTMP Streaming**: Encodes and streams 720p H.264 video and AAC audio to the Movozen RTMP server.
- **Camera Preview & Controls**: Live camera preview, camera flip (Front/Back toggle), and clean resource management.
- **Roll Number Management**: Auto-sanitizes, validates, and locally persists the student roll number across app restarts.
- **Stream Status & Metrics**: Real-time indicators for Camera, Mic, RTMP connection status (`IDLE`, `CONNECTING`, `LIVE`, `ERROR`), live duration timer, and publishing bitrate (`kbps`).
- **Dynamic Endpoint**: Automatically builds RTMP stream URLs:
  - `rtmp://15.207.177.194:1936/hackathon/{ROLLNO}_front`
  - `rtmp://15.207.177.194:1936/hackathon/{ROLLNO}_back`

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3 Dark Theme)
- **Streaming Library**: PedroSG94 RootEncoder (`RtmpCamera2`)
- **Min SDK**: 24 (Android 7.0+)
- **Target / Compile SDK**: 34 (Android 14)

## Stream Configuration

- **Video Codec**: H.264
- **Resolution**: 1280x720 (720p)
- **Framerate**: 25 FPS
- **Video Bitrate**: ~1.5 Mbps
- **Keyframe Interval**: 2 seconds
- **Audio Codec**: AAC (128 kbps, 44.1 kHz, Stereo)

## How to Build and Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Chaturvediharsh123/Pocket_dash.git
   cd Pocket_dash
   ```

2. Connect your Android phone via USB with USB Debugging enabled.

3. Build and install on device:
   ```bash
   ./gradlew installDebug
   ```

## How to Verify Stream

1. Launch PocketDash on your phone and grant Camera & Microphone permissions.
2. Enter your Roll Number and tap **START STREAM**.
3. Open the web viewer in your browser: `http://15.207.177.194:8081/web/player.html`
4. Enter your Roll Number and click **Watch** to verify live video and audio playback.
