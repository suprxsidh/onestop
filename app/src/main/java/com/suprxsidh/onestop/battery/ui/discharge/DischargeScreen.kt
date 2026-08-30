package com.suprxsidh.onestop.battery.ui.discharge

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
import com.suprxsidh.onestop.battery.calc.DischargeCalculator
import com.suprxsidh.onestop.battery.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class DischargeViewModel(db: AppDatabase) : ViewModel() {
    val readings = db.readingDao().all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class DischargeViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DischargeViewModel(db) as T
    }
}

@Composable
fun DischargeScreen(db: AppDatabase) {
    val viewModel: DischargeViewModel = viewModel(factory = DischargeViewModelFactory(db))
    val readings by viewModel.readings.collectAsState()
    val discharging = readings.filter { it.currentUa < 0 }

    Column(modifier = Modifier.padding(16.dp)) {
        if (discharging.size < 2) {
            Text("Not enough discharge data yet")
        } else {
            val (onAvg, offAvg) = DischargeCalculator.screenOnVsOffDrain(discharging)
            Text("Screen on drain: %.0f mA".format(onAvg))
            Text("Screen off drain: %.0f mA".format(offAvg))
        }
    }
}
