package com.suprxsidh.onestop.gestures.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.model.GestureType
import com.suprxsidh.onestop.gestures.model.GlobalActionType

@Composable
fun GesturesSettingsScreen(
    repository: GestureSettingsRepository,
    onOpenBlocklist: () -> Unit
) {
    val viewModel: GesturesSettingsViewModel =
        viewModel(factory = GesturesSettingsViewModelFactory(repository))
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Gestures enabled")
            Switch(checked = settings.enabled, onCheckedChange = { viewModel.setEnabled(it) })
        }
        GestureMappingRow(
            label = "Shake",
            selected = settings.actionFor(GestureType.SHAKE),
            onSelect = { viewModel.setMapping(GestureType.SHAKE, it) }
        )
        GestureMappingRow(
            label = "Rotate",
            selected = settings.actionFor(GestureType.ROTATE),
            onSelect = { viewModel.setMapping(GestureType.ROTATE, it) }
        )
        Button(onClick = onOpenBlocklist) {
            Text("Suppressed apps (${settings.blockedPackages.size})")
        }
    }
}

@Composable
private fun GestureMappingRow(
    label: String,
    selected: GlobalActionType,
    onSelect: (GlobalActionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        TextButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GlobalActionType.values().forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.name) },
                    onClick = {
                        onSelect(action)
                        expanded = false
                    }
                )
            }
        }
    }
}
