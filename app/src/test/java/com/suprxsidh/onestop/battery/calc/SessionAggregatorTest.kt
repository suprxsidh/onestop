package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAggregatorTest {
    private fun reading(ts: Long, pct: Int, watts: Float, tempC: Float) = Reading(
        ts = ts, pct = pct, tempC = tempC, voltageMv = 4000, currentUa = 0,
        watts = watts, status = 2, plugType = 1, screenOn = false
    )

    @Test
    fun `aggregates a charge session from a reading stream`() {
        val readings = listOf(
            reading(ts = 0L, pct = 20, watts = 8f, tempC = 30f),
            reading(ts = 60_000L, pct = 40, watts = 10f, tempC = 33f),
            reading(ts = 120_000L, pct = 80, watts = 6f, tempC = 35f)
        )
        val result = SessionAggregator.aggregate(
            readings = readings,
            chargeCounterStartUah = 1_000_000L,
            chargeCounterEndUah = 3_400_000L
        )
        assertEquals(0L, result.startTs)
        assertEquals(120_000L, result.endTs)
        assertEquals(20, result.startPct)
        assertEquals(80, result.endPct)
        assertEquals(2400.0, result.mahAdded, 0.001)
        assertEquals(8.0f, result.avgWatts, 0.001f)
        assertEquals(10.0f, result.peakWatts, 0.001f)
        assertEquals(35.0f, result.peakTempC, 0.001f)
        assertEquals(120L, result.durationS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects empty reading list`() {
        SessionAggregator.aggregate(emptyList(), 0L, 0L)
    }
}
