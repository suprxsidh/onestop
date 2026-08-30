package com.suprxsidh.onestop.battery.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class WattCalculatorTest {
    @Test
    fun `positive current yields positive watts`() {
        val w = WattCalculator.watts(voltageMv = 5000, currentUa = 2_000_000)
        assertEquals(10.0f, w, 0.001f)
    }

    @Test
    fun `negative current yields negative watts`() {
        val w = WattCalculator.watts(voltageMv = 3900, currentUa = -300_000)
        assertEquals(-1.17f, w, 0.001f)
    }

    @Test
    fun `zero current yields zero watts`() {
        assertEquals(0.0f, WattCalculator.watts(voltageMv = 4000, currentUa = 0), 0.001f)
    }
}
