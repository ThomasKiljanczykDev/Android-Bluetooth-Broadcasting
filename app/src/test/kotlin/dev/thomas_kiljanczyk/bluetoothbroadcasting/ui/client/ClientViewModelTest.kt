package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.FakeBroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClientViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val client = FakeBroadcastClient()
    private val viewModel by lazy { ClientViewModel(client) }
    private val device = RemoteDevice("id", "Server")

    @Test
    fun `maps client state to ui status`() {
        assertEquals(ClientUiStatus.Disconnected, viewModel.state.status)

        client.state.value = ClientState.Connecting
        assertEquals(ClientUiStatus.Connecting, viewModel.state.status)

        client.state.value = ClientState.Connected(device)
        assertEquals(ClientUiStatus.Connected("Server"), viewModel.state.status)

        client.state.value = ClientState.Connected(RemoteDevice("id", null))
        assertEquals(ClientUiStatus.ConnectedUnknown, viewModel.state.status)

        client.state.value = ClientState.ConnectionFailed(device)
        assertEquals(ClientUiStatus.ConnectionFailed, viewModel.state.status)
    }

    @Test
    fun `shows latest received message`() = runTest(mainDispatcherRule.dispatcher) {
        val state = viewModel.state
        client.messages.emit("first")
        client.messages.emit("second")

        assertEquals("second", state.receivedText)
    }

    @Test
    fun `delegates connect and disconnect`() {
        viewModel.startClient(device)
        viewModel.stopClient()

        assertEquals(listOf(device), client.connectRequests)
        assertEquals(1, client.disconnectCount)
    }
}
