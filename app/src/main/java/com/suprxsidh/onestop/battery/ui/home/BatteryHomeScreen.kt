package com.suprxsidh.onestop.battery.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BatteryHomeScreen(viewModel: BatteryHomeViewModel) {
    val reading by viewModel.latestReading.collectAsState()
    val health by viewModel.latestHealth.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Battery: ${reading?.pct ?: "--"}%")
        Text("Temp: ${reading?.tempC ?: "--"}°C")
        Text("Voltage: ${reading?.voltageMv ?: "--"} mV")
        Text("Power: ${reading?.watts ?: "--"} W")
        Text("Health: ${health?.healthPct?.let { "%.1f".format(it) } ?: "--"}%")
    }
}
