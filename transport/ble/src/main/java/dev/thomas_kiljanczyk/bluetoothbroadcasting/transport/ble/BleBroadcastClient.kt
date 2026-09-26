package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
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

/** Connectionless: "connected" while the server's adverts are received. */
class BleBroadcastClient @Inject constructor(
    private val scanner: BleScanner,
    @TransportScope private val scope: CoroutineScope
) : BroadcastClient {
    private val _state = MutableStateFlow<ClientState>(ClientState.Disconnected)
    override val state: StateFlow<ClientState> = _state

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val messages: Flow<String> = _messages

    private var sessionJob: Job? = null

    override fun connect(device: RemoteDevice) {
        sessionJob?.cancel()
        val sessionId = parseSessionKey(device.id)
        if (sessionId == null) {
            _state.value = ClientState.ConnectionFailed(device)
            return
        }
        _state.value = ClientState.Connecting

        sessionJob = scope.launch {
            trackSession(
                device = device,
                sessionId = sessionId,
                adverts = scanner.adverts(),
                timeoutMs = LIVENESS_TIMEOUT_MS,
                onState = { _state.value = it },
                onMessage = _messages::emit
            )
        }
    }

    override fun disconnect() {
        sessionJob?.cancel()
        sessionJob = null
        _state.value = ClientState.Disconnected
    }
}
