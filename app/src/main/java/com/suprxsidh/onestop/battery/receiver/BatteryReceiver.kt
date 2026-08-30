package com.suprxsidh.onestop.battery.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.PowerManager
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.service.ChargeSessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BatteryReceiver(
    private val databaseProvider: (Context) -> AppDatabase = { ctx -> AppDatabase.getInstance(ctx) }
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> handleBatteryChanged(context, intent)
            Intent.ACTION_POWER_CONNECTED -> ChargeSessionService.start(context)
            Intent.ACTION_POWER_DISCONNECTED -> ChargeSessionService.stop(context)
        }
    }

    private fun handleBatteryChanged(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return

        val tempTenthsC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val screenOn = powerManager?.isInteractive ?: true

        val reading = BatteryReadingParser.parse(
            level = level,
            scale = scale,
            tempTenthsC = tempTenthsC,
            voltageMv = voltageMv,
            currentUa = currentUa,
            status = status,
            plugType = plugType,
            screenOn = screenOn,
            nowTs = System.currentTimeMillis()
        )

        // Blocks until the insert completes so onReceive() doesn't return (and make the process
        // eligible for teardown) before the write lands — a fire-and-forget launch{} here could
        // silently drop the reading if Android kills the process right after the broadcast.
        val db = databaseProvider(context)
        runBlocking(Dispatchers.IO) {
            db.readingDao().insert(reading)
        }
    }
}
