package com.suprxsidh.onestop.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprxsidh.onestop.battery.ui.common.LineChart
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileState
import com.suprxsidh.onestop.gestures.ui.tile.GesturesTileState

@Composable
fun OneStopDashboardScreen(
    batteryTile: BatteryTileState,
    gesturesTile: GesturesTileState,
    onOpenBattery: () -> Unit,
    onOpenGestures: () -> Unit
) {
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
            GesturesTile(state = gesturesTile, onClick = onOpenGestures, modifier = Modifier.weight(1f))
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
            if (state.sparklinePercents.size >= 2) {
                LineChart(
                    values = state.sparklinePercents.map { it.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun GesturesTile(state: GesturesTileState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp, 28.dp, 20.dp, 28.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Gestures")
            Text(if (state.enabled) "On" else "Off")
            Text(
                if (state.suppressedAppCount == 0) "No apps suppressed"
                else "${state.suppressedAppCount} app${if (state.suppressedAppCount == 1) "" else "s"} suppressed"
            )
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
