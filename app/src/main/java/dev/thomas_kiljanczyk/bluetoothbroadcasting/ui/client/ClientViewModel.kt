package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.NearbyConnectionLifecycleCallback
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.SimpleNearbyPayloadCallback
import javax.inject.Inject

interface ClientUiState {
    val status: ClientUiStatus
    val receivedText: String
}

internal class MutableClientUiState : ClientUiState {
    override var status by mutableStateOf<ClientUiStatus>(ClientUiStatus.Disconnected)
    override var receivedText by mutableStateOf("")
}

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        private const val TAG = "ClientViewModel"
    }

    private val _state = MutableClientUiState()
    val state: ClientUiState get() = _state

    private inner class ClientConnectionLifecycleCallback : NearbyConnectionLifecycleCallback() {
        override fun onConnectionInitiated(
            endpointId: String, connectionInfo: ConnectionInfo
        ) {
            super.onConnectionInitiated(endpointId, connectionInfo)
            connectionsClient.acceptConnection(endpointId, SimpleNearbyPayloadCallback { payload ->
                _state.receivedText = payload?.decodeToString() ?: ""
            })
        }

        override fun onConnectionResult(
            endpointId: String, connectionInfo: ConnectionInfo?, result: ConnectionResolution
        ) {
            if (result.status.isSuccess) {
                val endpointName = connectionInfo?.endpointName
                _state.status = if (endpointName != null) {
                    ClientUiStatus.Connected(endpointName)
                } else {
                    ClientUiStatus.ConnectedUnknown
                }
            } else {
                _state.status = ClientUiStatus.Disconnected
            }
        }

        override fun onDisconnected(endpointId: String, connectionInfo: ConnectionInfo?) {
            connectionsClient.disconnectFromEndpoint(endpointId)
            _state.status = ClientUiStatus.Disconnected
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH])
    fun startClient(endpointId: String, deviceName: String) {
        connectionsClient.requestConnection(
            deviceName, endpointId, ClientConnectionLifecycleCallback()
        )
    }

    fun stopClient() {
        connectionsClient.stopAllEndpoints()
        Log.i(TAG, "Client disconnected")
    }
}