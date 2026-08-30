package com.suprxsidh.onestop.battery.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.data.HealthEstimate
import com.suprxsidh.onestop.battery.data.Reading
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BatteryHomeViewModel(db: AppDatabase) : ViewModel() {

    val latestReading: StateFlow<Reading?> = db.readingDao().latest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val latestHealth: StateFlow<HealthEstimate?> = db.healthEstimateDao().latest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

class BatteryHomeViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BatteryHomeViewModel(db) as T
    }
}
