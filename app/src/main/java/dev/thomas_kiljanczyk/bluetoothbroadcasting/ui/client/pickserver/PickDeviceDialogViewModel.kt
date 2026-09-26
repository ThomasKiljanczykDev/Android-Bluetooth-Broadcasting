package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client.pickserver

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Subclassed per transport with a concrete [ServerDiscovery]. */
open class PickDeviceDialogViewModel(
    discovery: ServerDiscovery
) : ViewModel() {
    @StringRes
    val emptyHint: Int = discovery.emptyHint

    val discoveredDevices: StateFlow<List<RemoteDevice>> = discovery.discover().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0),
        emptyList()
    )
}
