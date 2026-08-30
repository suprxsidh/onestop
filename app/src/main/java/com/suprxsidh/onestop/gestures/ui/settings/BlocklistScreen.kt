package com.suprxsidh.onestop.gestures.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository

@Composable
fun BlocklistScreen(repository: GestureSettingsRepository) {
    val context = LocalContext.current
    val viewModel: BlocklistViewModel = viewModel(
        factory = BlocklistViewModelFactory(
            repository,
            PackageManagerInstalledAppsProvider(context.applicationContext)
        )
    )
    val entries by viewModel.entries.collectAsState()

    LazyColumn {
        items(entries, key = { it.app.packageName }) { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.app.label)
                Switch(
                    checked = entry.suppressed,
                    onCheckedChange = { viewModel.setSuppressed(entry.app.packageName, it) }
                )
            }
        }
    }
}
