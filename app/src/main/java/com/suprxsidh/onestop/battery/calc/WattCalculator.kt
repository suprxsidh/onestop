package com.suprxsidh.onestop.battery.calc

object WattCalculator {
    fun watts(voltageMv: Int, currentUa: Int): Float =
        (voltageMv / 1000f) * (currentUa / 1_000_000f)
}
