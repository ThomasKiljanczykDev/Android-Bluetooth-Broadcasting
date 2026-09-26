package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.BluetoothLeAdvertiser
import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.BroadcastServer
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerEvent
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ServerState
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Non-connectable extended advertising; payload carries the current message.
 * [events] never emits: listeners are unknown. Permissions granted by the TransportRequirements gate.
 */
@SuppressLint("MissingPermission")
@ViewModelScoped
class BleBroadcastServer @Inject constructor(
    private val adapterProvider: BleAdapterProvider,
    localDeviceName: LocalDeviceName,
    @TransportScope scope: CoroutineScope
) : BroadcastServer {
    private companion object {
        const val TAG = "BleBroadcastServer"

        val PARAMETERS: AdvertisingSetParameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setConnectable(false)
            .setScannable(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_LOW)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .build()
    }

    private val _state = MutableStateFlow(ServerState.Stopped)
    override val state: StateFlow<ServerState> = _state

    override val events: Flow<ServerEvent> = emptyFlow()

    private val name = localDeviceName.value.truncateUtf8(AdvertPayload.MAX_NAME_BYTES)

    /** UTF-8 byte limit; longer [broadcast] messages are dropped. */
    val maxMessageBytes: Int = AdvertPayload.maxMessageBytes(name)

    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertisingSetCallback? = null
    private var advertisingSet: AdvertisingSet? = null
    private var payload = AdvertPayload(sessionId = 0, seq = 0, name = name, message = "")

    init {
        scope.onCancellation(::stop)
    }

    override fun start() {
        if (!_state.compareAndSet(ServerState.Stopped, ServerState.Starting)) return

        payload = AdvertPayload(sessionId = Random.nextInt(), seq = 0, name = name, message = "")
        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(set: AdvertisingSet?, txPower: Int, status: Int) {
                if (this != this@BleBroadcastServer.callback) return
                if (status == ADVERTISE_SUCCESS && set != null) {
                    advertisingSet = set
                    _state.value = ServerState.Running
                } else {
                    Log.e(TAG, "Advertising not started: $status")
                    stop()
                }
            }

            override fun onAdvertisingDataSet(set: AdvertisingSet, status: Int) {
                if (status != ADVERTISE_SUCCESS) Log.e(TAG, "Advertising data not set: $status")
            }
        }

        try {
            val advertiser = checkNotNull(adapterProvider.adapterOrNull?.bluetoothLeAdvertiser) {
                "Bluetooth off"
            }
            this.advertiser = advertiser
            this.callback = callback
            advertiser.startAdvertisingSet(PARAMETERS, payload.toAdvertiseData(), null, null, null, callback)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Advertising not started", e)
            stop()
        }
    }

    override fun stop() {
        val advertiser = advertiser
        val callback = callback
        if (advertiser != null && callback != null) {
            try {
                advertiser.stopAdvertisingSet(callback)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Advertising not stopped", e)
            }
        }
        this.advertiser = null
        this.callback = null
        advertisingSet = null
        _state.value = ServerState.Stopped
    }

    override fun broadcast(message: String) {
        val set = advertisingSet ?: return
        if (message.encodeToByteArray().size > maxMessageBytes) return

        payload = payload.copy(seq = AdvertPayload.nextSeq(payload.seq), message = message)
        set.setAdvertisingData(payload.toAdvertiseData())
    }

    private fun AdvertPayload.toAdvertiseData(): AdvertiseData =
        AdvertiseData.Builder().addServiceData(SERVICE_UUID, encode()).build()
}
