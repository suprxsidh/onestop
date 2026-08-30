package com.suprxsidh.onestop.battery.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeEstimatorTest {
    @Test
    fun `minutes to full from charge rate`() {
        assertEquals(40L, TimeEstimator.minutesToFull(currentPct = 60, avgPctPerMinuteCharging = 1.0))
    }

    @Test
    fun `null when charge rate is non-positive`() {
        assertNull(TimeEstimator.minutesToFull(60, 0.0))
    }

    @Test
    fun `minutes to empty from discharge rate`() {
        assertEquals(50L, TimeEstimator.minutesToEmpty(currentPct = 50, avgPctPerMinuteDischarging = 1.0))
    }

    @Test
    fun `null when discharge rate is non-positive`() {
        assertNull(TimeEstimator.minutesToEmpty(50, 0.0))
    }
}
