package com.suprxsidh.onestop.battery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charge_sessions")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTs: Long,
    val endTs: Long?,
    val startPct: Int,
    val endPct: Int?,
    val mahAdded: Double?,
    val avgWatts: Float?,
    val peakWatts: Float?,
    val avgTempC: Float?,
    val peakTempC: Float?,
    val durationS: Long?,
    val chargerType: String?
)
