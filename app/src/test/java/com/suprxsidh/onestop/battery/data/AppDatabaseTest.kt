package com.suprxsidh.onestop.battery.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndReadReading() = runTest {
        val reading = Reading(
            ts = 1_000L, pct = 55, tempC = 30.5f, voltageMv = 3900,
            currentUa = 500_000, watts = 1.95f, status = 2, plugType = 1, screenOn = true
        )
        db.readingDao().insert(reading)
        val all = db.readingDao().between(0, 2_000)
        assertEquals(1, all.size)
        assertEquals(55, all[0].pct)
    }

    @Test
    fun insertAndReadChargeSession() = runTest {
        val session = ChargeSession(
            startTs = 100L, endTs = 200L, startPct = 20, endPct = 80,
            mahAdded = 1500.0, avgWatts = 10f, peakWatts = 18f,
            avgTempC = 32f, peakTempC = 38f, durationS = 100, chargerType = "FAST"
        )
        val id = db.chargeSessionDao().insert(session)
        val fetched = db.chargeSessionDao().byId(id)
        assertEquals(80, fetched?.endPct)
    }

    @Test
    fun insertAndReadHealthEstimate() = runTest {
        val estimate = HealthEstimate(
            sessionId = 1L, ts = 500L, estFullCapacityMah = 4200.0,
            designCapacityMah = 4500.0, healthPct = 93.3, cycleCount = 120, source = "estimated"
        )
        db.healthEstimateDao().insert(estimate)
        val latest = db.healthEstimateDao().all()
        assertEquals(1, latest.size)
        assertEquals("estimated", latest[0].source)
    }
}
