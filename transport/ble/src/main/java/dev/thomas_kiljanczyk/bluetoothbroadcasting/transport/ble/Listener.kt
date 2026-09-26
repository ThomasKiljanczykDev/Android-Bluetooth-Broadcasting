package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ReceivedMessage
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

/**
 * Servers heard within [timeoutMs]; each new non-zero seq per session goes to [onMessage].
 * A session's current message is delivered on first reception.
 */
internal fun Flow<AdvertPayload>.listen(
    timeoutMs: Long,
    onMessage: suspend (ReceivedMessage) -> Unit
): Flow<List<RemoteDevice>> = flow {
    val lastSeqs = HashMap<Int, Int>()
    val adverts = this@listen.onEach { advert ->
        if (advert.seq != 0 && lastSeqs.put(advert.sessionId, advert.seq) != advert.seq) {
            onMessage(ReceivedMessage(advert.toRemoteDevice(), advert.message))
        }
    }
    emitAll(adverts.toServerList(timeoutMs))
}

internal fun AdvertPayload.toRemoteDevice() = RemoteDevice(sessionKey(sessionId), name.ifEmpty { null })
