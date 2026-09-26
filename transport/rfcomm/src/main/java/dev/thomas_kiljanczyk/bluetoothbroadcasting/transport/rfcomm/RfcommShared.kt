package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject

internal const val SERVICE_NAME = "Broadcast Service"

internal val SERVICE_UUID: UUID = TransportConstants.SERVICE_UUID

internal const val HEARTBEAT: Int = 0x00

internal const val HEARTBEAT_INTERVAL_MS = 1_000L

class RfcommAdapterProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val adapterOrNull: BluetoothAdapter?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    val adapter: BluetoothAdapter
        get() = checkNotNull(adapterOrNull) { "Bluetooth unavailable" }
}

@SuppressLint("MissingPermission")
internal fun BluetoothDevice.toRemoteDevice() = RemoteDevice(address, name)

/** Byte-reversed form: SDP on API 23–27 reports 128-bit UUIDs reversed (issuetracker 37075233). */
internal fun UUID.reversed(): UUID {
    val bytes = ByteBuffer.allocate(16)
        .putLong(mostSignificantBits)
        .putLong(leastSignificantBits)
        .array()
        .reversedArray()
    val buffer = ByteBuffer.wrap(bytes)
    return UUID(buffer.long, buffer.long)
}

/** [use] that also closes on cancellation; blocking socket calls ignore cancellation and interrupts, aborting only on close(). */
internal suspend fun <C : Closeable, R> C.useCancellable(block: suspend (C) -> R): R {
    val closeable = this
    return coroutineScope {
        val closer = launch(Dispatchers.IO) {
            try {
                awaitCancellation()
            } finally {
                closeable.close()
            }
        }
        try {
            block(closeable)
        } finally {
            closer.cancel()
        }
    }
}
