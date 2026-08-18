package com.movozen.pocketdash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.movozen.pocketdash.data.PreferencesManager
import com.movozen.pocketdash.streaming.RtmpStreamingManager
import com.movozen.pocketdash.ui.PocketDashApp
import com.movozen.pocketdash.ui.theme.PocketDashTheme
import com.pedro.library.view.OpenGlView

class MainActivity : ComponentActivity() {

    private val TAG = "PocketDash"

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var openGlView: OpenGlView
    private var streamingManager: RtmpStreamingManager? = null

    private var hasPermissionsState = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val allGranted = cameraGranted && audioGranted

        Log.d(TAG, "Permissions Result - Camera: $cameraGranted, Audio: $audioGranted")
        hasPermissionsState.value = allGranted

        if (allGranted) {
            initStreamingManager()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while dashcam is active
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        preferencesManager = PreferencesManager(this)
        openGlView = OpenGlView(this)

        checkPermissionsAndInit()

        setContent {
            PocketDashTheme {
                PocketDashApp(
                    streamingManager = streamingManager,
                    openGlView = openGlView,
                    preferencesManager = preferencesManager,
                    hasPermissions = hasPermissionsState.value,
                    onRequestPermissions = { requestPermissions() }
                )
            }
        }
    }

    private fun checkPermissionsAndInit() {
        val cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val audioGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        hasPermissionsState.value = cameraGranted && audioGranted

        if (hasPermissionsState.value) {
            initStreamingManager()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun initStreamingManager() {
        if (streamingManager == null) {
            try {
                streamingManager = RtmpStreamingManager(this, openGlView)
                Log.d(TAG, "Initialized RtmpStreamingManager successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize RtmpStreamingManager", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissionsState.value) {
            streamingManager?.startPreview()
        }
    }

    override fun onStop() {
        super.onStop()
        streamingManager?.stopStream()
    }

    override fun onDestroy() {
        super.onDestroy()
        streamingManager?.release()
    }
}
