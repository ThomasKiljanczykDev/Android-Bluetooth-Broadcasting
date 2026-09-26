package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.client

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.FakeBroadcastClient
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ReceivedMessage
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

        client.state.value = ClientState.Listening(listOf(device, RemoteDevice("id2", null)))
        assertEquals(ClientUiStatus.Listening(2), viewModel.state.status)
    }

    @Test
    fun `shows latest received message`() = runTest(mainDispatcherRule.dispatcher) {
        val state = viewModel.state
        client.messages.emit(ReceivedMessage(device, "first"))
        client.messages.emit(ReceivedMessage(device, "second"))

        assertEquals("second", state.receivedText)
        assertTrue(state.requiresServerPick)
        assertNull(state.sender)
    }

    @Test
    fun `shows sender without server pick`() = runTest(mainDispatcherRule.dispatcher) {
        val fake = FakeBroadcastClient(requiresServerPick = false)
        val viewModel = ClientViewModel(fake)
        val other = RemoteDevice("id2", "Other")
        assertFalse(viewModel.state.requiresServerPick)

        fake.messages.emit(ReceivedMessage(device, "first"))
        fake.messages.emit(ReceivedMessage(other, "second"))

        assertEquals("second", viewModel.state.receivedText)
        assertEquals(other, viewModel.state.sender)
    }

    @Test
    fun `delegates connect, listen and disconnect`() {
        viewModel.startClient(device)
        viewModel.listen()
        viewModel.stopClient()

        assertEquals(listOf(device), client.connectRequests)
        assertEquals(1, client.listenCount)
        assertEquals(1, client.disconnectCount)
    }
}
