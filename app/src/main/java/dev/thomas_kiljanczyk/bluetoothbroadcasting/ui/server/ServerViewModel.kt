package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerEvent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

interface ServerUiState {
    val isServerOn: Boolean
    val isStartingServer: Boolean
}

internal class MutableServerUiState : ServerUiState {
    override var isServerOn by mutableStateOf(false)
    override var isStartingServer by mutableStateOf(false)
}

/** Subclassed per transport with a concrete [BroadcastServer]. */
open class ServerViewModel(
    private val server: BroadcastServer
) : ViewModel() {
    private val _state = MutableServerUiState()
    val state: ServerUiState get() = _state

    val messageFlow: Flow<ServerUiMessage> = server.events.map { it.toUiMessage() }

    init {
        server.state.onEach {
            _state.isServerOn = it == ServerState.Running
            _state.isStartingServer = it == ServerState.Starting
        }.launchIn(viewModelScope)
    }

    fun startServer() = server.start()

    fun stopServer() = server.stop()

    fun broadcastMessage(message: String) = server.broadcast(message)
}

fun fitsByteLimit(message: String, maxBytes: Int?): Boolean =
    maxBytes == null || message.encodeToByteArray().size <= maxBytes

private fun ServerEvent.toUiMessage(): ServerUiMessage {
    val name = device.name
    return when (this) {
        is ServerEvent.ClientConnected ->
            if (name != null) ServerUiMessage.Connected(name) else ServerUiMessage.ConnectedUnknown

        is ServerEvent.ClientDisconnected ->
            if (name != null) ServerUiMessage.Disconnected(name) else ServerUiMessage.DisconnectedUnknown
    }
}
