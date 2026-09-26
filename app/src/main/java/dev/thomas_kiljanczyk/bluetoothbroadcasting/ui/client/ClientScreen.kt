package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver.PickDeviceDialog
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@Composable
fun ClientScreen(
    onNavigateUp: () -> Unit,
    viewModel: ClientViewModel
) {
    ClientScreen(
        state = viewModel.state,
        onNavigateUp = onNavigateUp,
        onDevicePicked = viewModel::startClient,
        onDisconnect = viewModel::stopClient
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(
    state: ClientUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateUp: () -> Unit,
    onDevicePicked: (RemoteDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    var showPickDeviceDialog by remember { mutableStateOf(false) }

    val statusText = when (val status = state.status) {
        is ClientUiStatus.Disconnected -> stringResource(R.string.activity_client_disconnected)
        is ClientUiStatus.Connecting -> stringResource(R.string.activity_client_connecting)
        is ClientUiStatus.ConnectionFailed -> stringResource(R.string.activity_client_connection_failed)
        is ClientUiStatus.ConnectedUnknown -> stringResource(R.string.activity_client_connected_unknown)
        is ClientUiStatus.Connected -> stringResource(
            R.string.activity_client_connected,
            status.deviceName
        )
    }

    if (showPickDeviceDialog) {
        PickDeviceDialog(
            onDevicePicked = { device ->
                onDevicePicked(device)
                showPickDeviceDialog = false
            },
            onDismiss = { showPickDeviceDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_client_activity)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.receivedText.ifBlank { stringResource(R.string.activity_client_no_content) },
                modifier = Modifier.weight(1f)
            )

            Text(text = statusText)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = { showPickDeviceDialog = true }) {
                    Text(stringResource(R.string.activity_client_connect_to_server))
                }

                OutlinedButton(onClick = onDisconnect) {
                    Text(stringResource(R.string.activity_client_disconnect))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ClientScreenDisconnectedPreview() {
    BluetoothBroadcastingTheme {
        ClientScreen(
            state = MutableClientUiState(),
            onNavigateUp = {},
            onDevicePicked = {},
            onDisconnect = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ClientScreenConnectedPreview() {
    BluetoothBroadcastingTheme {
        ClientScreen(
            state = MutableClientUiState().apply {
                status = ClientUiStatus.Connected("My Server")
                receivedText = "Hello from server!"
            },
            onNavigateUp = {},
            onDevicePicked = {},
            onDisconnect = {}
        )
    }
}
