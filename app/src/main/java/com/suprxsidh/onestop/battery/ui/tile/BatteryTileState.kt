package com.suprxsidh.onestop.battery.ui.tile

data class BatteryTileState(
    val percent: Int?,
    val isCharging: Boolean,
    val minutesRemaining: Long?,
    val peakWatts: Float?,
    val sparklinePercents: List<Int>
)
