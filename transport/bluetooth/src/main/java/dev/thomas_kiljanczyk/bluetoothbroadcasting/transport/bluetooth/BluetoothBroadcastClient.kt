package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.util.Log
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/** Permissions granted by the TransportRequirements gate before any call. */
@SuppressLint("MissingPermission")
class BluetoothBroadcastClient @Inject constructor(
    private val adapterProvider: BluetoothAdapterProvider,
    @TransportScope private val scope: CoroutineScope
) : BroadcastClient {
    private companion object {
        const val TAG = "BluetoothBroadcastClient"
    }

    private val _state = MutableStateFlow<ClientState>(ClientState.Disconnected)
    override val state: StateFlow<ClientState> = _state

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val messages: Flow<String> = _messages

    private var connectionJob: Job? = null

    override fun connect(device: RemoteDevice) {
        connectionJob?.cancel()
        _state.value = ClientState.Connecting

        connectionJob = scope.launch(Dispatchers.IO) {
            var connected = false
            try {
                adapterProvider.adapter
                    .getRemoteDevice(device.id)
                    .createRfcommSocketToServiceRecord(SERVICE_UUID)
                    .useCancellable { socket ->
                        socket.connect()
                        connected = true
                        _state.value = ClientState.Connected(device)
                        coroutineScope {
                            val heartbeat = launch { sendHeartbeats(socket) }
                            try {
                                receive(socket)
                            } finally {
                                heartbeat.cancel()
                            }
                        }
                    }
                ensureActive()
                _state.value = ClientState.Disconnected
            } catch (e: IOException) {
                ensureActive()
                Log.w(TAG, "Connection to ${device.id} lost", e)
                _state.value = if (connected) {
                    ClientState.Disconnected
                } else {
                    ClientState.ConnectionFailed(device)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Missing Bluetooth permission", e)
                _state.value = ClientState.ConnectionFailed(device)
            }
        }
    }

    override fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _state.value = ClientState.Disconnected
    }

    private suspend fun receive(socket: BluetoothSocket) {
        val framer = MessageFramer()
        val buffer = ByteArray(1024)
        while (true) {
            val read = socket.inputStream.read(buffer)
            if (read == -1) return
            framer.decode(buffer, read).forEach { _messages.emit(it) }
        }
    }

    /** BluetoothSocket.isConnected does not reflect remote loss; a failed write does. */
    private suspend fun sendHeartbeats(socket: BluetoothSocket) {
        while (true) {
            delay(HEARTBEAT_INTERVAL_MS)
            socket.outputStream.write(HEARTBEAT)
        }
    }
}
