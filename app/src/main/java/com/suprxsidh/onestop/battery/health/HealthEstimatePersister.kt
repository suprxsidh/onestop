package com.suprxsidh.onestop.battery.health

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.suprxsidh.onestop.battery.calc.DesignCapacityResolver
import com.suprxsidh.onestop.battery.calc.HealthEstimator
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.ChargeSession
import com.suprxsidh.onestop.battery.data.HealthEstimate

object HealthEstimatePersister {

    suspend fun persist(context: Context, db: AppDatabase, session: ChargeSession) {
        val estimatedFullCapacity = estimateFullCapacityFromSession(session) ?: return

        val cached = DesignCapacityStore.getCached(context)
        val reflected = if (cached == null) DesignCapacityStore.readViaPowerProfileReflection(context) else null
        val derived = if (cached == null && reflected == null && isFullSpanSession(session)) {
            estimatedFullCapacity
        } else null
        val designCapacity = DesignCapacityResolver.resolve(cached, reflected, derived) ?: return

        if (cached == null) {
            (reflected ?: derived)?.let { DesignCapacityStore.save(context, it) }
        }

        val estimatedHealthPct = HealthEstimator.healthPct(estimatedFullCapacity, designCapacity)
        val reportedStateOfHealthPct = readReportedStateOfHealthPct(context)
        val cycleCount = readReportedCycleCount(context)

        db.healthEstimateDao().insert(
            HealthEstimate(
                sessionId = session.id,
                ts = session.endTs ?: System.currentTimeMillis(),
                estFullCapacityMah = estimatedFullCapacity,
                designCapacityMah = designCapacity,
                healthPct = reportedStateOfHealthPct?.toDouble() ?: estimatedHealthPct,
                cycleCount = cycleCount,
                source = if (reportedStateOfHealthPct != null) "reported" else "estimated"
            )
        )
    }

    private fun estimateFullCapacityFromSession(session: ChargeSession): Double? {
        val mahAdded = session.mahAdded ?: return null
        val endPct = session.endPct ?: return null
        val pctDelta = endPct - session.startPct
        if (pctDelta <= 0) return null
        return mahAdded / (pctDelta / 100.0)
    }

    private fun isFullSpanSession(session: ChargeSession): Boolean =
        session.startPct <= 5 && (session.endPct ?: 0) >= 95

    // BATTERY_PROPERTY_STATE_OF_HEALTH and EXTRA_CYCLE_COUNT are both API 34+ only (spec §3);
    // older/OEM-restricted devices fall through to the estimated path above.
    //
    // BATTERY_PROPERTY_STATE_OF_HEALTH is a @hide/@SystemApi constant that is absent from the
    // public compileSdk android.jar stubs (verified missing at API 34, 35, and 36) — unlike
    // EXTRA_CYCLE_COUNT below, which is a public constant. Resolved via reflection, same as the
    // hidden PowerProfile class in DesignCapacityStore, so this compiles against the public SDK
    // and degrades gracefully (returns null) wherever the field or property isn't available.
    private fun readReportedStateOfHealthPct(context: Context): Int? {
        if (Build.VERSION.SDK_INT < 34) return null
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val propertyId = BatteryManager::class.java.getField("BATTERY_PROPERTY_STATE_OF_HEALTH").getInt(null)
            val value = batteryManager.getIntProperty(propertyId)
            value.takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }
    }

    private fun readReportedCycleCount(context: Context): Int? {
        if (Build.VERSION.SDK_INT < 34) return null
        val sticky = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val cycles = sticky?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        return cycles.takeIf { it > 0 }
    }
}
