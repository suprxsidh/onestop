package com.suprxsidh.onestop.gestures.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.model.GestureSettings
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GesturesSettingsViewModel(
    private val repository: GestureSettingsRepository
) : ViewModel() {

    val settings: StateFlow<GestureSettings> =
        repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GestureSettings()
        )

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(enabled) }
    }

    fun setMapping(gestureType: GestureType, action: GlobalActionType) {
        viewModelScope.launch { repository.setMapping(gestureType, action) }
    }
}

class GesturesSettingsViewModelFactory(
    private val repository: GestureSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GesturesSettingsViewModel(repository) as T
    }
}
