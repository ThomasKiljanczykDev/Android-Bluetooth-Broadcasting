package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

class TransportEntry(
    @param:StringRes val title: Int,
    @param:StringRes val subtitle: Int,
    val requirements: TransportRequirements,
    val onClient: () -> Unit,
    val onServer: () -> Unit
)

@Composable
fun TransportCard(entry: TransportEntry, modifier: Modifier = Modifier) {
    TransportGate(entry.requirements) { unavailableReason, runGated ->
        TransportCard(
            title = stringResource(entry.title),
            subtitle = stringResource(entry.subtitle),
            unavailableReason = unavailableReason?.let { stringResource(it) },
            onClient = { runGated(entry.onClient) },
            onServer = { runGated(entry.onServer) },
            modifier = modifier
        )
    }
}

@Composable
fun TransportCard(
    title: String,
    subtitle: String,
    unavailableReason: String?,
    onClient: () -> Unit,
    onServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            unavailableReason?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(enabled = unavailableReason == null, onClick = onClient) {
                    Text(stringResource(R.string.activity_main_btn_client))
                }
                Button(enabled = unavailableReason == null, onClick = onServer) {
                    Text(stringResource(R.string.activity_main_btn_server))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TransportCardPreview() {
    BluetoothBroadcastingTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TransportCard(
                title = "BLE advertising",
                subtitle = "No connection · any number of listeners",
                unavailableReason = null,
                onClient = {},
                onServer = {}
            )
            TransportCard(
                title = "Nearby",
                subtitle = "Google Nearby Connections",
                unavailableReason = "Google Play Services unavailable.",
                onClient = {},
                onServer = {}
            )
        }
    }
}
