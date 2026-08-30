package com.suprxsidh.onestop.gestures.detect

import org.junit.Assert.assertEquals
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun `steady readings never fire`() {
        var shakes = 0
        val detector = ShakeDetector(onShake = { shakes++ })
        var t = 0L
        repeat(10) {
            detector.onSensorChanged(0f, 9.8f, 0f, t)
            t += 20
        }
        assertEquals(0, shakes)
    }

    @Test
    fun `a large jump between consecutive samples fires once`() {
        var shakes = 0
        val detector = ShakeDetector(onShake = { shakes++ })
        detector.onSensorChanged(0f, 9.8f, 0f, 0L)
        detector.onSensorChanged(30f, 9.8f, 0f, 20L)
        assertEquals(1, shakes)
    }

    @Test
    fun `repeated jumps within the debounce window fire only once`() {
        var shakes = 0
        val detector = ShakeDetector(minIntervalMs = 1_000L, onShake = { shakes++ })
        detector.onSensorChanged(0f, 9.8f, 0f, 0L)
        detector.onSensorChanged(30f, 9.8f, 0f, 20L)
        detector.onSensorChanged(0f, 9.8f, 0f, 40L)
        detector.onSensorChanged(30f, 9.8f, 0f, 60L)
        assertEquals(1, shakes)
    }

    @Test
    fun `a second jump after the debounce window fires again`() {
        var shakes = 0
        val detector = ShakeDetector(minIntervalMs = 1_000L, onShake = { shakes++ })
        detector.onSensorChanged(0f, 9.8f, 0f, 0L)
        detector.onSensorChanged(30f, 9.8f, 0f, 20L)
        detector.onSensorChanged(0f, 9.8f, 0f, 1_100L)
        detector.onSensorChanged(30f, 9.8f, 0f, 1_120L)
        assertEquals(2, shakes)
    }
}
