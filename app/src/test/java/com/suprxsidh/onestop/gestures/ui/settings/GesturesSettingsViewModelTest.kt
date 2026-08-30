package com.suprxsidh.onestop.gestures.ui.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = Application::class)
@RunWith(RobolectricTestRunner::class)
class GesturesSettingsViewModelTest {

    private lateinit var repository: GestureSettingsRepository
    private lateinit var viewModel: GesturesSettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = GestureSettingsRepository(ApplicationProvider.getApplicationContext())
        viewModel = GesturesSettingsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setMapping updates settings state`() = runTest {
        viewModel.setMapping(GestureType.SHAKE, GlobalActionType.BACK)
        val settings = viewModel.settings.first { it.actionFor(GestureType.SHAKE) == GlobalActionType.BACK }
        assertEquals(GlobalActionType.BACK, settings.actionFor(GestureType.SHAKE))
    }

    @Test
    fun `setEnabled updates settings state`() = runTest {
        viewModel.setEnabled(false)
        val settings = viewModel.settings.first { !it.enabled }
        assertEquals(false, settings.enabled)
    }
}
