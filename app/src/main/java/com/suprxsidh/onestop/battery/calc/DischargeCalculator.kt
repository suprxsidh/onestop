package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.Reading
import kotlin.math.abs

data class DrainRate(val pctPerHour: Double, val maPerHour: Double)

object DischargeCalculator {
    fun drainRate(startReading: Reading, endReading: Reading): DrainRate {
        val hours = (endReading.ts - startReading.ts) / 3_600_000.0
        require(hours > 0) { "endReading must be after startReading" }
        val pctDelta = startReading.pct - endReading.pct
        val avgCurrentUa = (startReading.currentUa + endReading.currentUa) / 2.0
        return DrainRate(
            pctPerHour = pctDelta / hours,
            maPerHour = abs(avgCurrentUa) / 1000.0
        )
    }

    fun screenOnVsOffDrain(readings: List<Reading>): Pair<Double, Double> {
        val screenOn = readings.filter { it.screenOn }
        val screenOff = readings.filter { !it.screenOn }
        val onAvg = if (screenOn.isEmpty()) 0.0 else screenOn.map { abs(it.currentUa) / 1000.0 }.average()
        val offAvg = if (screenOff.isEmpty()) 0.0 else screenOff.map { abs(it.currentUa) / 1000.0 }.average()
        return onAvg to offAvg
    }
}
