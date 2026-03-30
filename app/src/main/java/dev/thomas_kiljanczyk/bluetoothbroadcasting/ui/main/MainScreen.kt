package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.application.NearbyPermissions
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToClient: () -> Unit,
    onNavigateToServer: () -> Unit
) {
    val context = LocalContext.current

    var showPermissionsRequestDialog by remember { mutableStateOf(false) }
    var showPermissionsDeniedDialog by remember { mutableStateOf(false) }
    var showNoPlayServicesDialog by remember { mutableStateOf(false) }

    fun checkPermissions() {
        if (!NearbyPermissions.areAllPermissionsGranted(context)) {
            showPermissionsRequestDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { !it }) {
            showPermissionsDeniedDialog = true
        }
    }

    LifecycleStartEffect(Unit) {
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS
        ) {
            showNoPlayServicesDialog = true
        } else {
            checkPermissions()
        }
        onStopOrDispose {}
    }

    if (showNoPlayServicesDialog) {
        AlertDialog(
            onDismissRequest = {},
            text = { Text(stringResource(R.string.dialog_fragment_no_play_services_message)) },
            confirmButton = {
                TextButton(onClick = { exitProcess(0) }) {
                    Text(stringResource(R.string.dialog_fragment_no_play_services_exit_app))
                }
            }
        )
    }

    if (showPermissionsRequestDialog) {
        PermissionsRequestDialog(
            onProceed = {
                showPermissionsRequestDialog = false
                permissionLauncher.launch(NearbyPermissions.REQUIRED_PERMISSIONS)
            },
            onExit = { exitProcess(0) }
        )
    }

    if (showPermissionsDeniedDialog) {
        PermissionsDeniedDialog(
            onGoToSettings = {
                showPermissionsDeniedDialog = false
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            },
            onExit = { exitProcess(0) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onNavigateToClient,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(stringResource(R.string.activity_main_btn_client))
            }

            Button(onClick = onNavigateToServer) {
                Text(stringResource(R.string.activity_main_btn_server))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    BluetoothBroadcastingTheme {
        MainScreen(
            onNavigateToClient = {},
            onNavigateToServer = {}
        )
    }
}
