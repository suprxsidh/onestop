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
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardScreen

@Composable
fun OneStopNavHost(db: AppDatabase) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val batteryTileViewModel: BatteryTileViewModel =
                viewModel(factory = BatteryTileViewModelFactory(db))
            val batteryTile by batteryTileViewModel.state.collectAsState()

            OneStopDashboardScreen(
                batteryTile = batteryTile,
                onOpenBattery = { navController.navigate("battery") }
            )
        }
        composable("battery") {
            BatteryNavHost(db)
        }
    }
}
