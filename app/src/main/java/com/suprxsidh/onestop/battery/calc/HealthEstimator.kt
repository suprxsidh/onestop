package com.suprxsidh.onestop.battery.calc

object HealthEstimator {
    fun estimateFullCapacityMah(
        chargeCounterStartUah: Long,
        chargeCounterEndUah: Long,
        startPct: Int,
        endPct: Int
    ): Double? {
        val pctDelta = endPct - startPct
        if (pctDelta <= 0) return null
        val mahDelta = (chargeCounterEndUah - chargeCounterStartUah) / 1000.0
        return mahDelta / (pctDelta / 100.0)
    }

    fun healthPct(estFullCapacityMah: Double, designCapacityMah: Double): Double {
        require(designCapacityMah > 0) { "designCapacityMah must be positive" }
        return (estFullCapacityMah / designCapacityMah) * 100.0
    }

    fun rollingMedian(values: List<Double>, window: Int): Double {
        require(values.isNotEmpty()) { "values must not be empty" }
        require(window > 0) { "window must be positive" }
        val recent = values.takeLast(window).sorted()
        val mid = recent.size / 2
        return if (recent.size % 2 == 0) (recent[mid - 1] + recent[mid]) / 2.0 else recent[mid]
    }
}
