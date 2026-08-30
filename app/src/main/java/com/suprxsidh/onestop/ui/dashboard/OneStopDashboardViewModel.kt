package com.suprxsidh.onestop.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileMapper
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val SPARKLINE_SAMPLE_COUNT = 40

class OneStopDashboardViewModel(db: AppDatabase) : ViewModel() {

    val batteryTile: StateFlow<BatteryTileState> = db.readingDao()
        .recent(SPARKLINE_SAMPLE_COUNT)
        .map { BatteryTileMapper.toBatteryTileState(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BatteryTileMapper.toBatteryTileState(emptyList())
        )
}

class OneStopDashboardViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OneStopDashboardViewModel(db) as T
    }
}
