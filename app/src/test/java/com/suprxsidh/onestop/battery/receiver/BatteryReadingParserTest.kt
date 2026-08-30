package com.suprxsidh.onestop.battery.receiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BatteryReadingParserTest {
    @Test
    fun `parses pct from level over scale`() {
        val reading = BatteryReadingParser.parse(
            level = 55, scale = 100, tempTenthsC = 305, voltageMv = 3900,
            currentUa = 500_000, status = 2, plugType = 1, screenOn = true, nowTs = 1_000L
        )
        assertEquals(55, reading.pct)
        assertEquals(30.5f, reading.tempC, 0.001f)
        assertEquals(1_000L, reading.ts)
    }

    @Test
    fun `computes watts from voltage and current`() {
        val reading = BatteryReadingParser.parse(
            level = 50, scale = 100, tempTenthsC = 250, voltageMv = 5000,
            currentUa = 2_000_000, status = 2, plugType = 1, screenOn = false, nowTs = 0L
        )
        assertEquals(10.0f, reading.watts, 0.001f)
    }

    @Test
    fun `rejects zero scale`() {
        assertThrows(IllegalArgumentException::class.java) {
            BatteryReadingParser.parse(50, 0, 250, 4000, 0, 2, 1, false, 0L)
        }
    }
}
