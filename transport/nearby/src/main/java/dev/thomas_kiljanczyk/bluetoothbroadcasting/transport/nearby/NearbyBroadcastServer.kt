package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import dagger.hilt.android.scopes.ViewModelScoped
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerEvent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ViewModelScoped
class NearbyBroadcastServer @Inject constructor(
    private val connectionsClient: ConnectionsClient,
    private val localEndpointName: LocalEndpointName,
    @TransportScope scope: CoroutineScope
) : BroadcastServer {
    private companion object {
        const val TAG = "NearbyBroadcastServer"
    }

    private val _state = MutableStateFlow(ServerState.Stopped)
    override val state: StateFlow<ServerState> = _state

    private val _events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 16)
    override val events: Flow<ServerEvent> = _events

    private val pendingEndpoints = ConcurrentHashMap<String, RemoteDevice>()
    private val connectedEndpoints = ConcurrentHashMap<String, RemoteDevice>()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            pendingEndpoints[endpointId] = RemoteDevice(endpointId, info.endpointName)
            connectionsClient.acceptConnection(endpointId, IgnoringPayloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val device = pendingEndpoints.remove(endpointId) ?: RemoteDevice(endpointId, null)
            if (result.status.isSuccess) {
                connectedEndpoints[endpointId] = device
                _events.tryEmit(ServerEvent.ClientConnected(device))
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)?.let {
                _events.tryEmit(ServerEvent.ClientDisconnected(it))
            }
        }
    }

    init {
        scope.onCancellation(::stop)
    }

    override fun start() {
        if (!_state.compareAndSet(ServerState.Stopped, ServerState.Starting)) return

        connectionsClient.startAdvertising(
            localEndpointName.value,
            SERVICE_ID,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        ).addOnSuccessListener {
            _state.compareAndSet(ServerState.Starting, ServerState.Running)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start advertising", e)
            _state.value = ServerState.Stopped
        }
    }

    override fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
        pendingEndpoints.clear()
        connectedEndpoints.clear()
        _state.value = ServerState.Stopped
    }

    override fun broadcast(message: String) {
        val endpointIds = connectedEndpoints.keys.toList()
        if (endpointIds.isEmpty()) return

        connectionsClient.sendPayload(endpointIds, Payload.fromBytes(message.encodeToByteArray()))
            .addOnFailureListener { e -> Log.e(TAG, "Failed to send message", e) }
    }
}
