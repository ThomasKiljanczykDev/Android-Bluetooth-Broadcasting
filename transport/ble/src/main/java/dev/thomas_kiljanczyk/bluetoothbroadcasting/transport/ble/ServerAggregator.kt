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

/** One entry per session; dropped after [timeoutMs] without an advert. */
internal fun Flow<AdvertPayload>.toServerList(timeoutMs: Long): Flow<List<RemoteDevice>> = channelFlow {
    val mutex = Mutex()
    val servers = LinkedHashMap<Int, RemoteDevice>()
    val expiries = HashMap<Int, Job>()

    collect { advert ->
        val id = advert.sessionId
        mutex.withLock {
            servers[id] = RemoteDevice(sessionKey(id), advert.name.ifEmpty { null })
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
