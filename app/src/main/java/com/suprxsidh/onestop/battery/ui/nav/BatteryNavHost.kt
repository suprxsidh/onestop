package com.suprxsidh.onestop.battery.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suprxsidh.onestop.battery.data.AppDatabase
import com.suprxsidh.onestop.battery.ui.charging.ChargingScreen
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeScreen
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeViewModel
import com.suprxsidh.onestop.battery.ui.home.BatteryHomeViewModelFactory
import com.suprxsidh.onestop.battery.ui.discharge.DischargeScreen
import com.suprxsidh.onestop.battery.ui.history.HistoryScreen
import com.suprxsidh.onestop.battery.ui.insights.InsightsScreen

private data class Destination(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("battery_home", "Now", Icons.Filled.BatteryFull),
    Destination("charging", "Charging", Icons.Filled.Bolt),
    Destination("discharge", "Discharge", Icons.Filled.TrendingDown),
    Destination("history", "History", Icons.Filled.History),
    Destination("insights", "Insights", Icons.Filled.Lightbulb)
)

@Composable
fun BatteryNavHost(db: AppDatabase) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = backStackEntry?.destination?.route == destination.route,
                        onClick = { navController.navigate(destination.route) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "battery_home",
            modifier = Modifier.padding(padding)
        ) {
            composable("battery_home") {
                val viewModel: BatteryHomeViewModel = viewModel(factory = BatteryHomeViewModelFactory(db))
                BatteryHomeScreen(viewModel)
            }
            composable("charging") { ChargingScreen(db) }
            composable("discharge") { DischargeScreen(db) }
            composable("history") { HistoryScreen(db) }
            composable("insights") { InsightsScreen(db) }
        }
    }
}
