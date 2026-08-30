package com.suprxsidh.onestop.battery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_estimates")
data class HealthEstimate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val ts: Long,
    val estFullCapacityMah: Double,
    val designCapacityMah: Double,
    val healthPct: Double,
    val cycleCount: Int?,
    val source: String
)
