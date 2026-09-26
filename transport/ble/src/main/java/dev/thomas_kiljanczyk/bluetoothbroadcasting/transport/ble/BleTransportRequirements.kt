package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.ble

import android.Manifest
import android.os.Build
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Availability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import javax.inject.Inject

class BleTransportRequirements @Inject constructor(
    private val adapterProvider: BleAdapterProvider
) : TransportRequirements {

    override val runtimePermissions: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    override val requiredRadios: Set<Radio> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setOf(Radio.Bluetooth)
        } else {
            setOf(Radio.Bluetooth, Radio.Location)
        }

    /** Extended advertising support is unreported while Bluetooth is off. */
    override fun checkAvailability(): Availability {
        val adapter = adapterProvider.adapterOrNull
            ?: return Availability.Unavailable(R.string.transport_ble_unavailable)
        return if (!adapter.isEnabled || adapter.isLeExtendedAdvertisingSupported) {
            Availability.Available
        } else {
            Availability.Unavailable(R.string.transport_ble_extended_unsupported)
        }
    }
}
