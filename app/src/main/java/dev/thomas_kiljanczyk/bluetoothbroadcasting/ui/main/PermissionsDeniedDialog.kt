package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.AppAlertDialog
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@Composable
fun PermissionsDeniedDialog(
    onGoToSettings: () -> Unit,
    onCancel: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onCancel,
        text = { Text(stringResource(R.string.dialog_fragment_permissions_denied_message)) },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.dialog_fragment_permissions_denied_go_to_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.dialog_fragment_permission_cancel))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun PermissionsDeniedDialogPreview() {
    BluetoothBroadcastingTheme {
        PermissionsDeniedDialog(
            onGoToSettings = {},
            onCancel = {}
        )
    }
}
