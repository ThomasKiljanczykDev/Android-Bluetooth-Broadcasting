package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.MainScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.MainViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerViewModel

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = MainRoute) {
        composable<MainRoute> {
            MainScreen(
                requirements = hiltViewModel<MainViewModel>().requirements,
                onNavigateToClient = { navController.navigate(ClientRoute) },
                onNavigateToServer = { navController.navigate(ServerRoute) }
            )
        }
        composable<ClientRoute> {
            ClientScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel = hiltViewModel<ClientViewModel>(key = ClientRoute.toString())
            )
        }
        composable<ServerRoute> {
            ServerScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel = hiltViewModel<ServerViewModel>(key = ServerRoute.toString())
            )
        }
    }
}
