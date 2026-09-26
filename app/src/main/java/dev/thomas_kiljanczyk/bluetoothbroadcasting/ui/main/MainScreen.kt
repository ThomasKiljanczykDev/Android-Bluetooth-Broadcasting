package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import dev.thomas_kiljanczyk.bluetoothbroadcasting.R
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Availability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.AppAlertDialog
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme
import kotlin.system.exitProcess

@Composable
fun MainScreen(
    requirements: TransportRequirements,
    onNavigateToClient: () -> Unit,
    onNavigateToServer: () -> Unit
) {
    val context = LocalContext.current

    var unavailableReason by remember { mutableStateOf<Int?>(null) }
    var showPermissionsRequestDialog by remember { mutableStateOf(false) }
    var showPermissionsDeniedDialog by remember { mutableStateOf(false) }

    // At most one prompt per radio per screen, so a decline does not loop.
    val promptedRadios = remember { mutableSetOf<Radio>() }
    var radioCheckRequest by remember { mutableIntStateOf(0) }

    val radioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { radioCheckRequest++ }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { !it }) {
            showPermissionsDeniedDialog = true
        } else {
            radioCheckRequest++
        }
    }

    LifecycleStartEffect(Unit) {
        when (val availability = requirements.checkAvailability()) {
            is Availability.Unavailable -> unavailableReason = availability.reason
            Availability.Available -> if (requirements.areAllPermissionsGranted(context)) {
                radioCheckRequest++
            } else {
                showPermissionsRequestDialog = true
            }
        }
        onStopOrDispose {}
    }

    LaunchedEffect(radioCheckRequest) {
        if (radioCheckRequest == 0) return@LaunchedEffect
        val radio = requirements.requiredRadios.firstOrNull {
            !it.isEnabled(context) && promptedRadios.add(it)
        } ?: return@LaunchedEffect
        try {
            radioLauncher.launch(radio.enableIntent())
        } catch (e: ActivityNotFoundException) {
            Log.w("MainScreen", "No activity to enable $radio", e)
            radioCheckRequest++
        }
    }

    unavailableReason?.let { reason ->
        AppAlertDialog(
            onDismissRequest = {},
            text = { Text(stringResource(reason)) },
            confirmButton = {
                TextButton(onClick = { exitProcess(0) }) {
                    Text(stringResource(R.string.dialog_unavailable_exit_app))
                }
            }
        )
    }

    if (showPermissionsRequestDialog) {
        PermissionsRequestDialog(
            onProceed = {
                showPermissionsRequestDialog = false
                permissionLauncher.launch(requirements.runtimePermissions)
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

    MainScreenContent(
        onNavigateToClient = onNavigateToClient,
        onNavigateToServer = onNavigateToServer
    )
}

private fun TransportRequirements.areAllPermissionsGranted(context: Context): Boolean =
    runtimePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    onNavigateToClient: () -> Unit,
    onNavigateToServer: () -> Unit
) {
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
        MainScreenContent(
            onNavigateToClient = {},
            onNavigateToServer = {}
        )
    }
}
