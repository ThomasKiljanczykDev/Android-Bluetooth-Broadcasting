package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.application.NearbyPermissions
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme
import kotlinx.coroutines.launch

@Composable
fun ServerScreen(
    onNavigateUp: () -> Unit,
    viewModel: ServerViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val noBluetoothText = stringResource(R.string.toast_no_bluetooth)
    val connectedUnknownText = stringResource(R.string.activity_server_connected_unknown)
    val connectedText = stringResource(R.string.activity_server_connected)
    val disconnectedUnknownText = stringResource(R.string.activity_server_disconnected_unknown)
    val disconnectedText = stringResource(R.string.activity_server_disconnected)

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
            Log.d("ServerScreen", "User refused REQUEST_ENABLE_BT")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    DisposableEffect(Unit) {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager?.adapter
        if (bluetoothAdapter == null) {
            scope.launch { snackbarHostState.showSnackbar(noBluetoothText) }
        } else if (!bluetoothAdapter.isEnabled) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        onDispose { viewModel.stopServer() }
    }

    LaunchedEffect(Unit) {
        viewModel.messageFlow.collect { message ->
            val text = when (message) {
                is ServerUiMessage.None -> return@collect
                is ServerUiMessage.ConnectedUnknown -> connectedUnknownText
                is ServerUiMessage.Connected -> connectedText.format(message.deviceName)
                is ServerUiMessage.DisconnectedUnknown -> disconnectedUnknownText
                is ServerUiMessage.Disconnected -> disconnectedText.format(message.deviceName)
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    ServerScreen(
        state = viewModel.state,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onStartServer = { deviceName ->
            try {
                viewModel.startServer(deviceName)
            } catch (ex: SecurityException) {
                Log.e("ServerScreen", "Failed to start server", ex)
                permissionLauncher.launch(NearbyPermissions.REQUIRED_PERMISSIONS)
            }
        },
        onStopServer = viewModel::stopServer,
        onBroadcastMessage = viewModel::broadcastMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    state: ServerUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateUp: () -> Unit,
    onStartServer: (deviceName: String) -> Unit,
    onStopServer: () -> Unit,
    onBroadcastMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }

    val messageSentText = stringResource(R.string.activity_server_message_sent)
    val messageNotSentText = stringResource(R.string.activity_server_message_not_sent)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_server_activity)) },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    enabled = !state.isServerOn && !state.isStartingServer,
                    onClick = {
                        val deviceName = Settings.Global.getString(
                            context.contentResolver, Settings.Global.DEVICE_NAME
                        )
                        onStartServer(deviceName)
                    }
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
                singleLine = true
            )

            Button(
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
private fun ServerScreenOnPreview() {
    BluetoothBroadcastingTheme {
        ServerScreen(
            state = MutableServerUiState().apply { isServerOn = true },
            onNavigateUp = {},
            onStartServer = {},
            onStopServer = {},
            onBroadcastMessage = {}
        )
    }
}
