package com.suprxsidh.onestop.gestures.ui.tile

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
class GesturesTileViewModelTest {

    private lateinit var repository: GestureSettingsRepository

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
    fun `initial state reflects repository defaults`() = runTest {
        val viewModel = GesturesTileViewModel(repository)
        val state = viewModel.state.first()
        assertTrue(state.enabled)
        assertEquals(0, state.suppressedAppCount)
    }

    @Test
    fun `state reflects an updated blocklist size`() = runTest {
        repository.setBlockedPackages(setOf("com.supermoney.app", "com.other.app"))
        val viewModel = GesturesTileViewModel(repository)
        val state = viewModel.state.first { it.suppressedAppCount == 2 }
        assertEquals(2, state.suppressedAppCount)
    }
}
