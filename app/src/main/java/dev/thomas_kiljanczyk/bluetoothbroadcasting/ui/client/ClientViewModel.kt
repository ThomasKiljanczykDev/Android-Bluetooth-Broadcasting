package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val client: BroadcastClient
) : ViewModel() {
    private val _state = MutableClientUiState()
    val state: ClientUiState get() = _state

    init {
        client.state.onEach { _state.status = it.toUiStatus() }.launchIn(viewModelScope)
        client.messages.onEach { _state.receivedText = it }.launchIn(viewModelScope)
    }

    fun startClient(device: RemoteDevice) = client.connect(device)

    fun stopClient() = client.disconnect()
}

private fun ClientState.toUiStatus(): ClientUiStatus = when (this) {
    ClientState.Disconnected -> ClientUiStatus.Disconnected
    ClientState.Connecting -> ClientUiStatus.Connecting
    is ClientState.ConnectionFailed -> ClientUiStatus.ConnectionFailed
    is ClientState.Connected -> device.name?.let(ClientUiStatus::Connected)
        ?: ClientUiStatus.ConnectedUnknown
}
