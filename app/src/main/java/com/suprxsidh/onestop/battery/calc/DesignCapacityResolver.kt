package com.suprxsidh.onestop.battery.calc

object DesignCapacityResolver {
    fun resolve(cachedMah: Double?, reflectedMah: Double?, derivedFromFullSessionMah: Double?): Double? =
        cachedMah ?: reflectedMah ?: derivedFromFullSessionMah
}
