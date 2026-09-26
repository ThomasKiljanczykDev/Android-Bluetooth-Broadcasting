package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dagger.hilt.android.scopes.ViewModelScoped
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ListenerState { Stopped, Listening }

/** Receives adverts from every server in range. */
@ViewModelScoped
class BleListener @Inject constructor(
    private val scanner: BleScanner,
    @TransportScope private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(ListenerState.Stopped)
    val state: StateFlow<ListenerState> = _state

    private val _servers = MutableStateFlow<List<HeardServer>>(emptyList())
    val servers: StateFlow<List<HeardServer>> = _servers

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        _state.value = ListenerState.Listening
        job = scope.launch {
            scanner.adverts()
                .toHeardServers(LIVENESS_TIMEOUT_MS)
                .collect { _servers.value = it }
            _state.value = ListenerState.Stopped
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _servers.value = emptyList()
        _state.value = ListenerState.Stopped
    }
}
