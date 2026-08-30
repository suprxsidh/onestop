package com.suprxsidh.onestop

import android.app.Application
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.suprxsidh.onestop.battery.receiver.BatteryReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// Robolectric instantiates the manifest-declared Application (OneStopApplication) and calls
// its onCreate() automatically during test environment setup, before this test's own body runs.
// That would call WorkManager.getInstance() before WorkManagerTestInitHelper has initialized the
// test WorkManager, crashing with "WorkManager is not initialized properly". Swap in a plain
// Application for Robolectric's automatic bootstrap lifecycle; the test still exercises the real
// OneStopApplication by constructing and calling onCreate() on it manually, after test
// WorkManager init, exactly like the brief's flow. The manually constructed instance needs a
// base Context attached (attachBaseContext is protected on ContextWrapper) before onCreate() can
// call registerReceiver/WorkManager.getInstance() without NPE-ing.
@Config(application = Application::class)
@RunWith(RobolectricTestRunner::class)
class OneStopApplicationTest {

    @Test
    fun `onCreate enqueues unique periodic sampling work`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        val app = OneStopApplication()
        val attachBaseContext = ContextWrapper::class.java.getDeclaredMethod(
            "attachBaseContext",
            android.content.Context::class.java
        )
        attachBaseContext.isAccessible = true
        attachBaseContext.invoke(app, context)
        app.onCreate()

        val infos = WorkManager.getInstance(context).getWorkInfosForUniqueWork("battery_sampling").get()
        assertEquals(1, infos.size)
        assertTrue(infos[0].state == WorkInfo.State.ENQUEUED || infos[0].state == WorkInfo.State.RUNNING)
    }

    @Test
    fun `onCreate registers BatteryReceiver for battery and power actions`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        val app = OneStopApplication()
        val attachBaseContext = ContextWrapper::class.java.getDeclaredMethod(
            "attachBaseContext",
            android.content.Context::class.java
        )
        attachBaseContext.isAccessible = true
        attachBaseContext.invoke(app, context)
        app.onCreate()

        val batteryReceiverFilters = shadowOf(context as Application).registeredReceivers
            .filter { it.broadcastReceiver is BatteryReceiver }
            .map { it.intentFilter }

        assertTrue(
            "expected a BatteryReceiver registered for ACTION_BATTERY_CHANGED",
            batteryReceiverFilters.any { it.hasAction(Intent.ACTION_BATTERY_CHANGED) }
        )
        assertTrue(
            "expected a BatteryReceiver registered for ACTION_POWER_CONNECTED",
            batteryReceiverFilters.any { it.hasAction(Intent.ACTION_POWER_CONNECTED) }
        )
        assertTrue(
            "expected a BatteryReceiver registered for ACTION_POWER_DISCONNECTED",
            batteryReceiverFilters.any { it.hasAction(Intent.ACTION_POWER_DISCONNECTED) }
        )
    }
}
