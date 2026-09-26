package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

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
class HeardServersTest {
    private companion object {
        const val TIMEOUT = 10_000L
    }

    private val adverts = MutableSharedFlow<AdvertPayload>()
    private val lists = mutableListOf<List<HeardServer>>()

    private fun TestScope.collect() = launch { adverts.toHeardServers(TIMEOUT).toList(lists) }

    private fun advert(session: Int, seq: Int, message: String = "m$session.$seq", name: String = "S$session") =
        AdvertPayload(session, seq, name, message)

    private fun heard(session: Int, message: String?, name: String? = "S$session") =
        HeardServer(RemoteDevice(sessionKey(session), name), message)

    @Test
    fun `tracks current message per server in first-heard order`() = runTest {
        val job = collect()
        runCurrent()
        adverts.emit(advert(1, 1))
        adverts.emit(advert(2, 4))
        adverts.emit(advert(1, 2))
        runCurrent()

        assertEquals(listOf(heard(1, "m1.2"), heard(2, "m2.4")), lists.last())
        job.cancel()
    }

    @Test
    fun `seq 0 has no message`() = runTest {
        val job = collect()
        runCurrent()
        adverts.emit(advert(1, 0, message = ""))
        runCurrent()

        assertEquals(listOf(heard(1, null)), lists.last())
        job.cancel()
    }

    @Test
    fun `unnamed server has null name`() = runTest {
        val job = collect()
        runCurrent()
        adverts.emit(advert(1, 1, name = ""))
        runCurrent()

        assertEquals(listOf(heard(1, "m1.1", name = null)), lists.last())
        job.cancel()
    }

    @Test
    fun `repeated adverts emit once`() = runTest {
        val job = collect()
        runCurrent()
        repeat(5) { adverts.emit(advert(1, 1)) }
        runCurrent()

        assertEquals(listOf(listOf(heard(1, "m1.1"))), lists)
        job.cancel()
    }

    @Test
    fun `drops silent servers`() = runTest {
        val job = collect()
        runCurrent()
        adverts.emit(advert(1, 1))
        adverts.emit(advert(2, 1))
        advanceTimeBy(TIMEOUT - 1)
        adverts.emit(advert(2, 1))
        advanceTimeBy(2)
        assertEquals(listOf(heard(2, "m2.1")), lists.last())

        advanceTimeBy(TIMEOUT)
        assertEquals(emptyList<HeardServer>(), lists.last())
        job.cancel()
    }
}
