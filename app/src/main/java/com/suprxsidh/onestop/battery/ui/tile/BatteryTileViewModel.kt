package com.suprxsidh.onestop.battery.ui.tile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.battery.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val TILE_WINDOW_MS = 24L * 60 * 60 * 1000

class BatteryTileViewModel(db: AppDatabase) : ViewModel() {
    val state: StateFlow<BatteryTileState> =
        db.readingDao()
            .since(System.currentTimeMillis() - TILE_WINDOW_MS)
            .map { BatteryTileMapper.toBatteryTileState(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                BatteryTileMapper.toBatteryTileState(emptyList())
            )
}

class BatteryTileViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BatteryTileViewModel(db) as T
    }
}
