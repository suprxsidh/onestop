package com.suprxsidh.onestop.battery.ui.charging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprxsidh.onestop.battery.calc.ChargerType
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.common.LineChart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChargingViewModel(db: AppDatabase) : ViewModel() {
    val sessions = db.chargeSessionDao().all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class ChargingViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChargingViewModel(db) as T
    }
}

@Composable
fun ChargingScreen(db: AppDatabase) {
    val viewModel: ChargingViewModel = viewModel(factory = ChargingViewModelFactory(db))
    val sessions by viewModel.sessions.collectAsState()
    val latest = sessions.firstOrNull()

    Column(modifier = Modifier.padding(16.dp)) {
        if (latest == null) {
            Text("No charge sessions yet")
        } else {
            // Read the persisted classification (computed from peak watts in ChargeSessionService)
            // rather than recomputing from avgWatts here, which would disagree with the stored
            // value and reintroduce the trickle-charge dilution that method has.
            val chargerLabel = latest.chargerType
                ?.let { runCatching { ChargerType.valueOf(it) }.getOrNull() }
                ?.label
                ?: ChargerType.UNKNOWN.label
            Text("Charger: $chargerLabel")
            Text("Peak: ${latest.peakWatts ?: "--"} W")
            Text("Added: ${latest.mahAdded ?: "--"} mAh")
            LineChart(values = sessions.take(20).mapNotNull { it.avgWatts })
        }
    }
}
