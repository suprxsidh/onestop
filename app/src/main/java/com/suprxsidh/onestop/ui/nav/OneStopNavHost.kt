package com.suprxsidh.onestop.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.nav.BatteryNavHost
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileViewModel
import com.suprxsidh.onestop.battery.ui.tile.BatteryTileViewModelFactory
import com.suprxsidh.onestop.gestures.data.GestureSettingsRepository
import com.suprxsidh.onestop.gestures.ui.settings.BlocklistScreen
import com.suprxsidh.onestop.gestures.ui.settings.GesturesSettingsScreen
import com.suprxsidh.onestop.gestures.ui.tile.GesturesTileViewModel
import com.suprxsidh.onestop.gestures.ui.tile.GesturesTileViewModelFactory
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardScreen

@Composable
fun OneStopNavHost(db: AppDatabase, gestureSettingsRepository: GestureSettingsRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val batteryTileViewModel: BatteryTileViewModel =
                viewModel(factory = BatteryTileViewModelFactory(db))
            val gesturesTileViewModel: GesturesTileViewModel =
                viewModel(factory = GesturesTileViewModelFactory(gestureSettingsRepository))
            val batteryTile by batteryTileViewModel.state.collectAsState()
            val gesturesTile by gesturesTileViewModel.state.collectAsState()

            OneStopDashboardScreen(
                batteryTile = batteryTile,
                gesturesTile = gesturesTile,
                onOpenBattery = { navController.navigate("battery") },
                onOpenGestures = { navController.navigate("gestures") }
            )
        }
        composable("battery") {
            BatteryNavHost(db)
        }
        composable("gestures") {
            GesturesSettingsScreen(
                repository = gestureSettingsRepository,
                onOpenBlocklist = { navController.navigate("gestures/blocklist") }
            )
        }
        composable("gestures/blocklist") {
            BlocklistScreen(repository = gestureSettingsRepository)
        }
    }
}
