package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.Constants
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.NearbyConnectionLifecycleCallback
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.SimpleNearbyPayloadCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

interface ServerUiState {
    val isServerOn: Boolean
    val isStartingServer: Boolean
}

internal class MutableServerUiState : ServerUiState {
    override var isServerOn by mutableStateOf(false)
    override var isStartingServer by mutableStateOf(false)
}

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        const val TAG = "ServerViewModel"
    }

    private val _state = MutableServerUiState()
    val state: ServerUiState get() = _state

    private val _messageFlow: MutableSharedFlow<ServerUiMessage> = MutableSharedFlow(replay = 1)
    val messageFlow: Flow<ServerUiMessage> = _messageFlow

    private val connectedEndpointIds = mutableSetOf<String>()

    private inner class ServerConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(endpointId, SimpleNearbyPayloadCallback {})
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                val endpointName = connectionInfo?.endpointName
                _messageFlow.tryEmit(
                    if (endpointName != null) ServerUiMessage.Connected(endpointName)
                    else ServerUiMessage.ConnectedUnknown
                )
                connectedEndpointIds.add(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            val endpointName = connectionInfo?.endpointName
            _messageFlow.tryEmit(
                if (endpointName != null) ServerUiMessage.Disconnected(endpointName)
                else ServerUiMessage.DisconnectedUnknown
            )
            connectedEndpointIds.remove(endpointId)
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startServer(deviceName: String) {
        _state.isStartingServer = true
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        connectionsClient.startAdvertising(
            deviceName,
            Constants.SERVICE_UUID.toString(),
            ServerConnectionLifecycleCallback(),
            advertisingOptions
        ).addOnSuccessListener { _: Void? ->
            _state.isStartingServer = false
            _state.isServerOn = true
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to start server", e)
            _state.isStartingServer = false
            _state.isServerOn = false
        }
    }

    fun stopServer() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
        _state.isServerOn = false
    }

    fun broadcastMessage(message: String) {
        Log.i(TAG, "Sending message : $message")
        connectionsClient.sendPayload(
            connectedEndpointIds.toList(), Payload.fromBytes(message.toByteArray())
        ).addOnSuccessListener {
            Log.i(TAG, "Message sent")
        }.addOnFailureListener { e: Exception? ->
            Log.e(TAG, "Failed to send message", e)
        }
    }
}