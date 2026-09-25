package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

object TransportConstants {
    val SERVICE_UUID: UUID = UUID.fromString("2f58e6c0-5ccf-4d2f-afec-65a2d98e2141")
}

/** [id] is transport-specific: Nearby endpoint id or Bluetooth MAC address. */
data class RemoteDevice(val id: String, val name: String?)

enum class ServerState { Stopped, Starting, Running }

sealed interface ServerEvent {
    val device: RemoteDevice

    data class ClientConnected(override val device: RemoteDevice) : ServerEvent
    data class ClientDisconnected(override val device: RemoteDevice) : ServerEvent
}

sealed interface ClientState {
    data object Disconnected : ClientState
    data object Connecting : ClientState
    data class Connected(val device: RemoteDevice) : ClientState
    data class ConnectionFailed(val device: RemoteDevice) : ClientState
}

interface BroadcastServer {
    val state: StateFlow<ServerState>
    val events: Flow<ServerEvent>

    fun start()
    fun stop()
    fun broadcast(message: String)
}

interface BroadcastClient {
    val state: StateFlow<ClientState>
    val messages: Flow<String>

    fun connect(device: RemoteDevice)
    fun disconnect()
}

interface ServerDiscovery {
    /** Shown while no server has been found. */
    @get:StringRes
    val emptyHint: Int

    /** Cold: discovery runs while collected. */
    fun discover(): Flow<List<RemoteDevice>>
}
