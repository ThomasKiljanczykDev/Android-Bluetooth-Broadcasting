package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.rfcomm

import android.Manifest
import android.os.Build
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Availability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import javax.inject.Inject

class RfcommTransportRequirements @Inject constructor(
    private val adapterProvider: RfcommAdapterProvider
) : TransportRequirements {

    override val runtimePermissions: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            emptyArray()
        }

    override val requiredRadios: Set<Radio> = setOf(Radio.Bluetooth)

    override fun checkAvailability(): Availability =
        if (adapterProvider.adapterOrNull != null) {
            Availability.Available
        } else {
            Availability.Unavailable(R.string.transport_rfcomm_unavailable)
        }
}
