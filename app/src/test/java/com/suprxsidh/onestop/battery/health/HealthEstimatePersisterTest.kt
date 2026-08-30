package com.suprxsidh.onestop.battery.health

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.ChargeSession
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// application = Application::class swaps out the manifest-declared BatteryLabApplication for a
// plain Application during Robolectric's automatic bootstrap — otherwise Robolectric would call
// BatteryLabApplication.onCreate() (which calls WorkManager.getInstance()) before this test body
// runs, crashing with "WorkManager is not initialized properly" (same issue and same fix as
// BatteryLabApplicationTest). This test never touches BatteryLabApplication, so swapping it for a
// plain Application has no effect on what's under test.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26], application = Application::class)
class HealthEstimatePersisterTest {

    @Before
    fun stubPowerProfileReflection() {
        // Bypass the hidden-API reflection entirely rather than relying on Robolectric's
        // PowerProfile stub behavior (it returns a placeholder capacity instead of failing, unlike
        // a genuine device) — this exercises the derived-from-full-session fallback path
        // deterministically regardless of what the test runtime's hidden APIs happen to do.
        DesignCapacityStore.powerProfileReader = { null }
    }

    @After
    fun restorePowerProfileReflection() {
        DesignCapacityStore.powerProfileReader = DesignCapacityStore::readViaPowerProfileReflectionImpl
    }

    @Test
    fun `persists an estimated health record for a full-span session`() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()

        val session = ChargeSession(
            startTs = 0L, endTs = 3_600_000L, startPct = 2, endPct = 98,
            mahAdded = 4700.0, avgWatts = 10f, peakWatts = 15f,
            avgTempC = 30f, peakTempC = 34f, durationS = 3600, chargerType = "STANDARD"
        )
        val id = db.chargeSessionDao().insert(session)

        HealthEstimatePersister.persist(context, db, session.copy(id = id))

        val estimates = db.healthEstimateDao().all()
        assertEquals(1, estimates.size)
        assertEquals("estimated", estimates[0].source)
        assertEquals(100.0, estimates[0].healthPct, 0.01)
        assertNull(estimates[0].cycleCount)
        db.close()
    }

    @Test
    fun `skips persistence when session has no percent delta`() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()

        val session = ChargeSession(
            startTs = 0L, endTs = 100L, startPct = 50, endPct = 50,
            mahAdded = 0.0, avgWatts = 0f, peakWatts = 0f,
            avgTempC = 30f, peakTempC = 30f, durationS = 100, chargerType = "UNKNOWN"
        )
        val id = db.chargeSessionDao().insert(session)

        HealthEstimatePersister.persist(context, db, session.copy(id = id))

        assertEquals(0, db.healthEstimateDao().all().size)
        db.close()
    }
}
