package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerEvent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/** Permissions granted by the TransportRequirements gate before any call. */
@SuppressLint("MissingPermission")
@ViewModelScoped
class RfcommBroadcastServer @Inject constructor(
    private val adapterProvider: RfcommAdapterProvider,
    @TransportScope private val scope: CoroutineScope
) : BroadcastServer {
    private companion object {
        const val TAG = "RfcommBroadcastServer"
    }

    private class Connection(val socket: BluetoothSocket) {
        val writeLock = Mutex()
    }

    private val _state = MutableStateFlow(ServerState.Stopped)
    override val state: StateFlow<ServerState> = _state

    private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 16)
    override val events: Flow<ServerEvent> = _events

    private val connections = ConcurrentHashMap<String, Connection>()

    private var serverJob: Job? = null

    override fun start() {
        if (!_state.compareAndSet(ServerState.Stopped, ServerState.Starting)) return

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                adapterProvider.adapter
                    .listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                    .useCancellable { serverSocket ->
                        _state.value = ServerState.Running
                        while (true) {
                            val socket = serverSocket.accept()
                            launch { serve(Connection(socket)) }
                        }
                    }
            } catch (e: IOException) {
                Log.w(TAG, "Server socket closed", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Missing Bluetooth permission", e)
            } finally {
                coroutineContext.cancelChildren()
                _state.value = ServerState.Stopped
            }
        }
    }

    override fun stop() {
        serverJob?.cancel()
        serverJob = null
    }

    override fun broadcast(message: String) {
        val frame = MessageFramer.encode(message)
        connections.values.forEach { connection ->
            scope.launch(Dispatchers.IO) {
                try {
                    connection.writeLock.withLock {
                        connection.socket.outputStream.run {
                            write(frame)
                            flush()
                        }
                    }
                } catch (e: IOException) {
                    Log.w(TAG, "Write failed; closing connection", e)
                    connection.socket.close()
                }
            }
        }
    }

    private suspend fun serve(connection: Connection) {
        val device = connection.socket.remoteDevice.toRemoteDevice()
        connections.put(device.id, connection)?.socket?.close()
        _events.tryEmit(ServerEvent.ClientConnected(device))

        try {
            connection.socket.useCancellable { socket ->
                val buffer = ByteArray(64)
                // Inbound traffic is heartbeats only; EOF or IOException means the client is gone.
                while (socket.inputStream.read(buffer) != -1) Unit
            }
        } catch (e: IOException) {
            Log.i(TAG, "Client ${device.id} disconnected", e)
        } finally {
            connections.remove(device.id, connection)
            if (currentCoroutineContext().isActive) {
                _events.tryEmit(ServerEvent.ClientDisconnected(device))
            }
        }
    }
}
