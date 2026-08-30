package com.suprxsidh.onestop.battery.work

import android.content.Context
import android.os.BatteryManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.suprxsidh.onestop.battery.data.AppDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class SamplingWorkerTest {

    private lateinit var db: AppDatabase

    // Always runs, even if an assertion above throws — otherwise a failed test leaves
    // AppDatabase's companion `instance` pointing at this (closed) in-memory DB for every
    // later test in the same JVM.
    @After
    fun tearDown() {
        AppDatabase.clearTestInstance()
        if (::db.isInitialized) db.close()
    }

    @Test
    fun `worker inserts a reading and succeeds when not charging`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        AppDatabase.setTestInstance(db)

        val worker = TestListenableWorkerBuilder<SamplingWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val readings = db.readingDao().between(0, Long.MAX_VALUE)
        assertEquals(1, readings.size)
    }

    @Test
    fun `worker skips inserting a reading when charging`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        AppDatabase.setTestInstance(db)

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        shadowOf(batteryManager).setIntProperty(
            BatteryManager.BATTERY_PROPERTY_STATUS,
            BatteryManager.BATTERY_STATUS_CHARGING
        )

        val worker = TestListenableWorkerBuilder<SamplingWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val readings = db.readingDao().between(0, Long.MAX_VALUE)
        assertEquals(0, readings.size)
    }
}
