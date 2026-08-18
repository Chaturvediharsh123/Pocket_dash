package com.movozen.pocketdash.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movozen.pocketdash.data.PreferencesManager
import com.movozen.pocketdash.streaming.ConnectionStatus
import com.movozen.pocketdash.streaming.RtmpStreamingManager
import com.movozen.pocketdash.streaming.StreamingConfig
import com.movozen.pocketdash.ui.components.CameraPreview
import com.movozen.pocketdash.ui.theme.*
import com.pedro.library.view.OpenGlView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketDashApp(
    streamingManager: RtmpStreamingManager?,
    openGlView: OpenGlView,
    preferencesManager: PreferencesManager,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    var rollNumberInput by remember { mutableStateOf(preferencesManager.rollNumber) }
    val state = streamingManager?.state?.collectAsState()?.value

    val isRollValid = remember(rollNumberInput) {
        preferencesManager.isValidRollNumber(rollNumberInput)
    }

    val isStreaming = state?.status == ConnectionStatus.LIVE || state?.status == ConnectionStatus.CONNECTING
    val isLive = state?.status == ConnectionStatus.LIVE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "POCKET DASHCAM",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            // Live Indicator Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (state?.status) {
                    ConnectionStatus.LIVE -> StatusGreen.copy(alpha = 0.2f)
                    ConnectionStatus.CONNECTING -> StatusYellow.copy(alpha = 0.2f)
                    ConnectionStatus.ERROR -> StatusRed.copy(alpha = 0.2f)
                    else -> SurfaceCard
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (state?.status) {
                        ConnectionStatus.LIVE -> StatusGreen
                        ConnectionStatus.CONNECTING -> StatusYellow
                        ConnectionStatus.ERROR -> StatusRed
                        else -> TextSecondary.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (state?.status) {
                                    ConnectionStatus.LIVE -> StatusGreen
                                    ConnectionStatus.CONNECTING -> StatusYellow
                                    ConnectionStatus.ERROR -> StatusRed
                                    else -> TextSecondary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = state?.status?.name ?: "IDLE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Camera Preview Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, SurfaceCard, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasPermissions && streamingManager != null) {
                CameraPreview(
                    streamingManager = streamingManager,
                    openGlView = openGlView,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Controls & Badges
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Top-Left: Duration & Bitrate Overlay
                    if (isLive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "REC ",
                                    color = PrimaryRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = state?.formattedDuration ?: "00:00:00",
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " | ${state?.formattedBitrateKbps ?: "0 kbps"}",
                                    color = AccentCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Top-Right: Switch Camera Button
                    IconButton(
                        onClick = { streamingManager.switchCamera() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera Preview Unavailable",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }

        // Roll Number Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ROLL NUMBER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rollNumberInput,
                    onValueChange = { newValue ->
                        val sanitized = preferencesManager.sanitizeRollNumber(newValue)
                        rollNumberInput = sanitized
                        preferencesManager.rollNumber = sanitized
                    },
                    placeholder = { Text("e.g. 22BCS0421", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isStreaming,
                    isError = rollNumberInput.isNotEmpty() && !isRollValid,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    supportingText = {
                        if (rollNumberInput.isEmpty()) {
                            Text("Required for RTMP stream endpoint identification", fontSize = 11.sp, color = TextSecondary)
                        } else if (!isRollValid) {
                            Text("Invalid roll number. Uppercase, no spaces allowed.", fontSize = 11.sp, color = StatusRed)
                        } else {
                            Text(
                                text = "Endpoint: rtmp://${StreamingConfig.BASE_SERVER_HOST}/hackathon/${rollNumberInput}_front",
                                fontSize = 11.sp,
                                color = StatusGreen
                            )
                        }
                    }
                )
            }
        }

        // Live Dashboard & Hardware Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SYSTEM STATUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusItem(
                        icon = Icons.Default.Camera,
                        label = "Camera",
                        value = if (state?.cameraActive == true) "ACTIVE (${if (state.isFrontCamera) "FRONT" else "BACK"})" else if (hasPermissions) "READY" else "NO PERMISSION",
                        isOk = state?.cameraActive == true || hasPermissions
                    )
                    StatusItem(
                        icon = Icons.Default.Mic,
                        label = "Microphone",
                        value = if (state?.microphoneActive == true) "ACTIVE" else if (hasPermissions) "READY" else "NO PERMISSION",
                        isOk = state?.microphoneActive == true || hasPermissions
                    )
                }

                Divider(color = SurfaceCard)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Streaming Duration", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = state?.formattedDuration ?: "00:00:00",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Video Target", fontSize = 12.sp, color = TextSecondary)
                        Text("720p @ 25fps (H.264/AAC)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                    }
                }
            }
        }

        // Error Banner Display
        AnimatedVisibility(visible = state?.errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = StatusRed.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = StatusRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state?.errorMessage ?: "",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Main Stream Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // START STREAM BUTTON
            Button(
                onClick = {
                    if (streamingManager != null && isRollValid) {
                        streamingManager.startStream(rollNumberInput)
                    }
                },
                enabled = hasPermissions && isRollValid && !isStreaming,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreen,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state?.status == ConnectionStatus.CONNECTING) "CONNECTING..." else "START STREAM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // STOP STREAM BUTTON
            Button(
                onClick = { streamingManager?.stopStream() },
                enabled = isStreaming,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STOP STREAM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Viewer Guide Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Live Stream Viewer Instructions:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AccentCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Tap START STREAM to publish.\n2. Open http://15.207.177.194:8081/web/player.html in a browser.\n3. Type your roll number (${if (rollNumberInput.isNotEmpty()) rollNumberInput else "YOUR_ROLLNO"}) and click Watch.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun StatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isOk: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isOk) StatusGreen else StatusYellow,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
