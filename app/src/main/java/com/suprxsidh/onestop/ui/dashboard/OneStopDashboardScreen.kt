package com.suprxsidh.onestop.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileState

@Composable
fun OneStopDashboardScreen(viewModel: OneStopDashboardViewModel, onOpenBattery: () -> Unit) {
    val batteryTile by viewModel.batteryTile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BatteryHeroTile(state = batteryTile, onClick = onOpenBattery)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlaceholderTile(label = "Gestures", modifier = Modifier.weight(1f))
            PlaceholderTile(label = "System", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BatteryHeroTile(state: BatteryTileState, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp, 20.dp, 28.dp, 20.dp),
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Battery")
            Text(state.percent?.let { "$it%" } ?: "--")
            Text(if (state.isCharging) "Charging" else "Discharging")
            Text(state.minutesRemaining?.let { "${it / 60}h ${it % 60}m left" } ?: "-- left")
            Text(state.peakWatts?.let { "Peak %.1f W today".format(it) } ?: "")
        }
    }
}

@Composable
private fun PlaceholderTile(label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp, 28.dp, 20.dp, 28.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label)
            Text("Coming soon")
        }
    }
}
