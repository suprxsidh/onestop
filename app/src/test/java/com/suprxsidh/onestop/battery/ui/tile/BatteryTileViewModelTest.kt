package com.suprxsidh.onestop.battery.ui.tile

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.Reading
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
class BatteryTileViewModelTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects the most recent reading`() = runTest {
        db.readingDao().insert(
            Reading(
                ts = System.currentTimeMillis(), pct = 71, tempC = 29f, voltageMv = 3850,
                currentUa = -400_000, watts = -1.5f, status = 2, plugType = 0, screenOn = true
            )
        )
        val viewModel = BatteryTileViewModel(db)
        val state = viewModel.state.first { it.percent != null }
        assertEquals(71, state.percent)
    }
}
