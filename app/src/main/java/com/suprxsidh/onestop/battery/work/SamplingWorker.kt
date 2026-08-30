package com.suprxsidh.onestop.battery.work

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.receiver.BatteryReadingParser

class SamplingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            // ChargeSessionService already samples at high frequency while charging.
            return Result.success()
        }

        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val screenOn = powerManager?.isInteractive ?: true

        // Registering with a null receiver returns the last ACTION_BATTERY_CHANGED sticky intent,
        // which is the only place temp/voltage are exposed (BatteryManager.getIntProperty doesn't have them).
        val sticky = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tempTenthsC = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltageMv = sticky?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val reading = BatteryReadingParser.parse(
            level = level, scale = 100, tempTenthsC = tempTenthsC, voltageMv = voltageMv,
            currentUa = currentUa, status = status, plugType = 0,
            screenOn = screenOn, nowTs = System.currentTimeMillis()
        )

        AppDatabase.getInstance(applicationContext).readingDao().insert(reading)
        return Result.success()
    }
}
