package com.suprxsidh.onestop.battery.ui.tile

import android.os.BatteryManager
import com.suprxsidh.onestop.battery.calc.DischargeCalculator
import com.suprxsidh.onestop.battery.calc.TimeEstimator
import com.suprxsidh.onestop.battery.data.Reading
import kotlin.math.abs

object BatteryTileMapper {
    fun toBatteryTileState(recentReadings: List<Reading>): BatteryTileState {
        if (recentReadings.isEmpty()) {
            return BatteryTileState(
                percent = null,
                isCharging = false,
                minutesRemaining = null,
                peakWatts = null,
                sparklinePercents = emptyList()
            )
        }

        val chronological = recentReadings.sortedBy { it.ts }
        val latest = chronological.last()

        val minutesRemaining = if (chronological.size >= 2) {
            val oldest = chronological.first()
            runCatching {
                val rate = DischargeCalculator.drainRate(oldest, latest)
                if (rate.pctPerHour <= 0) null else TimeEstimator.minutesToEmpty(latest.pct, rate.pctPerHour / 60.0)
            }.getOrNull()
        } else null

        return BatteryTileState(
            percent = latest.pct,
            isCharging = latest.status == BatteryManager.BATTERY_STATUS_CHARGING,
            minutesRemaining = minutesRemaining,
            peakWatts = chronological.maxOf { abs(it.watts) },
            sparklinePercents = chronological.map { it.pct }
        )
    }
}
