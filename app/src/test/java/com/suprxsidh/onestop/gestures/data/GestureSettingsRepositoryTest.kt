package com.suprxsidh.onestop.gestures.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Swap in a plain Application: Robolectric otherwise auto-bootstraps the real
// OneStopApplication, whose onCreate() calls WorkManager.getInstance() before
// any test initializes WorkManager, and this test has nothing to do with that.
@Config(application = Application::class)
@RunWith(RobolectricTestRunner::class)
class GestureSettingsRepositoryTest {

    private val repository = GestureSettingsRepository(ApplicationProvider.getApplicationContext())

    @Test
    fun `defaults are enabled with no mappings and empty blocklist`() = runTest {
        val settings = repository.settings.first()
        assertTrue(settings.enabled)
        assertEquals(GlobalActionType.NONE, settings.actionFor(GestureType.SHAKE))
        assertTrue(settings.blockedPackages.isEmpty())
    }

    @Test
    fun `setMapping persists and round-trips`() = runTest {
        repository.setMapping(GestureType.SHAKE, GlobalActionType.BACK)
        val settings = repository.settings.first { it.actionFor(GestureType.SHAKE) == GlobalActionType.BACK }
        assertEquals(GlobalActionType.BACK, settings.actionFor(GestureType.SHAKE))
    }

    @Test
    fun `setEnabled persists and round-trips`() = runTest {
        repository.setEnabled(false)
        val settings = repository.settings.first { !it.enabled }
        assertEquals(false, settings.enabled)
    }

    @Test
    fun `setBlockedPackages persists and round-trips`() = runTest {
        repository.setBlockedPackages(setOf("com.supermoney.app"))
        val settings = repository.settings.first { it.blockedPackages.isNotEmpty() }
        assertTrue("com.supermoney.app" in settings.blockedPackages)
    }
}
