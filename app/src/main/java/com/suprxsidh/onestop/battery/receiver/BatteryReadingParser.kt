package com.suprxsidh.onestop.battery.receiver

import com.suprxsidh.onestop.battery.calc.WattCalculator
import com.suprxsidh.onestop.battery.data.Reading

object BatteryReadingParser {
    fun parse(
        level: Int,
        scale: Int,
        tempTenthsC: Int,
        voltageMv: Int,
        currentUa: Int,
        status: Int,
        plugType: Int,
        screenOn: Boolean,
        nowTs: Long
    ): Reading {
        require(scale > 0) { "scale must be positive" }
        return Reading(
            ts = nowTs,
            pct = (level * 100) / scale,
            tempC = tempTenthsC / 10f,
            voltageMv = voltageMv,
            currentUa = currentUa,
            watts = WattCalculator.watts(voltageMv, currentUa),
            status = status,
            plugType = plugType,
            screenOn = screenOn
        )
    }
}
