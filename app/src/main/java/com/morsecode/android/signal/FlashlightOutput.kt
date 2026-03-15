package com.morsecode.android.signal

import android.content.Context
import android.hardware.camera2.CameraManager

/**
 * 闪光灯输出实现
 */
class FlashlightOutput(context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var isOn = false

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
        } catch (_: Exception) {
            cameraId = null
        }
    }

    fun turnOn() {
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, true)
                isOn = true
            } catch (_: Exception) { }
        }
    }

    fun turnOff() {
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, false)
                isOn = false
            } catch (_: Exception) { }
        }
    }

    fun stop() {
        if (isOn) turnOff()
    }

    fun isAvailable(): Boolean = cameraId != null
}
