package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import javax.inject.Inject

internal val SERVICE_UUID: ParcelUuid = ParcelUuid(TransportConstants.SERVICE_UUID)

internal const val LIVENESS_TIMEOUT_MS = 10_000L

class BleAdapterProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val adapterOrNull: BluetoothAdapter?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter
}

/** Settings value needs no BLUETOOTH_CONNECT, unlike BluetoothAdapter.getName(). */
class LocalDeviceName @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val value: String
        get() = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            ?: Build.MODEL
}

internal fun CoroutineScope.onCancellation(block: () -> Unit) {
    launch {
        try {
            awaitCancellation()
        } finally {
            block()
        }
    }
}
