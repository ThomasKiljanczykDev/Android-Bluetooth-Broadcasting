package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ReceivedMessage
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Connectionless: receives adverts from every server in range. */
class BleBroadcastClient @Inject constructor(
    private val scanner: BleScanner,
    @TransportScope private val scope: CoroutineScope
) : BroadcastClient {
    private val _state = MutableStateFlow<ClientState>(ClientState.Disconnected)
    override val state: StateFlow<ClientState> = _state

    private val _messages = MutableSharedFlow<ReceivedMessage>(extraBufferCapacity = 16)
    override val messages: Flow<ReceivedMessage> = _messages

    override val requiresServerPick: Boolean = false

    private var listenJob: Job? = null

    override fun connect(device: RemoteDevice) = listen()

    override fun listen() {
        listenJob?.cancel()
        _state.value = ClientState.Listening(emptyList())

        listenJob = scope.launch {
            scanner.adverts()
                .listen(LIVENESS_TIMEOUT_MS, _messages::emit)
                .collect { _state.value = ClientState.Listening(it) }
            _state.value = ClientState.Disconnected
        }
    }

    override fun disconnect() {
        listenJob?.cancel()
        listenJob = null
        _state.value = ClientState.Disconnected
    }
}
