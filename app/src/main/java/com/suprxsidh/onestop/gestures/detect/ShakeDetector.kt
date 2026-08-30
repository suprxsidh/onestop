package com.suprxsidh.onestop.gestures.detect

import kotlin.math.sqrt

/**
 * Fires when the acceleration vector changes sharply between two consecutive
 * samples (a "jerk"). Using the delta between samples rather than raw
 * magnitude means gravity's constant contribution cancels out naturally --
 * no separate gravity-subtraction step is needed.
 */
class ShakeDetector(
    private val thresholdMetersPerSecondSquared: Float = 15f,
    private val minIntervalMs: Long = 1_000L,
    private val onShake: () -> Unit
) {
    private var hasLastSample = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastShakeAtMs = Long.MIN_VALUE / 2

    fun onSensorChanged(x: Float, y: Float, z: Float, timestampMs: Long) {
        if (!hasLastSample) {
            lastX = x
            lastY = y
            lastZ = z
            hasLastSample = true
            return
        }

        val dx = x - lastX
        val dy = y - lastY
        val dz = z - lastZ
        lastX = x
        lastY = y
        lastZ = z

        val jerk = sqrt(dx * dx + dy * dy + dz * dz)
        if (jerk > thresholdMetersPerSecondSquared && timestampMs - lastShakeAtMs > minIntervalMs) {
            lastShakeAtMs = timestampMs
            onShake()
        }
    }
}
