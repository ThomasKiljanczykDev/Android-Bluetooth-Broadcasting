package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.ble

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.BleBroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.BleListener
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.HeardServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble.ListenerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server.ServerViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BleServerViewModel @Inject constructor(
    server: BleBroadcastServer
) : ServerViewModel(server) {
    val maxMessageBytes: Int = server.maxMessageBytes
}

@HiltViewModel
class BleClientViewModel @Inject constructor(
    private val listener: BleListener
) : ViewModel() {
    val state: StateFlow<ListenerState> = listener.state
    val servers: StateFlow<List<HeardServer>> = listener.servers

    fun startListening() = listener.start()

    fun stopListening() = listener.stop()
}
