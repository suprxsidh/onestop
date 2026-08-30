package com.suprxsidh.onestop.battery.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.suprxsidh.onestop.battery.calc.ChargerClassifier
import com.suprxsidh.onestop.battery.calc.SessionAggregator
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.ChargeSession
import com.suprxsidh.onestop.battery.data.Reading
import com.suprxsidh.onestop.battery.health.HealthEstimatePersister
import com.suprxsidh.onestop.battery.receiver.BatteryReadingParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChargeSessionService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val sessionReadings = mutableListOf<Reading>()
    private var chargeCounterStartUah = 0L
    private var isSampling = false
    private var samplingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(watts = 0f, tempC = 0f))
        // Guard against a second sampling loop starting concurrently with one already running —
        // e.g. two rapid ACTION_POWER_CONNECTED broadcasts, or START_STICKY redelivery — which
        // would otherwise race on sessionReadings and overwrite chargeCounterStartUah mid-session.
        if (!isSampling) {
            startSampling()
        }
        return START_STICKY
    }

    // Temperature/voltage are only exposed via the ACTION_BATTERY_CHANGED sticky intent, not
    // BatteryManager.getIntProperty — registering with a null receiver returns the last sticky value.
    private fun stickyBatteryIntent(): Intent? =
        registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    private fun startSampling() {
        isSampling = true
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        chargeCounterStartUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toLong()
        sessionReadings.clear()

        samplingJob = scope.launch {
            while (isSampling) {
                val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val sticky = stickyBatteryIntent()
                val tempTenthsC = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                val voltageMv = sticky?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
                val reading = BatteryReadingParser.parse(
                    level = level, scale = 100, tempTenthsC = tempTenthsC, voltageMv = voltageMv,
                    currentUa = currentUa, status = BatteryManager.BATTERY_STATUS_CHARGING,
                    plugType = 1, screenOn = true, nowTs = System.currentTimeMillis()
                )
                sessionReadings.add(reading)
                updateNotification(reading.watts, reading.tempC)
                delay(SAMPLE_INTERVAL_MS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isSampling = false
        // Cancel and wait for the sampling coroutine to fully stop before touching sessionReadings.
        // isSampling=false alone only stops the NEXT loop iteration from starting — an iteration
        // already past the while-check keeps running (it's not a suspension point) and could still
        // call sessionReadings.add() while persistSession() below iterates the same list via
        // SessionAggregator, throwing ConcurrentModificationException. cancelAndJoin() forces the
        // loop to exit at its next suspension point (delay()) and waits for that to actually happen.
        samplingJob?.let { job -> runBlocking { job.cancelAndJoin() } }
        persistSession()
        scope.cancel()
    }

    private fun persistSession() {
        if (sessionReadings.isEmpty()) return
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val chargeCounterEndUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toLong()
        val aggregate = SessionAggregator.aggregate(sessionReadings, chargeCounterStartUah, chargeCounterEndUah)
        // Peak watts, not the whole-session average, is what actually identifies the charger's
        // capability — averaging over a session that includes hours of post-100% trickle charging
        // would dilute avgWatts toward zero and misclassify a genuine fast charger as SLOW/STANDARD.
        val chargerType = ChargerClassifier.classify(aggregate.peakWatts)

        val db = AppDatabase.getInstance(applicationContext)
        val session = ChargeSession(
            startTs = aggregate.startTs, endTs = aggregate.endTs,
            startPct = aggregate.startPct, endPct = aggregate.endPct,
            mahAdded = aggregate.mahAdded, avgWatts = aggregate.avgWatts,
            peakWatts = aggregate.peakWatts, avgTempC = aggregate.avgTempC,
            peakTempC = aggregate.peakTempC, durationS = aggregate.durationS,
            chargerType = chargerType.name
        )
        // Blocks onDestroy() until the insert (and the health-estimate persistence that depends on
        // its generated id) completes — a fire-and-forget launch{} here could lose the entire
        // charge session if the process is reclaimed right after stopService().
        runBlocking(Dispatchers.IO) {
            val id = db.chargeSessionDao().insert(session)
            HealthEstimatePersister.persist(applicationContext, db, session.copy(id = id))
        }
    }

    private fun buildNotification(watts: Float, tempC: Float): Notification {
        ensureChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Charging")
            .setContentText("%.1fW · %.1f°C".format(watts, tempC))
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(watts: Float, tempC: Float) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(watts, tempC))
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Charging session", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "charge_session"
        private const val NOTIFICATION_ID = 1
        private const val SAMPLE_INTERVAL_MS = 3_000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ChargeSessionService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ChargeSessionService::class.java))
        }
    }
}
