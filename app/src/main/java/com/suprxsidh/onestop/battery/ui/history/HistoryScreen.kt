package com.suprxsidh.onestop.battery.ui.history

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
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.common.LineChart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(db: AppDatabase) : ViewModel() {
    val readings = db.readingDao().all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class HistoryViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(db) as T
    }
}

@Composable
fun HistoryScreen(db: AppDatabase) {
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(db))
    val readings by viewModel.readings.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Temperature")
        LineChart(values = readings.takeLast(200).map { it.tempC })
        Text("Charge %")
        LineChart(values = readings.takeLast(200).map { it.pct.toFloat() })
        Text("Watts")
        LineChart(values = readings.takeLast(200).map { it.watts })
    }
}
