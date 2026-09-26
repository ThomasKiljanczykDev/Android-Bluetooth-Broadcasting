package dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.nearby

import android.Manifest
import android.content.Context
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Availability
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.Radio
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import javax.inject.Inject

class NearbyTransportRequirements @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TransportRequirements {

    override val runtimePermissions: Array<String> = buildList {
        val sdk = Build.VERSION.SDK_INT
        if (sdk <= Build.VERSION_CODES.P) add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (sdk in Build.VERSION_CODES.Q..Build.VERSION_CODES.S) add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (sdk >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (sdk >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.NEARBY_WIFI_DEVICES)
        if (sdk >= Build.VERSION_CODES.CINNAMON_BUN) add(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }.toTypedArray()

    // Nearby stops enabling radios itself in late 2026; one radio is not documented as sufficient.
    override val requiredRadios: Set<Radio> = setOf(Radio.Bluetooth, Radio.WiFi)

    override fun checkAvailability(): Availability {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return if (status == ConnectionResult.SUCCESS) {
            Availability.Available
        } else {
            Availability.Unavailable(R.string.transport_nearby_unavailable)
        }
    }
}
