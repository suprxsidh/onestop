package com.suprxsidh.onestop.battery.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val pct: Int,
    val tempC: Float,
    val voltageMv: Int,
    val currentUa: Int,
    val watts: Float,
    val status: Int,
    val plugType: Int,
    val screenOn: Boolean
)
