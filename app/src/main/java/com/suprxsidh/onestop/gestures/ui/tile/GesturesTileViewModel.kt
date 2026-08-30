package com.suprxsidh.onestop.gestures.ui.tile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.model.GestureSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GesturesTileViewModel(repository: GestureSettingsRepository) : ViewModel() {
    val state: StateFlow<GesturesTileState> =
        repository.settings
            .map { GesturesTileMapper.toGesturesTileState(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                GesturesTileMapper.toGesturesTileState(GestureSettings())
            )
}

class GesturesTileViewModelFactory(
    private val repository: GestureSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GesturesTileViewModel(repository) as T
    }
}
