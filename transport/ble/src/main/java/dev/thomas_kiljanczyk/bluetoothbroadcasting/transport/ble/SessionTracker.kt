package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ClientState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Follows one server session until no matching advert arrives for [timeoutMs].
 * Ends with [ClientState.Disconnected] if the session was heard, else [ClientState.ConnectionFailed].
 */
internal suspend fun trackSession(
    device: RemoteDevice,
    sessionId: Int,
    adverts: Flow<AdvertPayload>,
    timeoutMs: Long,
    onState: (ClientState) -> Unit,
    onMessage: suspend (String) -> Unit
) {
    var connected = false
    coroutineScope {
        val channel = adverts.filter { it.sessionId == sessionId }.produceIn(this)
        try {
            var lastSeq = 0
            while (true) {
                val advert = withTimeoutOrNull(timeoutMs) {
                    channel.receiveCatching().getOrNull()
                } ?: break
                if (!connected) {
                    connected = true
                    onState(ClientState.Connected(device.copy(name = advert.name.ifEmpty { device.name })))
                }
                if (advert.seq != 0 && advert.seq != lastSeq) {
                    lastSeq = advert.seq
                    onMessage(advert.message)
                }
            }
        } finally {
            channel.cancel()
        }
    }
    onState(if (connected) ClientState.Disconnected else ClientState.ConnectionFailed(device))
}
