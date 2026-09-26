package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.AppAlertDialog
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@Composable
fun PickDeviceDialog(
    onDevicePicked: (RemoteDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: PickDeviceDialogViewModel = hiltViewModel(key = "PickDeviceDialog")
    val devices by viewModel.discoveredDevices.collectAsStateWithLifecycle()

    PickDeviceDialog(
        devices = devices,
        emptyHint = viewModel.emptyHint,
        onDevicePicked = onDevicePicked,
        onDismiss = onDismiss
    )
}

@Composable
fun PickDeviceDialog(
    devices: List<RemoteDevice>,
    @StringRes emptyHint: Int,
    onDevicePicked: (RemoteDevice) -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_fragment_pick_device_title)) },
        text = {
            if (devices.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(emptyHint), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(devices, key = { it.id }) { device ->
                        Text(
                            text = device.name
                                ?: stringResource(R.string.placeholder_server_device),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDevicePicked(device) }
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
            devices = emptyList(),
            emptyHint = R.string.dialog_fragment_pick_device_title,
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
            devices = listOf(
                RemoteDevice("endpoint-1", "Living Room TV"),
                RemoteDevice("endpoint-2", "Bedroom Speaker")
            ),
            emptyHint = R.string.dialog_fragment_pick_device_title,
            onDevicePicked = {},
            onDismiss = {}
        )
    }
}
