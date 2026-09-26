package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme
import kotlinx.coroutines.launch

@Composable
fun ServerScreen(
    title: String,
    viewModel: ServerViewModel,
    onNavigateUp: () -> Unit,
    maxMessageBytes: Int? = null,
    hint: String? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val connectedUnknownText = stringResource(R.string.activity_server_connected_unknown)
    val connectedText = stringResource(R.string.activity_server_connected)
    val disconnectedUnknownText = stringResource(R.string.activity_server_disconnected_unknown)
    val disconnectedText = stringResource(R.string.activity_server_disconnected)

    LaunchedEffect(Unit) {
        viewModel.messageFlow.collect { message ->
            val text = when (message) {
                is ServerUiMessage.ConnectedUnknown -> connectedUnknownText
                is ServerUiMessage.Connected -> connectedText.format(message.deviceName)
                is ServerUiMessage.DisconnectedUnknown -> disconnectedUnknownText
                is ServerUiMessage.Disconnected -> disconnectedText.format(message.deviceName)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    ServerScreen(
        title = title,
        state = viewModel.state,
        maxMessageBytes = maxMessageBytes,
        hint = hint,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onStartServer = viewModel::startServer,
        onStopServer = viewModel::stopServer,
        onBroadcastMessage = viewModel::broadcastMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    title: String,
    state: ServerUiState,
    maxMessageBytes: Int? = null,
    hint: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateUp: () -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    onBroadcastMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    val fits = fitsByteLimit(messageText, maxMessageBytes)

    val messageSentText = stringResource(R.string.activity_server_message_sent)
    val messageNotSentText = stringResource(R.string.activity_server_message_not_sent)

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
                text = if (state.isServerOn)
                    stringResource(R.string.activity_server_server_on)
                else
                    stringResource(R.string.activity_server_server_off)
            )

            hint?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    enabled = !state.isServerOn && !state.isStartingServer,
                    onClick = onStartServer
                ) {
                    Text(stringResource(R.string.activity_server_start_server))
                }

                Button(
                    enabled = state.isServerOn,
                    onClick = onStopServer
                ) {
                    Text(stringResource(R.string.activity_server_stop_server))
                }
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text(stringResource(R.string.activity_server_enter_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !fits,
                supportingText = maxMessageBytes?.let { max ->
                    {
                        Text(
                            stringResource(
                                R.string.activity_server_message_bytes,
                                messageText.encodeToByteArray().size,
                                max
                            )
                        )
                    }
                }
            )

            Button(
                enabled = fits,
                onClick = {
                    onBroadcastMessage(messageText)
                    val text = if (state.isServerOn) messageSentText else messageNotSentText
                    scope.launch { snackbarHostState.showSnackbar(text) }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.activity_server_send_message))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ServerScreenOffPreview() {
    BluetoothBroadcastingTheme {
        ServerScreen(
            title = "Bluetooth server",
            state = MutableServerUiState(),
            onNavigateUp = {},
            onStartServer = {},
            onStopServer = {},
            onBroadcastMessage = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ServerScreenStartingPreview() {
    BluetoothBroadcastingTheme {
        ServerScreen(
            title = "Bluetooth server",
            state = MutableServerUiState().apply { isStartingServer = true },
            onNavigateUp = {},
            onStartServer = {},
            onStopServer = {},
            onBroadcastMessage = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ServerScreenLimitedPreview() {
    BluetoothBroadcastingTheme {
        ServerScreen(
            title = "BLE advertising server",
            state = MutableServerUiState().apply { isServerOn = true },
            maxMessageBytes = 150,
            hint = "Clients receive without connecting.",
            onNavigateUp = {},
            onStartServer = {},
            onStopServer = {},
            onBroadcastMessage = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ServerScreenOnPreview() {
    BluetoothBroadcastingTheme {
        ServerScreen(
            title = "Bluetooth server",
            state = MutableServerUiState().apply { isServerOn = true },
            onNavigateUp = {},
            onStartServer = {},
            onStopServer = {},
            onBroadcastMessage = {}
        )
    }
}
