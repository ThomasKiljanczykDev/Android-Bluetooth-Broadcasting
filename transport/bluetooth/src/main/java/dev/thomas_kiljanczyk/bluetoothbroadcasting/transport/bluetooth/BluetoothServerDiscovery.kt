package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Bonded devices only. SDP falls back to cached UUIDs for unreachable devices,
 * so results are candidates; connect() is the reachability test.
 */
@SuppressLint("MissingPermission")
class BluetoothServerDiscovery @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val adapterProvider: BluetoothAdapterProvider
) : ServerDiscovery {
    private companion object {
        const val REFRESH_INTERVAL_MS = 10_000L

        val MATCHING_UUIDS: Set<UUID> = setOf(SERVICE_UUID, SERVICE_UUID.reversed())
    }

    override val emptyHint: Int = R.string.transport_bluetooth_discovery_hint

    override fun discover(): Flow<List<RemoteDevice>> = callbackFlow {
        val matches = ConcurrentHashMap<String, RemoteDevice>()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = IntentCompat.getParcelableExtra(
                    intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                ) ?: return
                // Null EXTRA_UUID means SDP timed out.
                val uuids = IntentCompat.getParcelableArrayExtra(
                    intent, BluetoothDevice.EXTRA_UUID, ParcelUuid::class.java
                ).orEmpty().map { (it as ParcelUuid).uuid }

                if (uuids.any(MATCHING_UUIDS::contains)) {
                    matches[device.address] = device.toRemoteDevice()
                } else {
                    matches.remove(device.address)
                }
                trySend(matches.values.toList())
            }
        }

        // ACTION_UUID comes from the Bluetooth app, not the system UID; NOT_EXPORTED receivers miss it.
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(BluetoothDevice.ACTION_UUID),
            ContextCompat.RECEIVER_EXPORTED
        )

        launch {
            while (true) {
                adapterProvider.adapter.bondedDevices.orEmpty().forEach { it.fetchUuidsWithSdp() }
                delay(REFRESH_INTERVAL_MS)
            }
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }
}
