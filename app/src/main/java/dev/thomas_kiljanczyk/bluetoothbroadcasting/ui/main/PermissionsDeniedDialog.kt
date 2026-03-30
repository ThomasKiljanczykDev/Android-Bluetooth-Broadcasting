package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R

@Composable
fun PermissionsDeniedDialog(
    onGoToSettings: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        text = { Text(stringResource(R.string.dialog_fragment_permissions_denied_message)) },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.dialog_fragment_permissions_denied_go_to_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text(stringResource(R.string.dialog_fragment_permission_close_app))
            }
        }
    )
}
