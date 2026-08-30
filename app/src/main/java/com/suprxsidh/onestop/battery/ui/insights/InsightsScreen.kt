package com.suprxsidh.onestop.battery.ui.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suprxsidh.onestop.battery.calc.InsightsEngine
import com.suprxsidh.onestop.battery.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class InsightsViewModel(db: AppDatabase) : ViewModel() {
    val sessions = db.chargeSessionDao().all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class InsightsViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InsightsViewModel(db) as T
    }
}

@Composable
fun InsightsScreen(db: AppDatabase) {
    val viewModel: InsightsViewModel = viewModel(factory = InsightsViewModelFactory(db))
    val sessions by viewModel.sessions.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        val warnings = InsightsEngine.highTempWarnings(sessions)
        val fastest = InsightsEngine.fastestChargeWindow(sessions)
        val best = InsightsEngine.bestSession(sessions)
        val worst = InsightsEngine.worstSession(sessions)

        Text("High-temp charges: ${warnings.size}")
        Text("Fastest 20-80% session: ${fastest?.durationS?.let { "${it / 60} min" } ?: "--"}")
        Text("Best session: ${best?.avgWatts ?: "--"} W avg")
        Text("Worst session: ${worst?.avgWatts ?: "--"} W avg")
    }
}
