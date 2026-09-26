package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.RemoteDevice
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerDiscovery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BleServerDiscovery @Inject constructor(
    private val scanner: BleScanner
) : ServerDiscovery {
    override val emptyHint: Int = R.string.transport_ble_discovery_hint

    override fun discover(): Flow<List<RemoteDevice>> =
        scanner.adverts().toServerList(LIVENESS_TIMEOUT_MS)
}
