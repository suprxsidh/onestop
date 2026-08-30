package com.suprxsidh.onestop.gestures.detect

import kotlin.math.sqrt

/**
 * Fires on a short-duration angular-velocity spike from the gyroscope --
 * a deliberate quick flip/spin, not a static portrait/landscape state
 * (see spec §3). Raw magnitude is enough here, unlike shake: the gyroscope
 * already reports a near-zero baseline during ordinary handling.
 */
class RotationDetector(
    private val angularVelocityThresholdRadPerSec: Float = 6f,
    private val minIntervalMs: Long = 1_000L,
    private val onRotate: () -> Unit
) {
    private var lastRotateAtMs = Long.MIN_VALUE / 2

    fun onSensorChanged(x: Float, y: Float, z: Float, timestampMs: Long) {
        val magnitude = sqrt(x * x + y * y + z * z)
        if (magnitude > angularVelocityThresholdRadPerSec && timestampMs - lastRotateAtMs > minIntervalMs) {
            lastRotateAtMs = timestampMs
            onRotate()
        }
    }
}
