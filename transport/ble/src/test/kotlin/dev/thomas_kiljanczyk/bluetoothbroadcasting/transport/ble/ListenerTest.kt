package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ReceivedMessage
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListenerTest {
    private companion object {
        const val TIMEOUT = 10_000L
    }

    private val adverts = MutableSharedFlow<AdvertPayload>()
    private val messages = mutableListOf<ReceivedMessage>()
    private val serverLists = mutableListOf<List<RemoteDevice>>()

    private fun TestScope.listen() = launch {
        adverts.listen(TIMEOUT, messages::add).toList(serverLists)
    }

    private fun advert(session: Int, seq: Int, name: String = "S$session", message: String = "m$session.$seq") =
        AdvertPayload(session, seq, name, message)

    private fun server(session: Int, name: String? = "S$session") = RemoteDevice(sessionKey(session), name)

    @Test
    fun `receives from all servers without picking`() = runTest {
        val job = listen()
        runCurrent()
        adverts.emit(advert(1, 1))
        adverts.emit(advert(2, 5))
        runCurrent()

        assertEquals(
            listOf(ReceivedMessage(server(1), "m1.1"), ReceivedMessage(server(2), "m2.5")),
            messages
        )
        assertEquals(listOf(server(1), server(2)), serverLists.last())
        job.cancel()
    }

    @Test
    fun `emits each new seq once per server and skips seq 0`() = runTest {
        val job = listen()
        runCurrent()
        adverts.emit(advert(1, 0))
        adverts.emit(advert(1, 3))
        adverts.emit(advert(1, 3))
        adverts.emit(advert(2, 3))
        adverts.emit(advert(1, 4))
        adverts.emit(advert(1, 4))
        runCurrent()

        assertEquals(listOf("m1.3", "m2.3", "m1.4"), messages.map { it.text })
        job.cancel()
    }

    @Test
    fun `unnamed server has null name`() = runTest {
        val job = listen()
        runCurrent()
        adverts.emit(advert(1, 1, name = ""))
        runCurrent()

        assertEquals(server(1, null), messages.single().sender)
        job.cancel()
    }

    @Test
    fun `drops silent servers`() = runTest {
        val job = listen()
        runCurrent()
        adverts.emit(advert(1, 1))
        adverts.emit(advert(2, 1))
        advanceTimeBy(TIMEOUT - 1)
        adverts.emit(advert(2, 1))
        advanceTimeBy(2)
        assertEquals(listOf(server(2)), serverLists.last())

        advanceTimeBy(TIMEOUT)
        assertEquals(emptyList<RemoteDevice>(), serverLists.last())
        assertEquals(2, messages.size)
        job.cancel()
    }
}
