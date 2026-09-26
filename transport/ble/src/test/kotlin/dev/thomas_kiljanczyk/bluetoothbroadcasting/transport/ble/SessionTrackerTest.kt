package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTrackerTest {
    private companion object {
        const val SESSION = 42
        const val TIMEOUT = 10_000L
    }

    private val device = RemoteDevice(sessionKey(SESSION), "Picked")
    private val adverts = MutableSharedFlow<AdvertPayload>()
    private val states = mutableListOf<ClientState>()
    private val messages = mutableListOf<String>()

    private fun TestScope.track() = launch {
        trackSession(device, SESSION, adverts, TIMEOUT, states::add, messages::add)
    }

    private fun advert(seq: Int, message: String = "m$seq", session: Int = SESSION, name: String = "Server") =
        AdvertPayload(session, seq, name, message)

    @Test
    fun `fails when session never heard`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(1, session = 7))
        advanceTimeBy(TIMEOUT + 1)

        assertTrue(job.isCompleted)
        assertEquals(listOf(ClientState.ConnectionFailed(device)), states)
        assertEquals(emptyList<String>(), messages)
    }

    @Test
    fun `connects on first advert with advertised name`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(0))
        runCurrent()

        assertEquals(listOf(ClientState.Connected(RemoteDevice(device.id, "Server"))), states)
        job.cancel()
    }

    @Test
    fun `keeps picked name when advert has none`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(0, name = ""))
        runCurrent()

        assertEquals(listOf(ClientState.Connected(device)), states)
        job.cancel()
    }

    @Test
    fun `emits each new seq once and skips seq 0`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(0, message = ""))
        adverts.emit(advert(3))
        adverts.emit(advert(3))
        adverts.emit(advert(4))
        adverts.emit(advert(4))
        runCurrent()

        assertEquals(listOf("m3", "m4"), messages)
        job.cancel()
    }

    @Test
    fun `disconnects after silence`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(1))
        advanceTimeBy(TIMEOUT - 1)
        adverts.emit(advert(1))
        advanceTimeBy(TIMEOUT - 1)
        assertEquals(1, states.size)

        advanceTimeBy(2)
        assertTrue(job.isCompleted)
        assertEquals(ClientState.Disconnected, states.last())
    }

    @Test
    fun `ignores other sessions for liveness`() = runTest {
        val job = track()
        runCurrent()
        adverts.emit(advert(1))
        advanceTimeBy(TIMEOUT / 2)
        adverts.emit(advert(2, session = 7))
        advanceTimeBy(TIMEOUT / 2 + 1)

        assertTrue(job.isCompleted)
        assertEquals(listOf("m1"), messages)
        assertEquals(ClientState.Disconnected, states.last())
    }
}
