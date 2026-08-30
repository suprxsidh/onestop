package com.suprxsidh.onestop.gestures.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.model.GestureSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BlocklistEntry(val app: InstalledApp, val suppressed: Boolean)

class BlocklistViewModel(
    private val repository: GestureSettingsRepository,
    installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    private val installedApps = installedAppsProvider.listInstalledApps()

    private val settings: StateFlow<GestureSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GestureSettings())

    val entries: StateFlow<List<BlocklistEntry>> =
        settings
            .map { s -> installedApps.map { app -> BlocklistEntry(app, app.packageName in s.blockedPackages) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSuppressed(packageName: String, suppressed: Boolean) {
        val current = settings.value.blockedPackages
        val updated = if (suppressed) current + packageName else current - packageName
        viewModelScope.launch { repository.setBlockedPackages(updated) }
    }
}

class BlocklistViewModelFactory(
    private val repository: GestureSettingsRepository,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BlocklistViewModel(repository, installedAppsProvider) as T
    }
}
