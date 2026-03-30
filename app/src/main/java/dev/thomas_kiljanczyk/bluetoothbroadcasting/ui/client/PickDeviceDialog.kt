package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pick_gms_server_device.GmsNearbyServerDeviceItem
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pick_gms_server_device.MutablePickDeviceDialogUiState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pick_gms_server_device.PickDeviceDialogUiState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pick_gms_server_device.PickDeviceDialogViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@Composable
fun PickDeviceDialog(
    onDevicePicked: (endpointId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: PickDeviceDialogViewModel = hiltViewModel(key = "PickDeviceDialog")

    LaunchedEffect(Unit) { viewModel.startDiscovery() }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopDiscovery() }
    }

    PickDeviceDialog(
        state = viewModel.state,
        onDevicePicked = onDevicePicked,
        onDismiss = onDismiss
    )
}

@Composable
fun PickDeviceDialog(
    state: PickDeviceDialogUiState,
    onDevicePicked: (endpointId: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_fragment_pick_device_title)) },
        text = {
            if (state.discoveredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.discoveredDevices) { device ->
                        Text(
                            text = device.deviceName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDevicePicked(device.endpointId) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_fragment_close))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun PickDeviceDialogLoadingPreview() {
    BluetoothBroadcastingTheme {
        PickDeviceDialog(
            state = MutablePickDeviceDialogUiState(),
            onDevicePicked = {},
            onDismiss = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PickDeviceDialogWithDevicesPreview() {
    BluetoothBroadcastingTheme {
        PickDeviceDialog(
            state = MutablePickDeviceDialogUiState().apply {
                discoveredDevices = listOf(
                    GmsNearbyServerDeviceItem("Living Room TV", "endpoint-1"),
                    GmsNearbyServerDeviceItem("Bedroom Speaker", "endpoint-2")
                )
            },
            onDevicePicked = {},
            onDismiss = {}
        )
    }
}
