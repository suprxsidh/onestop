package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Test

class DischargeCalculatorTest {
    private fun reading(ts: Long, pct: Int, currentUa: Int, screenOn: Boolean) = Reading(
        ts = ts, pct = pct, tempC = 25f, voltageMv = 3900, currentUa = currentUa,
        watts = 0f, status = 3, plugType = 0, screenOn = screenOn
    )

    @Test
    fun `drain rate over one hour`() {
        val start = reading(ts = 0L, pct = 80, currentUa = -400_000, screenOn = true)
        val end = reading(ts = 3_600_000L, pct = 70, currentUa = -400_000, screenOn = true)
        val rate = DischargeCalculator.drainRate(start, end)
        assertEquals(10.0, rate.pctPerHour, 0.001)
        assertEquals(400.0, rate.maPerHour, 0.001)
    }

    @Test
    fun `screen on vs off average current`() {
        val readings = listOf(
            reading(0L, 80, -600_000, screenOn = true),
            reading(1L, 79, -400_000, screenOn = true),
            reading(2L, 78, -100_000, screenOn = false),
            reading(3L, 77, -50_000, screenOn = false)
        )
        val (onAvg, offAvg) = DischargeCalculator.screenOnVsOffDrain(readings)
        assertEquals(500.0, onAvg, 0.001)
        assertEquals(75.0, offAvg, 0.001)
    }
}
