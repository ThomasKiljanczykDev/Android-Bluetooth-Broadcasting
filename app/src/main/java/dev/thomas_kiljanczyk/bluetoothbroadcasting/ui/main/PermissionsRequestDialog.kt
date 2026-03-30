package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R

@Composable
fun PermissionsRequestDialog(
    onProceed: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        text = { Text(stringResource(R.string.dialog_fragment_permissions_request_message)) },
        confirmButton = {
            TextButton(onClick = onProceed) {
                Text(stringResource(R.string.dialog_fragment_permissions_request_proceed))
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text(stringResource(R.string.dialog_fragment_permission_close_app))
            }
        }
    )
}
