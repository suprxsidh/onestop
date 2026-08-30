package com.suprxsidh.onestop.battery.health

import android.content.Context

object DesignCapacityStore {
    private const val PREFS = "battery_lab_prefs"
    private const val KEY_DESIGN_CAPACITY_MAH = "design_capacity_mah"

    fun getCached(context: Context): Double? {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY_DESIGN_CAPACITY_MAH, -1f)
        return stored.toDouble().takeIf { it > 0.0 }
    }

    fun save(context: Context, mah: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_DESIGN_CAPACITY_MAH, mah.toFloat()).apply()
    }

    // Swappable seam: tests override this to bypass the hidden-API reflection entirely, rather
    // than relying on environment-specific throw/return-null behavior from the reflection call
    // itself (Robolectric's android-all-instrumented jar bundles a real PowerProfile stub whose
    // getBatteryCapacity() returns a fixed placeholder instead of failing, unlike a genuine device
    // where the class's presence/behavior are never guaranteed). The reflection function below
    // carries no test-environment awareness of its own. `internal` (not `private`) so a test can
    // restore the exact default after overriding it, instead of capturing a snapshot at setup time.
    internal var powerProfileReader: (Context) -> Double? = ::readViaPowerProfileReflectionImpl

    // com.android.internal.os.PowerProfile is a hidden platform class (spec §3), not part of the
    // public SDK — wrapped defensively since reflection can legitimately fail on some OEM builds.
    fun readViaPowerProfileReflection(context: Context): Double? = powerProfileReader(context)

    internal fun readViaPowerProfileReflectionImpl(context: Context): Double? = try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val constructor = powerProfileClass.getConstructor(Context::class.java)
        val instance = constructor.newInstance(context)
        val method = powerProfileClass.getMethod("getBatteryCapacity")
        (method.invoke(instance) as? Double)?.takeIf { it > 0.0 }
    } catch (e: ReflectiveOperationException) {
        null
    } catch (e: ClassCastException) {
        null
    }
}
