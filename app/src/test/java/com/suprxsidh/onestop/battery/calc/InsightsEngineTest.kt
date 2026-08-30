package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.ChargeSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InsightsEngineTest {
    private fun session(
        id: Long, startPct: Int, endPct: Int, avgWatts: Float?, peakTempC: Float?, durationS: Long?
    ) = ChargeSession(
        id = id, startTs = 0, endTs = durationS?.let { it * 1000 }, startPct = startPct, endPct = endPct,
        mahAdded = null, avgWatts = avgWatts, peakWatts = null, avgTempC = null,
        peakTempC = peakTempC, durationS = durationS, chargerType = null
    )

    @Test
    fun `flags sessions above the high-temp threshold`() {
        val sessions = listOf(
            session(1, 20, 80, 10f, 38f, 100),
            session(2, 20, 80, 10f, 42f, 100)
        )
        val warnings = InsightsEngine.highTempWarnings(sessions)
        assertEquals(1, warnings.size)
        assertEquals(2L, warnings[0].sessionId)
    }

    @Test
    fun `finds fastest 20 to 80 window`() {
        val sessions = listOf(
            session(1, 18, 80, 10f, 30f, 3000),
            session(2, 20, 80, 15f, 30f, 1800),
            session(3, 50, 90, 10f, 30f, 500)
        )
        assertEquals(2L, InsightsEngine.fastestChargeWindow(sessions)?.id)
    }

    @Test
    fun `best and worst session by average watts`() {
        val sessions = listOf(
            session(1, 20, 80, 5f, 30f, 100),
            session(2, 20, 80, 20f, 30f, 100)
        )
        assertEquals(2L, InsightsEngine.bestSession(sessions)?.id)
        assertEquals(1L, InsightsEngine.worstSession(sessions)?.id)
    }

    @Test
    fun `empty session list yields no insights`() {
        assertEquals(0, InsightsEngine.highTempWarnings(emptyList()).size)
        assertNull(InsightsEngine.fastestChargeWindow(emptyList()))
        assertNull(InsightsEngine.bestSession(emptyList()))
    }
}
