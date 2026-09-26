package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.ble

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.HeardServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.ListenerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@Composable
fun BleClientScreen(
    title: String,
    viewModel: BleClientViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()

    BleClientScreen(
        title = title,
        state = state,
        servers = servers,
        onNavigateUp = onNavigateUp,
        onStartListening = viewModel::startListening,
        onStopListening = viewModel::stopListening
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleClientScreen(
    title: String,
    state: ListenerState,
    servers: List<HeardServer>,
    onNavigateUp: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val listening = state == ListenerState.Listening

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (listening && servers.isEmpty()) {
                Text(
                    text = stringResource(R.string.ble_client_scanning),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(servers, key = { it.device.id }) { HeardServerCard(it) }
                }
            }

            Text(
                text = if (listening) {
                    pluralStringResource(
                        R.plurals.ble_client_listening_servers,
                        servers.size,
                        servers.size
                    )
                } else {
                    stringResource(R.string.ble_client_not_listening)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(enabled = !listening, onClick = onStartListening) {
                    Text(stringResource(R.string.ble_client_listen))
                }
                OutlinedButton(enabled = listening, onClick = onStopListening) {
                    Text(stringResource(R.string.ble_client_stop_listening))
                }
            }
        }
    }
}

@Composable
private fun HeardServerCard(server: HeardServer) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = server.device.name ?: stringResource(R.string.placeholder_server_device),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = server.message ?: stringResource(R.string.ble_client_no_message),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BleClientScreenStoppedPreview() {
    BluetoothBroadcastingTheme {
        BleClientScreen(
            title = "BLE advertising client",
            state = ListenerState.Stopped,
            servers = emptyList(),
            onNavigateUp = {},
            onStartListening = {},
            onStopListening = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun BleClientScreenListeningPreview() {
    BluetoothBroadcastingTheme {
        BleClientScreen(
            title = "BLE advertising client",
            state = ListenerState.Listening,
            servers = listOf(
                HeardServer(RemoteDevice("0000002a", "Pixel 9"), "Hello from server!"),
                HeardServer(RemoteDevice("0000002b", null), null)
            ),
            onNavigateUp = {},
            onStartListening = {},
            onStopListening = {}
        )
    }
}
