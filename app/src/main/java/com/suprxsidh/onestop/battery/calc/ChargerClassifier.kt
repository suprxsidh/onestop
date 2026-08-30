package com.suprxsidh.onestop.battery.calc

enum class ChargerType(val label: String) {
    SLOW("~5W slow charger"),
    STANDARD("~10W standard charger"),
    FAST("~18W fast charger"),
    SUPER_FAST("~30W+ super-fast charger"),
    UNKNOWN("Unknown charger")
}

object ChargerClassifier {
    fun classify(avgWatts: Float): ChargerType = when {
        avgWatts <= 0f -> ChargerType.UNKNOWN
        avgWatts < 7f -> ChargerType.SLOW
        avgWatts < 12f -> ChargerType.STANDARD
        avgWatts < 22f -> ChargerType.FAST
        else -> ChargerType.SUPER_FAST
    }
}
