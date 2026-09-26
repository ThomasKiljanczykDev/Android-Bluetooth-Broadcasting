package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class FakeBroadcastServer(
    override val maxMessageBytes: Int? = null,
    override val hint: Int? = null
) : BroadcastServer {
    override val state = MutableStateFlow(ServerState.Stopped)
    override val events = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 16)

    val broadcasts = mutableListOf<String>()
    var startCount = 0
    var stopCount = 0

    override fun start() {
        startCount++
    }

    override fun stop() {
        stopCount++
    }

    override fun broadcast(message: String) {
        broadcasts += message
    }
}

class FakeBroadcastClient : BroadcastClient {
    override val state = MutableStateFlow<ClientState>(ClientState.Disconnected)
    override val messages = MutableSharedFlow<String>(extraBufferCapacity = 16)

    val connectRequests = mutableListOf<RemoteDevice>()
    var disconnectCount = 0

    override fun connect(device: RemoteDevice) {
        connectRequests += device
    }

    override fun disconnect() {
        disconnectCount++
    }
}

class FakeServerDiscovery(override val emptyHint: Int = 0) : ServerDiscovery {
    val devices = MutableStateFlow<List<RemoteDevice>>(emptyList())
    val activeCollectors = MutableStateFlow(0)

    override fun discover(): Flow<List<RemoteDevice>> = devices
        .onStart { activeCollectors.update { it + 1 } }
        .onCompletion { activeCollectors.update { it - 1 } }
}
