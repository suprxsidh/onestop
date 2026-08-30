package com.suprxsidh.onestop.battery.receiver

import android.content.Intent
import android.os.BatteryManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.battery.data.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BatteryReceiverTest {

    @Test
    fun `battery changed intent inserts a reading`() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()

        val intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 60)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 280)
            putExtra(BatteryManager.EXTRA_VOLTAGE, 3950)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
            putExtra(BatteryManager.EXTRA_PLUGGED, 0)
        }

        val receiver = BatteryReceiver { db }
        receiver.onReceive(context, intent)

        val readings = db.readingDao().between(0, Long.MAX_VALUE)
        assertEquals(1, readings.size)
        assertEquals(60, readings[0].pct)
        db.close()
    }
}
