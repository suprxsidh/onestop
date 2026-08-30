package com.suprxsidh.onestop

import android.app.Application
import android.content.IntentFilter
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.suprxsidh.onestop.battery.receiver.BatteryReceiver
import com.suprxsidh.onestop.battery.work.SamplingWorker
import java.util.concurrent.TimeUnit

class OneStopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerBatteryReceiver()
        enqueueSamplingWork()
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(android.content.Intent.ACTION_BATTERY_CHANGED)
            addAction(android.content.Intent.ACTION_POWER_CONNECTED)
            addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(BatteryReceiver(), filter)
    }

    private fun enqueueSamplingWork() {
        val request = PeriodicWorkRequestBuilder<SamplingWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "battery_sampling",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
