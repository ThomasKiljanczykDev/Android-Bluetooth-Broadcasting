package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pick_gms_server_device

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.shared.Constants
import javax.inject.Inject

interface PickDeviceDialogUiState {
    val discoveredDevices: List<GmsNearbyServerDeviceItem>
}

internal class MutablePickDeviceDialogUiState : PickDeviceDialogUiState {
    override var discoveredDevices by mutableStateOf<List<GmsNearbyServerDeviceItem>>(emptyList())
}

@HiltViewModel
class PickDeviceDialogViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
) : ViewModel() {
    companion object {
        const val TAG: String = "PickDeviceDialogViewModel"
    }

    private val _state = MutablePickDeviceDialogUiState()
    val state: PickDeviceDialogUiState get() = _state

    private val deviceMap = mutableMapOf<String, GmsNearbyServerDeviceItem>()

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            Constants.SERVICE_UUID.toString(),
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    deviceMap[endpointId] = GmsNearbyServerDeviceItem(info.endpointName, endpointId)
                    _state.discoveredDevices = deviceMap.values.toList()
                }

                override fun onEndpointLost(endpointId: String) {
                    deviceMap.remove(endpointId)
                    _state.discoveredDevices = deviceMap.values.toList()
                }
            },
            discoveryOptions
        ).addOnFailureListener { e ->
            Log.e(TAG, "Failed to start discovering", e)
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        deviceMap.clear()
        _state.discoveredDevices = emptyList()
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        stopDiscovery()
        super.onCleared()
    }
}
