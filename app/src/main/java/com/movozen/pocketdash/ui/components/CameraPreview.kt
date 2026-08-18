package com.movozen.pocketdash.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.movozen.pocketdash.streaming.RtmpStreamingManager
import com.pedro.library.view.OpenGlView

@Composable
fun CameraPreview(
    streamingManager: RtmpStreamingManager?,
    openGlView: OpenGlView,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            if (openGlView.parent != null) {
                (openGlView.parent as ViewGroup).removeView(openGlView)
            }
            openGlView
        },
        modifier = modifier,
        update = {
            streamingManager?.startPreview()
        }
    )
}
