package com.suprxsidh.onestop.battery.calc

object TimeEstimator {
    fun minutesToFull(currentPct: Int, avgPctPerMinuteCharging: Double): Long? {
        if (avgPctPerMinuteCharging <= 0) return null
        return ((100 - currentPct) / avgPctPerMinuteCharging).toLong()
    }

    fun minutesToEmpty(currentPct: Int, avgPctPerMinuteDischarging: Double): Long? {
        if (avgPctPerMinuteDischarging <= 0) return null
        return (currentPct / avgPctPerMinuteDischarging).toLong()
    }
}
