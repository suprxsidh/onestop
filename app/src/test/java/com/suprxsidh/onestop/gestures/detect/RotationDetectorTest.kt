package com.suprxsidh.onestop.gestures.detect

import org.junit.Assert.assertEquals
import org.junit.Test

class RotationDetectorTest {

    @Test
    fun `low angular velocity never fires`() {
        var rotations = 0
        val detector = RotationDetector(onRotate = { rotations++ })
        detector.onSensorChanged(0.1f, 0.1f, 0.1f, 0L)
        detector.onSensorChanged(0.2f, 0.0f, 0.1f, 20L)
        assertEquals(0, rotations)
    }

    @Test
    fun `a fast spin fires once`() {
        var rotations = 0
        val detector = RotationDetector(angularVelocityThresholdRadPerSec = 6f, onRotate = { rotations++ })
        detector.onSensorChanged(0f, 8f, 0f, 0L)
        assertEquals(1, rotations)
    }

    @Test
    fun `repeated fast readings within the debounce window fire only once`() {
        var rotations = 0
        val detector = RotationDetector(minIntervalMs = 1_000L, onRotate = { rotations++ })
        detector.onSensorChanged(0f, 8f, 0f, 0L)
        detector.onSensorChanged(0f, 8f, 0f, 100L)
        assertEquals(1, rotations)
    }

    @Test
    fun `a second fast spin after the debounce window fires again`() {
        var rotations = 0
        val detector = RotationDetector(minIntervalMs = 1_000L, onRotate = { rotations++ })
        detector.onSensorChanged(0f, 8f, 0f, 0L)
        detector.onSensorChanged(0f, 8f, 0f, 1_100L)
        assertEquals(2, rotations)
    }
}
