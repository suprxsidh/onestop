package com.suprxsidh.onestop.gestures.ui.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = Application::class)
@RunWith(RobolectricTestRunner::class)
class BlocklistViewModelTest {

    private lateinit var repository: GestureSettingsRepository
    private val fakeApps = listOf(
        InstalledApp("com.supermoney.app", "Super.Money"),
        InstalledApp("com.other.app", "Other App")
    )

    // The underlying DataStore file is a fixed singleton path -- Robolectric does not
    // guarantee a fresh files directory per test method, so state can leak between
    // tests (here and in any other test using GestureSettingsRepository) unless cleared.
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = GestureSettingsRepository(ApplicationProvider.getApplicationContext())
        repository.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entries start unsuppressed`() = runTest {
        val viewModel = BlocklistViewModel(repository) { fakeApps }
        val entries = viewModel.entries.first { it.isNotEmpty() }
        assertEquals(2, entries.size)
        assertTrue(entries.none { it.suppressed })
    }

    @Test
    fun `setSuppressed true adds the package to the blocklist`() = runTest {
        val viewModel = BlocklistViewModel(repository) { fakeApps }
        // Subscribe first so the ViewModel's cached settings reflect live repository state
        // before setSuppressed reads it -- matches real usage, where the screen's
        // collectAsState() is already active before any toggle is touched.
        viewModel.entries.first { it.isNotEmpty() }
        viewModel.setSuppressed("com.supermoney.app", true)
        val entries = viewModel.entries.first { list -> list.any { it.suppressed } }
        assertTrue(entries.first { it.app.packageName == "com.supermoney.app" }.suppressed)
    }

    @Test
    fun `setSuppressed false removes the package from the blocklist`() = runTest {
        repository.setBlockedPackages(setOf("com.supermoney.app"))
        val viewModel = BlocklistViewModel(repository) { fakeApps }
        viewModel.entries.first { it.isNotEmpty() }
        viewModel.setSuppressed("com.supermoney.app", false)
        val entries = viewModel.entries.first { list -> list.isNotEmpty() && list.none { it.suppressed } }
        assertTrue(entries.none { it.suppressed })
    }
}
