package com.suprxsidh.onestop.battery.calc

import com.suprxsidh.onestop.battery.data.ChargeSession

data class HighTempWarning(val sessionId: Long, val peakTempC: Float)

object InsightsEngine {
    const val HIGH_TEMP_THRESHOLD_C = 40f

    fun highTempWarnings(sessions: List<ChargeSession>): List<HighTempWarning> =
        sessions.filter { (it.peakTempC ?: 0f) >= HIGH_TEMP_THRESHOLD_C }
            .map { HighTempWarning(it.id, it.peakTempC ?: 0f) }

    fun fastestChargeWindow(sessions: List<ChargeSession>): ChargeSession? =
        sessions.filter { it.startPct in 15..25 && (it.endPct ?: 0) in 75..85 }
            .minByOrNull { it.durationS ?: Long.MAX_VALUE }

    fun bestSession(sessions: List<ChargeSession>): ChargeSession? =
        sessions.maxByOrNull { it.avgWatts ?: 0f }

    fun worstSession(sessions: List<ChargeSession>): ChargeSession? =
        sessions.filter { it.avgWatts != null }.minByOrNull { it.avgWatts ?: Float.MAX_VALUE }
}
