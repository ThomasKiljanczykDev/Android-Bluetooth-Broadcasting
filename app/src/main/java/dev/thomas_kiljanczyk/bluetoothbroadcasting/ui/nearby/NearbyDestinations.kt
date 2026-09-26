package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.nearby

import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby.NearbyTransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.ClientScreen
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main.TransportEntry
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerScreen
import kotlinx.serialization.Serializable

@Serializable
object NearbyServerRoute

@Serializable
object NearbyClientRoute

fun nearbyTransport(requirements: NearbyTransportRequirements, navController: NavController) = TransportEntry(
    title = R.string.nearby_card_title,
    subtitle = R.string.nearby_card_subtitle,
    requirements = requirements,
    onClient = { navController.navigate(NearbyClientRoute) },
    onServer = { navController.navigate(NearbyServerRoute) }
)

fun NavGraphBuilder.nearbyDestinations(navController: NavController) {
    composable<NearbyServerRoute> {
        ServerScreen(
            title = stringResource(R.string.nearby_server_title),
            viewModel = hiltViewModel<NearbyServerViewModel>(),
            onNavigateUp = { navController.navigateUp() }
        )
    }
    composable<NearbyClientRoute> {
        ClientScreen(
            title = stringResource(R.string.nearby_client_title),
            viewModel = hiltViewModel<NearbyClientViewModel>(),
            pickDeviceViewModel = { hiltViewModel<NearbyPickDeviceViewModel>() },
            onNavigateUp = { navController.navigateUp() }
        )
    }
}
