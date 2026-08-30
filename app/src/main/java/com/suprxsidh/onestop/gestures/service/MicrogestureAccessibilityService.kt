package com.suprxsidh.onestop.gestures.service

import android.accessibilityservice.AccessibilityService
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.accessibility.AccessibilityEvent
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.detect.RotationDetector
import com.suprxsidh.onestop.gestures.detect.ShakeDetector
import com.suprxsidh.onestop.gestures.guard.DispatchGuard
import com.suprxsidh.onestop.gestures.model.GestureSettings
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MicrogestureAccessibilityService : AccessibilityService(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val currentSettings = MutableStateFlow(GestureSettings())
    private var currentForegroundPackage: String? = null

    private lateinit var sensorManager: SensorManager
    private lateinit var repository: GestureSettingsRepository

    private val shakeDetector = ShakeDetector(onShake = { onGesture(GestureType.SHAKE) })
    private val rotationDetector = RotationDetector(onRotate = { onGesture(GestureType.ROTATE) })

    override fun onServiceConnected() {
        super.onServiceConnected()
        sensorManager = getSystemService(SensorManager::class.java)
        repository = GestureSettingsRepository(applicationContext)

        serviceScope.launch {
            repository.settings.collect { currentSettings.value = it }
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            currentForegroundPackage = event.packageName?.toString()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val timestampMs = event.timestamp / 1_000_000
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER ->
                shakeDetector.onSensorChanged(event.values[0], event.values[1], event.values[2], timestampMs)
            Sensor.TYPE_GYROSCOPE ->
                rotationDetector.onSensorChanged(event.values[0], event.values[1], event.values[2], timestampMs)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    private fun onGesture(gestureType: GestureType) {
        val action = DispatchGuard.resolveAction(currentForegroundPackage, currentSettings.value, gestureType)
        if (action != null) {
            performGlobalAction(action.toGlobalActionInt())
        }
    }
}

private fun GlobalActionType.toGlobalActionInt(): Int = when (this) {
    GlobalActionType.BACK -> AccessibilityService.GLOBAL_ACTION_BACK
    GlobalActionType.RECENTS -> AccessibilityService.GLOBAL_ACTION_RECENTS
    GlobalActionType.NOTIFICATIONS -> AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
    GlobalActionType.NONE -> error("NONE is filtered out by DispatchGuard.resolveAction() before dispatch")
}
