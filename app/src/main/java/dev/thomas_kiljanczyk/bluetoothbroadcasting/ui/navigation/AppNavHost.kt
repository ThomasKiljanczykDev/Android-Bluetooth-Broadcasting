package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.ble.bleDestinations
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.ble.bleTransport
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.MainScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.MainViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.nearby.nearbyDestinations
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.nearby.nearbyTransport
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.rfcomm.rfcommDestinations
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.rfcomm.rfcommTransport

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = MainRoute) {
        composable<MainRoute> {
            val viewModel = hiltViewModel<MainViewModel>()
            MainScreen(
                transports = listOf(
                    bleTransport(viewModel.ble, navController),
                    rfcommTransport(viewModel.rfcomm, navController),
                    nearbyTransport(viewModel.nearby, navController)
                )
            )
        }
        bleDestinations(navController)
        rfcommDestinations(navController)
        nearbyDestinations(navController)
    }
}
