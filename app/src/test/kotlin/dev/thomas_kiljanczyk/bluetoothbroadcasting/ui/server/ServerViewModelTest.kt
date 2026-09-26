package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.server

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.FakeBroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerEvent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.MainDispatcherRule
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val server = FakeBroadcastServer()
    private val viewModel by lazy { ServerViewModel(server) }

    @Test
    fun `maps server state to ui state`() {
        assertFalse(viewModel.state.isServerOn)
        assertFalse(viewModel.state.isStartingServer)

        server.state.value = ServerState.Starting
        assertFalse(viewModel.state.isServerOn)
        assertTrue(viewModel.state.isStartingServer)

        server.state.value = ServerState.Running
        assertTrue(viewModel.state.isServerOn)
        assertFalse(viewModel.state.isStartingServer)

        server.state.value = ServerState.Stopped
        assertFalse(viewModel.state.isServerOn)
    }

    @Test
    fun `maps server events to ui messages`() = runTest(mainDispatcherRule.dispatcher) {
        val messages = mutableListOf<ServerUiMessage>()
        val job = launch { viewModel.messageFlow.take(4).toList(messages) }

        server.events.emit(ServerEvent.ClientConnected(RemoteDevice("a", "Phone")))
        server.events.emit(ServerEvent.ClientConnected(RemoteDevice("b", null)))
        server.events.emit(ServerEvent.ClientDisconnected(RemoteDevice("a", "Phone")))
        server.events.emit(ServerEvent.ClientDisconnected(RemoteDevice("b", null)))
        job.join()

        assertEquals(
            listOf(
                ServerUiMessage.Connected("Phone"),
                ServerUiMessage.ConnectedUnknown,
                ServerUiMessage.Disconnected("Phone"),
                ServerUiMessage.DisconnectedUnknown
            ),
            messages
        )
    }

    @Test
    fun `delegates commands to server`() {
        viewModel.startServer()
        viewModel.broadcastMessage("hello")
        viewModel.stopServer()

        assertEquals(1, server.startCount)
        assertEquals(listOf("hello"), server.broadcasts)
        assertEquals(1, server.stopCount)
    }

    @Test
    fun `exposes transport limits`() {
        assertNull(viewModel.state.maxMessageBytes)
        assertNull(viewModel.state.hint)
        assertTrue(viewModel.state.fits("x".repeat(10_000)))

        val limited = ServerViewModel(FakeBroadcastServer(maxMessageBytes = 5, hint = 1))
        assertEquals(5, limited.state.maxMessageBytes)
        assertEquals(1, limited.state.hint)
    }

    @Test
    fun `drops messages over byte limit`() {
        val limitedServer = FakeBroadcastServer(maxMessageBytes = 5)
        val limited = ServerViewModel(limitedServer)

        assertTrue(limited.state.fits("héll"))
        assertFalse(limited.state.fits("héllo"))

        limited.broadcastMessage("héll")
        limited.broadcastMessage("héllo")

        assertEquals(listOf("héll"), limitedServer.broadcasts)
    }
}
