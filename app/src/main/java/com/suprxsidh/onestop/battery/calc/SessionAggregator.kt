package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.Reading

data class ChargeSessionAggregate(
    val startTs: Long,
    val endTs: Long,
    val startPct: Int,
    val endPct: Int,
    val mahAdded: Double,
    val avgWatts: Float,
    val peakWatts: Float,
    val avgTempC: Float,
    val peakTempC: Float,
    val durationS: Long
)

object SessionAggregator {
    fun aggregate(
        readings: List<Reading>,
        chargeCounterStartUah: Long,
        chargeCounterEndUah: Long
    ): ChargeSessionAggregate {
        require(readings.isNotEmpty()) { "readings must not be empty" }
        val sorted = readings.sortedBy { it.ts }
        val first = sorted.first()
        val last = sorted.last()
        return ChargeSessionAggregate(
            startTs = first.ts,
            endTs = last.ts,
            startPct = first.pct,
            endPct = last.pct,
            mahAdded = (chargeCounterEndUah - chargeCounterStartUah) / 1000.0,
            avgWatts = sorted.map { it.watts }.average().toFloat(),
            peakWatts = sorted.maxOf { it.watts },
            avgTempC = sorted.map { it.tempC }.average().toFloat(),
            peakTempC = sorted.maxOf { it.tempC },
            durationS = (last.ts - first.ts) / 1000
        )
    }
}
