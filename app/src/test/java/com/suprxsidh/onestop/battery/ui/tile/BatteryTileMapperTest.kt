package com.suprxsidh.onestop.battery.ui.tile

import android.os.BatteryManager
import com.suprxsidh.onestop.battery.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryTileMapperTest {

    // Fixed epoch base so "minutes ago" maps to an *earlier* (smaller) timestamp,
    // not a later one -- ts must increase forward in time for drainRate()/sortedBy{ts} to behave.
    private val now = 1_000_000_000L

    private fun reading(
        tsMinutesAgo: Long,
        pct: Int,
        watts: Float,
        status: Int = BatteryManager.BATTERY_STATUS_DISCHARGING
    ) = Reading(
        ts = now - tsMinutesAgo * 60_000L,
        pct = pct,
        tempC = 30f,
        voltageMv = 3800,
        currentUa = -500_000,
        watts = watts,
        status = status,
        plugType = 0,
        screenOn = true
    )

    @Test
    fun `empty readings produces empty state`() {
        val state = BatteryTileMapper.toBatteryTileState(emptyList())
        assertNull(state.percent)
        assertTrue(!state.isCharging)
        assertNull(state.minutesRemaining)
        assertNull(state.peakWatts)
        assertTrue(state.sparklinePercents.isEmpty())
    }

    @Test
    fun `single reading has percent but no time remaining`() {
        val state = BatteryTileMapper.toBatteryTileState(listOf(reading(tsMinutesAgo = 0, pct = 62, watts = -4.5f)))
        assertEquals(62, state.percent)
        assertNull(state.minutesRemaining)
        assertEquals(4.5f, state.peakWatts)
        assertEquals(listOf(62), state.sparklinePercents)
    }

    @Test
    fun `discharging status is reflected`() {
        val state = BatteryTileMapper.toBatteryTileState(
            listOf(reading(tsMinutesAgo = 0, pct = 50, watts = -3f, status = BatteryManager.BATTERY_STATUS_DISCHARGING))
        )
        assertTrue(!state.isCharging)
    }

    @Test
    fun `charging status is reflected`() {
        val state = BatteryTileMapper.toBatteryTileState(
            listOf(reading(tsMinutesAgo = 0, pct = 50, watts = 5f, status = BatteryManager.BATTERY_STATUS_CHARGING))
        )
        assertTrue(state.isCharging)
    }

    @Test
    fun `two readings compute minutes remaining from drain rate`() {
        // 90 minutes apart, dropped from 80% to 65% => 15% / 1.5h = 10%/h => 65% left / (10%/h) = 6.5h = 390min
        val older = reading(tsMinutesAgo = 90, pct = 80, watts = -3f)
        val newer = reading(tsMinutesAgo = 0, pct = 65, watts = -3.5f)
        val state = BatteryTileMapper.toBatteryTileState(listOf(older, newer))
        assertEquals(390L, state.minutesRemaining)
    }

    @Test
    fun `peak watts is the max magnitude across readings`() {
        val readings = listOf(
            reading(tsMinutesAgo = 30, pct = 70, watts = -2f),
            reading(tsMinutesAgo = 15, pct = 66, watts = -9.2f),
            reading(tsMinutesAgo = 0, pct = 62, watts = -4f)
        )
        val state = BatteryTileMapper.toBatteryTileState(readings)
        assertEquals(9.2f, state.peakWatts)
    }

    @Test
    fun `sparkline percents follow chronological order regardless of input order`() {
        val readings = listOf(
            reading(tsMinutesAgo = 0, pct = 62, watts = -4f),
            reading(tsMinutesAgo = 30, pct = 70, watts = -2f),
            reading(tsMinutesAgo = 15, pct = 66, watts = -3f)
        )
        val state = BatteryTileMapper.toBatteryTileState(readings)
        assertEquals(listOf(70, 66, 62), state.sparklinePercents)
    }
}
