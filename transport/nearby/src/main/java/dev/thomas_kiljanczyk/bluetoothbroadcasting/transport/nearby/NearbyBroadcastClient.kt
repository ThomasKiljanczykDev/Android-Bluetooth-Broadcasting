package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import dagger.hilt.android.scopes.ViewModelScoped
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ViewModelScoped
class NearbyBroadcastClient @Inject constructor(
    private val connectionsClient: ConnectionsClient,
    private val localEndpointName: LocalEndpointName,
    @TransportScope scope: CoroutineScope
) : BroadcastClient {
    private companion object {
        const val TAG = "NearbyBroadcastClient"
    }

    private val _state = MutableStateFlow<ClientState>(ClientState.Disconnected)
    override val state: StateFlow<ClientState> = _state

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val messages: Flow<String> = _messages

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { _messages.tryEmit(it.decodeToString()) }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private var endpointId: String? = null

    init {
        scope.onCancellation(::disconnect)
    }

    override fun connect(device: RemoteDevice) {
        disconnect()
        _state.value = ClientState.Connecting

        val callback = object : ConnectionLifecycleCallback() {
            private var remote = device

            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                remote = RemoteDevice(endpointId, info.endpointName)
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                _state.value = if (result.status.isSuccess) {
                    ClientState.Connected(remote)
                } else {
                    ClientState.ConnectionFailed(remote)
                }
            }

            override fun onDisconnected(endpointId: String) {
                _state.value = ClientState.Disconnected
            }
        }

        endpointId = device.id
        connectionsClient.requestConnection(localEndpointName.value, device.id, callback)
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to request connection", e)
                _state.value = ClientState.ConnectionFailed(device)
            }
    }

    /** Endpoint only: stopAllEndpoints() resets the shared client, dropping discovered endpoints (8009). */
    override fun disconnect() {
        endpointId?.let(connectionsClient::disconnectFromEndpoint)
        endpointId = null
        _state.value = ClientState.Disconnected
    }
}
