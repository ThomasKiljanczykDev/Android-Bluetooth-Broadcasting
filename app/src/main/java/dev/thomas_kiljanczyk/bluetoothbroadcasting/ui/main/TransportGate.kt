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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Availability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements

/**
 * Runs actions only once [requirements] permissions are granted and radios enabled.
 * Each disabled radio is prompted once per action; a declined prompt drops the action.
 * `unavailableReason`: string resource, null if available.
 */
@Composable
fun TransportGate(
    requirements: TransportRequirements,
    content: @Composable (unavailableReason: Int?, runGated: (action: () -> Unit) -> Unit) -> Unit
) {
    val context = LocalContext.current

    var unavailableReason by remember { mutableStateOf<Int?>(null) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showPermissionsRequestDialog by remember { mutableStateOf(false) }
    var showPermissionsDeniedDialog by remember { mutableStateOf(false) }
    val promptedRadios = remember { mutableSetOf<Radio>() }
    var radioCheckRequest by remember { mutableIntStateOf(0) }

    LifecycleStartEffect(requirements) {
        unavailableReason = (requirements.checkAvailability() as? Availability.Unavailable)?.reason
        onStopOrDispose {}
    }

    val radioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { radioCheckRequest++ }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            radioCheckRequest++
        } else {
            pendingAction = null
            showPermissionsDeniedDialog = true
        }
    }

    LaunchedEffect(radioCheckRequest) {
        val action = pendingAction ?: return@LaunchedEffect
        val disabled = requirements.requiredRadios.filterNot { it.isEnabled(context) }
        if (disabled.isEmpty()) {
            pendingAction = null
            action()
            return@LaunchedEffect
        }
        val radio = disabled.firstOrNull(promptedRadios::add)
        if (radio == null) {
            pendingAction = null
            return@LaunchedEffect
        }
        try {
            radioLauncher.launch(radio.enableIntent())
        } catch (e: ActivityNotFoundException) {
            Log.w("TransportGate", "No activity to enable $radio", e)
            radioCheckRequest++
        }
    }

    if (showPermissionsRequestDialog) {
        PermissionsRequestDialog(
            onProceed = {
                showPermissionsRequestDialog = false
                permissionLauncher.launch(requirements.runtimePermissions)
            },
            onCancel = {
                showPermissionsRequestDialog = false
                pendingAction = null
            }
        )
    }

    if (showPermissionsDeniedDialog) {
        PermissionsDeniedDialog(
            onGoToSettings = {
                showPermissionsDeniedDialog = false
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            },
            onCancel = { showPermissionsDeniedDialog = false }
        )
    }

    content(unavailableReason) { action ->
        pendingAction = action
        promptedRadios.clear()
        if (requirements.areAllPermissionsGranted(context)) {
            radioCheckRequest++
        } else {
            showPermissionsRequestDialog = true
        }
    }
}

private fun TransportRequirements.areAllPermissionsGranted(context: Context): Boolean =
    runtimePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
