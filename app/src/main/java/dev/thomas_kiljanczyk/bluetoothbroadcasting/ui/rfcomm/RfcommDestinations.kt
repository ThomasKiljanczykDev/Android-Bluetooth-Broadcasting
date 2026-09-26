package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.rfcomm

import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm.RfcommTransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.TransportEntry
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerScreen
import kotlinx.serialization.Serializable

@Serializable
object RfcommServerRoute

@Serializable
object RfcommClientRoute

fun rfcommTransport(requirements: RfcommTransportRequirements, navController: NavController) = TransportEntry(
    title = R.string.rfcomm_card_title,
    subtitle = R.string.rfcomm_card_subtitle,
    requirements = requirements,
    onClient = { navController.navigate(RfcommClientRoute) },
    onServer = { navController.navigate(RfcommServerRoute) }
)

fun NavGraphBuilder.rfcommDestinations(navController: NavController) {
    composable<RfcommServerRoute> {
        ServerScreen(
            title = stringResource(R.string.rfcomm_server_title),
            viewModel = hiltViewModel<RfcommServerViewModel>(),
            onNavigateUp = { navController.navigateUp() }
        )
    }
    composable<RfcommClientRoute> {
        ClientScreen(
            title = stringResource(R.string.rfcomm_client_title),
            viewModel = hiltViewModel<RfcommClientViewModel>(),
            pickDeviceViewModel = { hiltViewModel<RfcommPickDeviceViewModel>() },
            onNavigateUp = { navController.navigateUp() }
        )
    }
}
