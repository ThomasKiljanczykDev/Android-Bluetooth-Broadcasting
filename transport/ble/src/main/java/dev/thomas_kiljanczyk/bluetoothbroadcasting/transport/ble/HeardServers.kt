package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** [device] id: session id as 8 hex digits; [message] null until the server sends one. */
data class HeardServer(val device: RemoteDevice, val message: String?)

/** Latest advert per session, first-heard order; dropped after [timeoutMs] without an advert. */
internal fun Flow<AdvertPayload>.toHeardServers(timeoutMs: Long): Flow<List<HeardServer>> = channelFlow {
    val mutex = Mutex()
    val servers = LinkedHashMap<Int, HeardServer>()
    val expiries = HashMap<Int, Job>()

    collect { advert ->
        val id = advert.sessionId
        mutex.withLock {
            servers[id] = advert.toHeardServer()
            expiries.remove(id)?.cancel()
            expiries[id] = launch {
                delay(timeoutMs)
                mutex.withLock {
                    servers.remove(id)
                    expiries.remove(id)
                    send(servers.values.toList())
                }
            }
            send(servers.values.toList())
        }
    }
}.distinctUntilChanged()

private fun AdvertPayload.toHeardServer() = HeardServer(
    device = RemoteDevice(sessionKey(sessionId), name.ifEmpty { null }),
    message = message.takeIf { seq != 0 }
)
