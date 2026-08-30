package com.suprxsidh.onestop.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.nav.BatteryNavHost
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardScreen
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardViewModel
import com.suprxsidh.onestop.ui.dashboard.OneStopDashboardViewModelFactory

@Composable
fun OneStopNavHost(db: AppDatabase) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val viewModel: OneStopDashboardViewModel = viewModel(factory = OneStopDashboardViewModelFactory(db))
            OneStopDashboardScreen(
                viewModel = viewModel,
                onOpenBattery = { navController.navigate("battery") }
            )
        }
        composable("battery") {
            BatteryNavHost(db)
        }
    }
}
