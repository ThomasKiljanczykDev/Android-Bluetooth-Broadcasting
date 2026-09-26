package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.ble

import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.BleTransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.TransportEntry
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerScreen
import kotlinx.serialization.Serializable

@Serializable
object BleServerRoute

@Serializable
object BleClientRoute

fun bleTransport(requirements: BleTransportRequirements, navController: NavController) = TransportEntry(
    title = R.string.ble_card_title,
    subtitle = R.string.ble_card_subtitle,
    requirements = requirements,
    onClient = { navController.navigate(BleClientRoute) },
    onServer = { navController.navigate(BleServerRoute) }
)

fun NavGraphBuilder.bleDestinations(navController: NavController) {
    composable<BleServerRoute> {
        val viewModel = hiltViewModel<BleServerViewModel>()
        ServerScreen(
            title = stringResource(R.string.ble_server_title),
            viewModel = viewModel,
            onNavigateUp = { navController.navigateUp() },
            maxMessageBytes = viewModel.maxMessageBytes,
            hint = stringResource(R.string.ble_server_hint)
        )
    }
    composable<BleClientRoute> {
        BleClientScreen(
            title = stringResource(R.string.ble_client_title),
            viewModel = hiltViewModel<BleClientViewModel>(),
            onNavigateUp = { navController.navigateUp() }
        )
    }
}
