package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServerAggregatorTest {
    private companion object {
        const val TIMEOUT = 10_000L
    }

    private val adverts = MutableSharedFlow<AdvertPayload>()

    private fun advert(session: Int, name: String, seq: Int = 0) = AdvertPayload(session, seq, name, "")

    private fun server(session: Int, name: String?) = RemoteDevice(sessionKey(session), name)

    @Test
    fun `groups by session, dedupes and expires`() = runTest {
        val lists = mutableListOf<List<RemoteDevice>>()
        val job = launch { adverts.toServerList(TIMEOUT).toList(lists) }
        runCurrent()

        adverts.emit(advert(1, "A"))
        adverts.emit(advert(1, "A", seq = 5))
        adverts.emit(advert(2, ""))
        advanceTimeBy(TIMEOUT - 1)
        adverts.emit(advert(1, "A"))
        advanceTimeBy(2)
        advanceTimeBy(TIMEOUT)

        assertEquals(
            listOf(
                listOf(server(1, "A")),
                listOf(server(1, "A"), server(2, null)),
                listOf(server(1, "A")),
                emptyList()
            ),
            lists
        )
        job.cancel()
    }
}
